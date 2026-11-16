package com.jobpilot.pad.exec

import android.view.accessibility.AccessibilityNodeInfo
import com.jobpilot.pad.A11yBridge
import com.jobpilot.pad.PanelState
import com.jobpilot.pad.data.Db
import com.jobpilot.pad.data.Job
import com.jobpilot.pad.data.Keys

/**
 * 批量投递：从 BOSS 职位列表页开始，逐个「找到岗位 → 进详情 → 投递 → 退回列表」。
 *
 * 和 UPCV 的差别：它是在网页里点 DOM，我们是在原生 App 里点控件树，
 * 所以每一步都要重新读窗口、每一步都可能失败，失败就跳过这一条继续下一条。
 *
 * 三条硬约束：
 *   1. 每条投递前都过 Safety（重复/同公司/配额/间隔）
 *   2. 每条之间强制等 45–120 秒
 *   3. 任何时刻可以 stop()
 */
object BatchRunner {

    private const val PKG = "com.hpbr.bosszhipin"

    @Volatile
    private var stopFlag = false

    fun stop() {
        stopFlag = true
    }

    data class Prog(
        val index: Int,
        val total: Int,
        val company: String,
        val phase: String,
        val ok: Int,
        val fail: Int,
        val waitSec: Long = 0,
    )

    data class Outcome(val job: Job, val level: String, val note: String)

    fun run(
        jobs: List<Job>,
        db: Db,
        greeting: (Job) -> String,
        onProg: (Prog) -> Unit,
    ): List<Outcome> {
        stopFlag = false
        val out = ArrayList<Outcome>()
        var ok = 0
        var fail = 0

        PanelState.beginBatch(jobs.size)
        PanelState.todayCount = runCatching { db.countDeliveredToday() }.getOrDefault(0)
        AppExecutor.onStep = { s ->
            PanelState.addStep(s.name, s.ok, s.detail)
        }

        try {
            for ((i, job) in jobs.withIndex()) {
                if (stopFlag) break
                awaitIfPaused()
                if (stopFlag) break

                PanelState.batchDone = i
                PanelState.curCompany = job.company
                PanelState.curTitle = job.title
                PanelState.curStartedAt = System.currentTimeMillis()
                PanelState.waitSec = 0
                PanelState.resetSteps()
                updateEta(i, jobs.size)

                // ---- 1. 安全阀 ----
                PanelState.curAction = "检查安全阀：重复 / 同公司 / 配额 / 间隔"
                PanelState.beginStep("检查安全阀")
                onProg(Prog(i, jobs.size, job.company, "检查安全阀", ok, fail))
                val gate = Safety.check(db, job)
                PanelState.endStep(gate.ok, gate.reason.lineSequence().first().take(28))
                if (!gate.ok) {
                    fail++
                    val reason = gate.reason.lineSequence().first()
                    val isDup = reason.contains("重复") || reason.contains("已投") || reason.contains("已发")
                    if (isDup) PanelState.dup++ else PanelState.fail++
                    PanelState.log("${job.company} 跳过：$reason")
                    out.add(Outcome(job, "skipped", reason))
                    if (!isDup && registerFail()) break
                    continue
                }

                // ---- 2. 进详情 ----
                PanelState.curAction = "在列表里定位「${job.company}」的卡片并点进详情"
                PanelState.beginStep("定位卡片 · 进详情")
                onProg(Prog(i, jobs.size, job.company, "打开岗位详情", ok, fail))
                val navigated = navigateToDetail(job)
                PanelState.endStep(navigated, if (navigated) "" else "没找到卡片")
                if (!navigated) {
                    fail++
                    PanelState.fail++
                    PanelState.log("${job.company} 列表里没找到卡片")
                    out.add(Outcome(job, "not_found", "列表里没找到「${job.company}」的卡片"))
                    if (registerFail()) break
                    continue
                }

                // ---- 3. 投递（步骤由 AppExecutor.onStep 实时上报）----
                PanelState.curAction = "点「立即沟通」→ 校验会话 → 填话术 → 读回指纹"
                onProg(Prog(i, jobs.size, job.company, "投递中", ok, fail))
                awaitIfPaused()
                val r = AppExecutor.deliver(job, greeting(job), dryRun = false, waitBossMs = 0)
                val note = r.steps.lastOrNull { !it.ok }?.detail
                    ?: r.steps.lastOrNull()?.detail
                    ?: ""
                db.recordDelivery(job, r.level, note)
                when (r.level) {
                    "delivered" -> {
                        ok++
                        PanelState.l1++; PanelState.l2++; PanelState.l3++
                        PanelState.consecutiveFail = 0
                    }
                    "bound" -> {
                        fail++; PanelState.l1++; PanelState.l2++; PanelState.fail++
                    }
                    "clicked" -> {
                        fail++; PanelState.l1++; PanelState.fail++
                    }
                    else -> {
                        fail++; PanelState.fail++
                    }
                }
                PanelState.log("${job.company} → ${r.level} ${note.take(30)}")
                out.add(Outcome(job, r.level, note))
                if (r.level != "delivered" && registerFail()) break

                PanelState.batchDone = i + 1

                // ---- 4. 回列表 ----
                PanelState.curAction = "退回职位列表"
                backToList()

                // ---- 5. 间隔 ----
                if (i < jobs.lastIndex && !stopFlag) {
                    val gap = Safety.nextGapSec()
                    var left = gap
                    while (left > 0 && !stopFlag) {
                        awaitIfPaused()
                        PanelState.waitSec = left
                        onProg(Prog(i + 1, jobs.size, "", "冷却", ok, fail, left))
                        Thread.sleep(1000)
                        left--
                    }
                    PanelState.waitSec = 0
                }
            }
        } finally {
            AppExecutor.onStep = null
            PanelState.batchDone = out.size
            PanelState.todayCount = runCatching { db.countDeliveredToday() }.getOrDefault(0)
            PanelState.finishBatch()
        }
        return out
    }

    /** 连续失败计数 +1；到阈值就熔断并返回 true */
    private fun registerFail(): Boolean {
        PanelState.consecutiveFail++
        if (PanelState.consecutiveFail >= PanelState.BREAKER_THRESHOLD) {
            PanelState.tripBreaker("连续 ${PanelState.consecutiveFail} 次未送达，已自动停止")
            stopFlag = true
            return true
        }
        return false
    }

    /** 暂停时原地等待，直到继续或终止 */
    private fun awaitIfPaused() {
        while (PanelState.phase == PanelState.Phase.PAUSED && !stopFlag) {
            Thread.sleep(300)
        }
    }

    private fun updateEta(index: Int, total: Int) {
        val remaining = (total - index).coerceAtLeast(0)
        val perJob = (PanelState.gapMin + PanelState.gapMax) / 2 + 40
        PanelState.etaSec = (remaining * perJob).toLong()
    }


    // ------------------------------------------------------------------ 导航

    /** 从列表页找到这家公司的卡片并点进详情；已经在详情页就直接返回 true */
    fun navigateToDetail(job: Job): Boolean {
        val svc = A11yBridge.service ?: return false
        var root = rootOf(svc) ?: return false

        // 已经停在某个岗位的详情页
        if (byId(root, "$PKG:id/btn_chat").isNotEmpty()) return true

        // 列表页：按公司名找卡片
        val candidates = ArrayList<AccessibilityNodeInfo>()
        for (kw in listOf(job.company, Keys.normCompany(job.company))) {
            if (kw.isBlank()) continue
            runCatching { root.findAccessibilityNodeInfosByText(kw) }.getOrNull()
                ?.let { candidates.addAll(it) }
            if (candidates.isNotEmpty()) break
        }
        if (candidates.isEmpty()) return false

        val card = candidates.asSequence()
            .mapNotNull { clickableSelfOrAncestor(it) }
            .firstOrNull() ?: return false

        if (!card.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return false

        // 等详情页出现（有「立即沟通」）
        var waited = 0
        while (waited < 6000) {
            Thread.sleep(400); waited += 400
            root = rootOf(svc) ?: continue
            if (byId(root, "$PKG:id/btn_chat").isNotEmpty()) return true
        }
        return false
    }

        fun backToList() {
        val svc = A11yBridge.service ?: return
        repeat(2) {
            runCatching {
                svc.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
            }
            Thread.sleep(800)
        }
    }

    // ------------------------------------------------------------------ 工具

    private fun rootOf(svc: android.accessibilityservice.AccessibilityService) =
        runCatching { svc.rootInActiveWindow }.getOrNull()

    private fun byId(root: AccessibilityNodeInfo?, id: String): List<AccessibilityNodeInfo> =
        runCatching { root?.findAccessibilityNodeInfosByViewId(id) }.getOrNull().orEmpty()

    private fun clickableSelfOrAncestor(n: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        var cur = n
        var depth = 0
        while (cur != null && depth < 8) {
            if (cur.isClickable && cur.isEnabled) return cur
            cur = runCatching { cur?.parent }.getOrNull()
            depth++
        }
        return null
    }
}
