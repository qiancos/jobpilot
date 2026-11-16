package com.jobpilot.pad.exec

import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import com.jobpilot.pad.A11yBridge
import com.jobpilot.pad.data.Job
import com.jobpilot.pad.data.Keys

/**
 * 在 BOSS 原生 App 里完成一次投递。
 *
 * 前置：BOSS 已经停在**目标岗位的详情页**（用户自己点进去，或后续由列表自动进入）。
 * 流程：详情页「立即沟通」→ 进聊天 → 核对公司名 → 填话术 →（可选）发送 → 读回最后一条自己消息。
 *
 * 三态核验：
 *   L1 clicked  —— 点了「立即沟通」
 *   L2 bound    —— 进了聊天页，且 tv_sub_title 里的公司对得上
 *   L3 delivered—— 最后一条自己发的消息里能找到话术指纹
 */
object AppExecutor {

    private const val PKG = "com.hpbr.bosszhipin"

    data class Step(val name: String, val ok: Boolean, val detail: String = "")
    data class Result(
        val ok: Boolean,
        val level: String,          // none / clicked / bound / delivered
        val steps: List<Step>,
    )

    /**
     * @param dryRun true = 只填进输入框并读回校验，不点发送
     */
    fun deliver(job: Job, text: String, dryRun: Boolean, waitBossMs: Long = 0): Result {
        val steps = ArrayList<Step>()
        val svc = A11yBridge.service
            ?: return fail(steps, "无障碍", "服务未连接，无法操作 BOSS")

        if (!text.isBlank() && text.length > 200) {
            return fail(steps, "话术", "太长（${text.length} 字），BOSS 输入框会截断")
        }

        // ---------- 0. 等 BOSS 回到前台（用户从清单页切过来的时间） ----------
        if (waitBossMs > 0) {
            val arrived = poll(waitBossMs) {
                if (rootOrNull(svc)?.packageName == PKG) "ok" else null
            }
            if (arrived == null) {
                return fail(steps, "等待 BOSS", "${waitBossMs / 1000} 秒内没检测到 BOSS 在前台")
            }
        }

        // ---------- 0b. 确认当前是 BOSS ----------
        var root = svc.rootInActiveWindow
            ?: return fail(steps, "读取窗口", "拿不到窗口内容")
        if (root.packageName != PKG) {
            return fail(steps, "前台检查", "当前不是 BOSS（${root.packageName}），请先切到 BOSS 岗位详情页")
        }
        steps.add(Step("前台检查", true, "BOSS 在前台"))

        // ---------- 1. 详情页：点「立即沟通」 ----------
        val chatBtn = poll(4000) {
            byId(rootOrNull(svc), "$PKG:id/btn_chat").firstOrNull { it.isClickable }
                ?: byText(rootOrNull(svc), "立即沟通").firstOrNull { it.isClickable }
        }
        val clicked = chatBtn?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true
        if (!clicked) {
            // 可能已经在聊天页了（用户手动进来的），继续往后走
            steps.add(Step("点击立即沟通", false, "没找到按钮，尝试按聊天页继续"))
        } else {
            steps.add(Step("点击立即沟通", true))
        }

        // ---------- 2. 等聊天页出现 ----------
        val input = poll(6000) {
            byId(rootOrNull(svc), "$PKG:id/editText_with_scrollbar")
                .firstOrNull { it.isEditable }
        } ?: return fail(steps, "进入聊天", "6 秒内没等到输入框，可能没进到聊天页")
        steps.add(Step("进入聊天", true))

        // ---------- 3. 核对公司名（唯一不可挽回错误的守门人） ----------
        val subTitle = byId(rootOrNull(svc), "$PKG:id/tv_sub_title")
            .firstOrNull()?.text?.toString().orEmpty()
        val title = byId(rootOrNull(svc), "$PKG:id/tv_title")
            .firstOrNull()?.text?.toString().orEmpty()
        val want = Keys.normCompany(job.company)
        val got = Keys.normCompany(subTitle.substringBefore("·").ifBlank { subTitle })
        if (want.isNotBlank() && got.isNotBlank() && !similar(want, got)) {
            return Result(
                false, "clicked", steps + Step(
                    "核对公司名", false,
                    "对不上：期望「$want」，实际「$got」（$title / $subTitle）——已放弃，没有发送"
                )
            )
        }
        steps.add(Step("核对公司名", true, "$title · $subTitle"))

        // ---------- 4. 填话术 ----------
        val marker = text.takeLast(8)
        var ok = false
        input.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        val args = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text
            )
        }
        ok = input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        if (!ok) {
            return fail(steps, "填入话术", "ACTION_SET_TEXT 被拒绝（输入框可能没聚焦）")
        }
        // 读回校验
        val readBack = poll(2000) {
            byId(rootOrNull(svc), "$PKG:id/editText_with_scrollbar")
                .firstOrNull { it.isEditable }
                ?.text?.toString()
                ?.takeIf { it.isNotBlank() }
        }.orEmpty()
        if (!readBack.contains(marker)) {
            return fail(
                steps, "填入话术",
                "读回不一致：期望含「$marker」，实际「${readBack.take(30)}」"
            )
        }
        steps.add(Step("填入话术", true, "${readBack.length} 字"))

        if (dryRun) {
            steps.add(Step("试跑模式", true, "已停在输入框，没有点发送"))
            return Result(true, "bound", steps)
        }

        // ---------- 5. 发送 ----------
        val sendBtn = poll(3000) {
            byText(rootOrNull(svc), "发送").firstOrNull { it.isClickable }
                ?: byIdContains(rootOrNull(svc), "send").firstOrNull { it.isClickable }
        } ?: return fail(steps, "找发送按钮", "没找到「发送」，话术还留在输入框里")
        sendBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        steps.add(Step("点击发送", true))

        // ---------- 6. 核验送达 ----------
        Thread.sleep(1800)
        val mine = myMessages(rootOrNull(svc))
        val hit = mine.any { it.contains(marker) }
        return if (hit) {
            steps.add(Step("核验送达", true, "L3 已核对指纹「$marker」"))
            Result(true, "delivered", steps)
        } else {
            steps.add(
                Step("核验送达", false, "没找到含「$marker」的消息；最后 3 条：${mine.takeLast(3).joinToString(" / ")}")
            )
            Result(false, "bound", steps)
        }
    }

    // ------------------------------------------------------------------ 工具

    private fun fail(steps: MutableList<Step>, name: String, detail: String): Result {
        steps.add(Step(name, false, detail))
        return Result(false, "none", steps)
    }

    private fun rootOrNull(svc: android.accessibilityservice.AccessibilityService): AccessibilityNodeInfo? =
        runCatching { svc.rootInActiveWindow }.getOrNull()

    private fun byId(root: AccessibilityNodeInfo?, id: String): List<AccessibilityNodeInfo> =
        runCatching { root?.findAccessibilityNodeInfosByViewId(id) }.getOrNull().orEmpty()

    private fun byText(root: AccessibilityNodeInfo?, text: String): List<AccessibilityNodeInfo> =
        runCatching { root?.findAccessibilityNodeInfosByText(text) }.getOrNull().orEmpty()

    private fun byIdContains(root: AccessibilityNodeInfo?, key: String): List<AccessibilityNodeInfo> {
        val out = ArrayList<AccessibilityNodeInfo>()
        walk(root, 0) { n ->
            val id = runCatching { n.viewIdResourceName }.getOrNull().orEmpty()
            if (id.contains(key, true)) out.add(n)
        }
        return out
    }

    private fun walk(n: AccessibilityNodeInfo?, depth: Int, fn: (AccessibilityNodeInfo) -> Unit) {
        if (n == null || depth > 40) return
        fn(n)
        for (i in 0 until runCatching { n.childCount }.getOrDefault(0)) {
            walk(runCatching { n.getChild(i) }.getOrNull(), depth + 1, fn)
        }
    }

    /** 自己的消息：屏幕右半边 */
    private fun myMessages(root: AccessibilityNodeInfo?): List<String> {
        val screenW = runCatching {
            A11yBridge.service?.resources?.displayMetrics?.widthPixels ?: 0
        }.getOrDefault(0)
        val out = ArrayList<String>()
        for (node in byId(root, "$PKG:id/tv_content_text")) {
            val b = android.graphics.Rect()
            node.getBoundsInScreen(b)
            val centerX = (b.left + b.right) / 2
            if (screenW > 0 && centerX > screenW / 2) {
                node.text?.toString()?.let { out.add(it) }
            }
        }
        return out
    }

    private fun similar(a: String, b: String): Boolean {
        if (a.isBlank() || b.isBlank()) return false
        if (a.contains(b) || b.contains(a)) return true
        // 字符重合度
        val common = a.toSet().intersect(b.toSet()).size
        return common.toDouble() / maxOf(a.length, b.length) >= 0.5
    }

    private fun <T> poll(timeoutMs: Long, block: () -> T?): T? {
        val end = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < end) {
            val v = runCatching(block).getOrNull()
            if (v != null) return v
            Thread.sleep(400)
        }
        return null
    }
}
