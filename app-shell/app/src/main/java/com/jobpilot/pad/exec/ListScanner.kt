package com.jobpilot.pad.exec

import android.content.Context
import android.view.accessibility.AccessibilityNodeInfo
import com.jobpilot.pad.A11yBridge
import com.jobpilot.pad.data.Db
import com.jobpilot.pad.data.Job
import com.jobpilot.pad.data.Keys

/**
 * 在 BOSS「职位」列表页上直接扫：
 * 读卡片 → 逐个点进详情 → 读 JD → 模型打分 → 退回列表 → 下一个。
 *
 * 因为悬浮面板不属于 BOSS，点它不会把 BOSS 挤到后台，
 * 所以整个流程 BOSS 始终在前台，用户能看到它在翻页。
 */
object ListScanner {

    private const val PKG = "com.hpbr.bosszhipin"
    private const val CARD_ID = "$PKG:id/view_job_card"

    @Volatile
    private var stopFlag = false

    fun stop() {
        stopFlag = true
    }

    data class Card(val company: String, val title: String, val salary: String, val area: String)

    data class Prog(
        val index: Int,
        val total: Int,
        val company: String,
        val phase: String,
        val score: Int,
        val ok: Int,
        val fail: Int,
    )

    /** 读当前列表页所有卡片的文字 */
    fun readCards(): List<Card> {
        val svc = A11yBridge.service ?: return emptyList()
        val out = ArrayList<Card>()
        for (root in bossRoots(svc)) {
            val nodes = runCatching { root.findAccessibilityNodeInfosByViewId(CARD_ID) }.getOrNull()
                ?: continue
            for (n in nodes) {
                val texts = ArrayList<String>()
                walk(n, 0) { node ->
                    val t = node.text?.toString()?.trim().orEmpty()
                    if (t.isNotEmpty() && t.length < 40 && !texts.contains(t)) texts.add(t)
                }
                if (texts.isEmpty()) continue
                // 约定：第一行是岗位名，第二行是「公司 · 地区 · 薪资」这类
                val title = texts.firstOrNull().orEmpty()
                val rest = texts.drop(1)
                val company = rest.firstOrNull()?.substringBefore("·")?.trim().orEmpty()
                val salary = Regex("(\\d+[-–]\\d+\\s*[Kk]|\\d+[-–]\\d+元/天|面议)")
                    .find(texts.joinToString(" "))?.value.orEmpty()
                val area = rest.firstOrNull { it.contains("·") }?.trim().orEmpty()
                if (title.isNotBlank()) out.add(Card(company, title, salary, area))
            }
            if (out.isNotEmpty()) break
        }
        return out
    }

    /**
     * 扫描并打分。
     * @param limit 最多处理几个
     */
    fun scanAndScore(ctx: Context, db: Db, limit: Int, onP: (Prog) -> Unit): Int {
        stopFlag = false
        val cards = readCards()
        if (cards.isEmpty()) {
            Log.i(ctx, "scan", "列表页没读到卡片")
            return 0
        }
        Log.i(ctx, "scan", "读到 ${cards.size} 张卡片，最多处理 $limit")
        val resume = ResumeStore.current(ctx)
        var ok = 0
        var fail = 0

        val todo = cards.take(limit)
        for ((i, card) in todo.withIndex()) {
            if (stopFlag) break
            onP(Prog(i, todo.size, card.company, "打开详情", 0, ok, fail))

            // 每次都重新读卡片再点，避免列表刷新导致错位
            val node = findCardNode(card)
            if (node == null) {
                fail++
                onP(Prog(i, todo.size, card.company, "列表里没找到", 0, ok, fail))
                continue
            }
            if (!clickCard(node)) {
                fail++
                onP(Prog(i, todo.size, card.company, "点不动", 0, ok, fail))
                continue
            }

            // 等 JD
            var jd = ""
            var w = 0
            while (w < 6000) {
                Thread.sleep(400); w += 400
                jd = JdReader.read(ctx)
                if (jd.length >= 80) break
            }
            if (jd.length < 80) {
                fail++
                Log.i(ctx, "scan", "[${card.company}] JD 只有 ${jd.length} 字，跳过")
                onP(Prog(i, todo.size, card.company, "JD 读不到", 0, ok, fail))
                back(); Thread.sleep(700)
                continue
            }

            onP(Prog(i, todo.size, card.company, "模型打分", 0, ok, fail))
            val job = Job(
                id = "app_" + (card.company + "|" + card.title).hashCode(),
                title = card.title, company = card.company,
                salary = card.salary, area = card.area,
                tags = "", href = "boss://${card.company}/${card.title}",
                keyword = "", collectedAt = System.currentTimeMillis(), source = "app",
            )
            val v = LlmScorer.score(ctx, job, resume, jd)
            if (v.ok) {
                val need = if (v.verify.isEmpty()) "" else "【待核实】" + v.verify.joinToString("；")
                db.saveScore(
                    job.id, v.score, "", "",
                    v.pros.joinToString("；"),
                    (v.gaps + listOfNotNull(need.ifBlank { null })).joinToString("；"),
                    v.advice
                )
                ok++
                Log.i(ctx, "scan", "[${card.company}] ${v.score} 分")
                onP(Prog(i, todo.size, card.company, "完成 ${v.score} 分", v.score, ok, fail))
            } else {
                fail++
                Log.i(ctx, "scan", "[${card.company}] 失败：${v.err}")
                onP(Prog(i, todo.size, card.company, "失败", 0, ok, fail))
            }

            back()
            Thread.sleep(900)
        }
        Log.i(ctx, "scan", "结束：成功 $ok / 失败 $fail")
        return ok
    }

    // ------------------------------------------------------------------ 工具

    private fun bossRoots(svc: android.accessibilityservice.AccessibilityService): List<AccessibilityNodeInfo> {
        val roots = ArrayList<AccessibilityNodeInfo>()
        runCatching {
            svc.windows.forEach { w ->
                val r = runCatching { w.root }.getOrNull()
                if (r != null && r.packageName == PKG) roots.add(r)
            }
        }
        val a = runCatching { svc.rootInActiveWindow }.getOrNull()
        if (a != null && a.packageName == PKG) roots.add(a)
        return roots
    }

    /** 按 公司+岗位名 重新定位卡片（列表可能已经变了） */
    private fun findCardNode(card: Card): AccessibilityNodeInfo? {
        val svc = A11yBridge.service ?: return null
        for (root in bossRoots(svc)) {
            val nodes = runCatching { root.findAccessibilityNodeInfosByViewId(CARD_ID) }.getOrNull()
                ?: continue
            for (n in nodes) {
                var found = false
                walk(n, 0) { node ->
                    val t = node.text?.toString().orEmpty()
                    if (t.contains(card.title.take(8)) || (card.company.isNotBlank() && t.contains(card.company))) {
                        found = true
                    }
                }
                if (found) return n
            }
        }
        return null
    }

    private fun clickCard(n: AccessibilityNodeInfo): Boolean {
        var cur: AccessibilityNodeInfo? = n
        var d = 0
        while (cur != null && d < 5) {
            if (cur.isClickable && cur.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
            cur = runCatching { cur?.parent }.getOrNull()
            d++
        }
        // 直接点卡片本身
        return n.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun back() {
        runCatching {
            A11yBridge.service?.performGlobalAction(
                android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
            )
        }
    }

    private fun walk(n: AccessibilityNodeInfo?, depth: Int, fn: (AccessibilityNodeInfo) -> Unit) {
        if (n == null || depth > 30) return
        fn(n)
        for (i in 0 until runCatching { n.childCount }.getOrDefault(0)) {
            walk(runCatching { n.getChild(i) }.getOrNull(), depth + 1, fn)
        }
    }
}
