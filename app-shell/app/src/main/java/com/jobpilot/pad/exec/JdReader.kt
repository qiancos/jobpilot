package com.jobpilot.pad.exec

import android.content.Context
import android.view.accessibility.AccessibilityNodeInfo
import com.jobpilot.pad.A11yBridge

/**
 * 读 BOSS 岗位详情页的 JD 正文。
 *
 * v2 修正：BOSS 的详情页可能是独立窗口，rootInActiveWindow 拿不到，
 * 必须遍历 service.windows 里所有 root。
 *
 * 三级兜底：
 *   1. tv_description
 *   2. 常见 id 变体
 *   3. 全屏最长且像 JD 的文本
 */
object JdReader {

    private const val PKG = "com.hpbr.bosszhipin"

    private val ID_CANDIDATES = listOf(
        "$PKG:id/tv_description",
        "$PKG:id/tv_job_desc",
        "$PKG:id/job_detail",
        "$PKG:id/tv_detail",
        "$PKG:id/description",
        "$PKG:id/tv_job_description",
    )

    data class Hit(val text: String, val source: String, val windows: Int)

    fun read(ctx: Context? = null): String = readDetailed(ctx).text

    fun readDetailed(ctx: Context? = null): Hit {
        val svc = A11yBridge.service ?: return Hit("", "无障碍未开", 0)

        val roots = ArrayList<AccessibilityNodeInfo>()
        runCatching {
            svc.windows.forEach { w ->
                val r = runCatching { w.root }.getOrNull()
                if (r != null && r.packageName == PKG) roots.add(r)
            }
        }
        val active = runCatching { svc.rootInActiveWindow }.getOrNull()
        if (active != null && active.packageName == PKG) roots.add(active)

        if (roots.isEmpty()) return Hit("", "没有 BOSS 窗口", 0)

        // 1) 按 id 找
        for (id in ID_CANDIDATES) {
            for (root in roots) {
                val nodes = runCatching { root.findAccessibilityNodeInfosByViewId(id) }.getOrNull()
                    ?: continue
                val sb = StringBuilder()
                for (n in nodes) {
                    val t = n.text?.toString().orEmpty()
                    if (t.length > 20) sb.append(t).append('\n')
                }
                if (sb.length >= 80) {
                    ctx?.let { Log.i(it, "jd", "命中 id=$id 窗口数=${roots.size} 字数=${sb.length}") }
                    return Hit(sb.toString().trim(), "id:$id", roots.size)
                }
            }
        }

        // 2) 全屏兜底
        var best = ""
        var bestId = ""
        for (root in roots) {
            walk(root, 0) { n ->
                val t = n.text?.toString().orEmpty()
                if (t.length > best.length && looksLikeJd(t)) {
                    best = t
                    bestId = runCatching { n.viewIdResourceName }.getOrNull().orEmpty()
                }
            }
        }
        if (best.length >= 80) {
            ctx?.let { Log.i(it, "jd", "兜底全屏 字数=${best.length} id=${bestId.ifBlank { "-" }}") }
            return Hit(best.trim(), "fallback:${bestId.ifBlank { "longest" }}", roots.size)
        }

        ctx?.let { Log.i(it, "jd", "没读到 JD：${roots.size} 个窗口，最长文本 ${best.length} 字") }
        return Hit("", "未命中", roots.size)
    }

    private fun looksLikeJd(t: String): Boolean {
        if (t.length < 80) return false
        val kw = listOf("职责", "要求", "负责", "熟悉", "任职", "经验", "优先")
        return kw.any { t.contains(it) } || t.length > 300
    }

    private fun walk(n: AccessibilityNodeInfo?, depth: Int, fn: (AccessibilityNodeInfo) -> Unit) {
        if (n == null || depth > 40) return
        fn(n)
        for (i in 0 until runCatching { n.childCount }.getOrDefault(0)) {
            walk(runCatching { n.getChild(i) }.getOrNull(), depth + 1, fn)
        }
    }
}
