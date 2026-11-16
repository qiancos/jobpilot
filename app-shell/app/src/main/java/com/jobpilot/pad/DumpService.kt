package com.jobpilot.pad

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.io.File

/**
 * M0-APP 验证用：把当前前台窗口的控件树 dump 到文件。
 *
 * 目的只有一个：搞清楚 BOSS App 的界面结构能不能被无障碍读到。
 * 读得到 → 原生 App 路线可行；读不到 → 只能退回网页版。
 *
 * 只读，不点、不滑、不输入。
 */
class DumpService : AccessibilityService() {

    private var lastDump = 0L
    private var lastName = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        A11yBridge.service = this
        Log.i(TAG, "a11y connected")
        write("connected @ ${System.currentTimeMillis()}\n")
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        A11yBridge.service = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        A11yBridge.service = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val e = event ?: return
        val pkg = e.packageName?.toString() ?: return
        A11yBridge.lastPackage = pkg
        if (pkg == packageName) return

        val now = System.currentTimeMillis()
        if (now - lastDump < 1500) return
        lastDump = now

        val sb = StringBuilder()
        sb.append("=== pkg=").append(pkg)
            .append(" event=").append(eventTypeName(e.eventType))
            .append(" t=").append(now).append(" ===\n")

        // 关键：不能只用 rootInActiveWindow——它常常指向底部导航那个窗口，
        // 拿不到列表内容。必须遍历所有 window 再取 root，且不要提前 break：
        // BOSS 可能同时有「内容窗口」和「底部悬浮条窗口」两个 type=1。
        var dumped = 0
        val ws = windows
        sb.append("(windows=").append(ws.size).append(")\n")
        for (w in ws) {
            val root = runCatching { w.root }.getOrNull()
            if (root == null) {
                sb.append("--- window type=").append(w.type)
                    .append(" layer=").append(w.layer)
                    .append(" root=NULL ---\n")
            } else {
                sb.append("--- window type=").append(w.type)
                    .append(" layer=").append(w.layer)
                    .append(" ---\n")
                walk(root, 1, sb)
                runCatching { root.recycle() }
                dumped++
            }
            if (dumped >= 8) break
        }
        if (dumped == 0) {
            val root = rootInActiveWindow ?: return
            sb.append("--- fallback rootInActiveWindow ---\n")
            walk(root, 1, sb)
            runCatching { root.recycle() }
        }
        if (sb.length < 300) sb.append("(no text nodes)\n")

        // 决定性测试：直接用 view id / 文本反查节点，绕过树遍历
        runCatching {
            val r = rootInActiveWindow
            if (r != null) {
                val byId = r.findAccessibilityNodeInfosByViewId(
                    "com.hpbr.bosszhipin:id/cl_card_container"
                )
                val byId2 = r.findAccessibilityNodeInfosByViewId(
                    "com.hpbr.bosszhipin:id/view_job_card"
                )
                val byText = r.findAccessibilityNodeInfosByText("元/天")
                val byText2 = r.findAccessibilityNodeInfosByText("HR")
                sb.append(">> PROBE byId(cl_card_container)=").append(byId?.size ?: -1)
                    .append(" byId(view_job_card)=").append(byId2?.size ?: -1)
                    .append(" byText(元/天)=").append(byText?.size ?: -1)
                    .append(" byText(HR)=").append(byText2?.size ?: -1)
                    .append(" rootPkg=").append(r.packageName)
                    .append(" rootChildren=").append(r.childCount)
                    .append("\n")
                // 如果按 id 找得到，把第一张卡的文本打出来
                byId2?.firstOrNull()?.let { card ->
                    val t = StringBuilder()
                    collectText(card, t, 0)
                    sb.append(">> CARD TEXT: ").append(t.take(300)).append("\n")
                }
            } else {
                sb.append(">> PROBE rootInActiveWindow=null\n")
            }
        }

        write(sb.toString())
    }

    private fun collectText(n: AccessibilityNodeInfo?, sb: StringBuilder, depth: Int) {
        if (n == null || depth > 10 || sb.length > 600) return
        n.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let { sb.append(it).append(' ') }
        for (i in 0 until n.childCount) collectText(n.getChild(i), sb, depth + 1)
    }

    private fun walk(n: AccessibilityNodeInfo?, depth: Int, sb: StringBuilder) {
        if (n == null || depth > 16 || sb.length > 120_000) return
        val text = n.text?.toString()?.trim()?.take(40).orEmpty()
        val desc = n.contentDescription?.toString()?.trim()?.take(30).orEmpty()
        if (text.isNotEmpty() || desc.isNotEmpty() || !n.isClickable.not()) {
            val cls = n.className?.toString()?.substringAfterLast('.') ?: "?"
            sb.append("  ".repeat(depth))
                .append(cls)
                .append(" | id=").append(n.viewIdResourceName ?: "-")
                .append(" | click=").append(if (n.isClickable) 1 else 0)
                .append(" | t=").append(text)
                .append(" | d=").append(desc)
                .append("\n")
        }
        for (i in 0 until n.childCount) {
            walk(n.getChild(i), depth + 1, sb)
        }
    }

    private fun write(s: String) {
        runCatching {
            val f = File(getExternalFilesDir(null), "a11y.txt")
            if (f.length() > 400_000) f.writeText("")
            f.appendText(s)
        }
    }

    private fun eventTypeName(t: Int): String = when (t) {
        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> "WIN_STATE"
        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> "WIN_CONTENT"
        AccessibilityEvent.TYPE_VIEW_SCROLLED -> "SCROLL"
        else -> t.toString()
    }

    override fun onInterrupt() {}

    companion object {
        private const val TAG = "JobPilot"
    }
}
