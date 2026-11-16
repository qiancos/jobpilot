package com.jobpilot.pad

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView

/**
 * 悬浮控制面板。
 *
 * 能做：拖动、右下角缩放、折叠成小球、调档位/上限/间隔、看日志。
 * 原则：TYPE_APPLICATION_OVERLAY + FLAG_NOT_FOCUSABLE —— 浮在页面上但不抢焦点，
 *       WebView 仍能正常滚动和输入。
 */
class FloatingPanel(private val activity: Activity) {

    private val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val prefs = activity.getSharedPreferences("jobpilot_panel", Context.MODE_PRIVATE)

    private var root: FrameLayout? = null
    private var lp: WindowManager.LayoutParams? = null

    private lateinit var expanded: LinearLayout
    private lateinit var bubble: LinearLayout
    private lateinit var tvStatus: TextView
    private lateinit var tvBubble: TextView
    private lateinit var tvLog: TextView
    private lateinit var scroll: ScrollView
    private lateinit var tvRun: TextView
    private lateinit var tvLimitVal: TextView
    private lateinit var tvGapVal: TextView
    private lateinit var resizeHandle: TextView
    private val chips = ArrayList<TextView>()

    var isShowing = false
        private set

    /** 由 MainActivity 注入：面板上的「采集」要读当前前台 App（BOSS） */
    var onCollect: (() -> Unit)? = null

    private val logSink: (String) -> Unit = { appendLog(it) }

    // ---------------------------------------------------------------- 生命周期

    fun show(): Boolean {
        if (isShowing) return true
        if (!Settings.canDrawOverlays(activity)) return false
        build()
        runCatching { wm.addView(root, lp) }.onFailure { return false }
        isShowing = true
        prefs.edit().putBoolean("enabled", true).apply()
        PanelState.subscribe(logSink)
        PanelState.log("面板已启动 · 拖标题移动，右下角 ◢ 缩放")
        updateUi()
        return true
    }

    /** 用户主动关闭：记住"别再自动弹出来" */
    fun hide() {
        prefs.edit().putBoolean("enabled", false).apply()
        detach()
    }

    /** 页面销毁：只摘掉视图，不动用户偏好 */
    fun destroy() = detach()

    private fun detach() {
        if (!isShowing) return
        PanelState.unsubscribe(logSink)
        root?.let { runCatching { wm.removeView(it) } }
        root = null
        isShowing = false
    }

    // ---------------------------------------------------------------- 构建

    private fun build() {
        expanded = buildExpanded()
        bubble = buildBubble()
        val collapsed = prefs.getBoolean("collapsed", false)

        val container = FrameLayout(activity)
        container.addView(
            expanded,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        container.addView(bubble, bubbleParams())
        resizeHandle = buildResizeHandle()
        container.addView(resizeHandle, resizeParams())
        resizeHandle.visibility = if (collapsed) View.GONE else View.VISIBLE

        expanded.visibility = if (collapsed) View.GONE else View.VISIBLE
        bubble.visibility = if (collapsed) View.VISIBLE else View.GONE

        root = container
        val w = prefs.getInt("w", dp(if (collapsed) 64 else 360))
        val h = prefs.getInt("h", dp(if (collapsed) 64 else 460))
        lp = WindowManager.LayoutParams(
            w, h,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = prefs.getInt("x", dp(24))
            y = prefs.getInt("y", dp(120))
            clampInto(this)
        }
    }

    // ---------------------------------------------------------------- 位置约束

    private val screenW: Int get() = activity.resources.displayMetrics.widthPixels
    private val screenH: Int get() = activity.resources.displayMetrics.heightPixels

    /** 状态栏高度：不让面板被拖进去 */
    private val topInset: Int by lazy {
        val id = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (id > 0) activity.resources.getDimensionPixelSize(id) else dp(24)
    }

    private fun clampInto(p: WindowManager.LayoutParams) {
        if (p.width <= 0 || p.height <= 0) return
        val maxX = (screenW - p.width).coerceAtLeast(0)
        val maxY = (screenH - p.height).coerceAtLeast(topInset)
        p.x = p.x.coerceIn(0, maxX)
        p.y = p.y.coerceIn(topInset, maxY)
    }

    private fun buildExpanded(): LinearLayout {
        val panel = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(0xFF0F172A.toInt(), dp(16))
            setPadding(dp(14), dp(10), dp(14), dp(12))
        }

        // ---- 标题栏（拖动区）
        val header = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val title = TextView(activity).apply {
            text = "⠿  JobPilot"
            setTextColor(Color.WHITE)
            textSize = 14f
            setTypeface(typeface, Typeface.BOLD)
        }
        tvStatus = TextView(activity).apply {
            text = "待命"
            setTextColor(0xFF94A3B8.toInt())
            textSize = 11f
            setPadding(dp(10), 0, 0, 0)
        }
        val btnCollapse = iconButton("—") { toggleCollapse() }
        val btnClose = iconButton("✕") { hide() }

        header.addView(title)
        header.addView(tvStatus, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        header.addView(btnCollapse)
        header.addView(btnClose)
        attachDrag(header)
        panel.addView(header)

        // ---- 档位
        panel.addView(sectionLabel("投递档位"))
        val tierRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(2), 0, dp(6))
        }
        chips.clear()
        PanelState.tierNames.forEachIndexed { i, name ->
            val chip = TextView(activity).apply {
                text = name
                textSize = 12f
                gravity = Gravity.CENTER
                setPadding(dp(4), dp(8), dp(4), dp(8))
                setOnClickListener {
                    PanelState.applyTier(i)
                    PanelState.log("档位切到「$name」，日上限 ${PanelState.dailyLimit}")
                    updateUi()
                }
            }
            val lpp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            if (i > 0) lpp.leftMargin = dp(6)
            tierRow.addView(chip, lpp)
            chips.add(chip)
        }
        panel.addView(tierRow)

        // ---- 每日上限
        panel.addView(sectionLabel("每日上限"))
        tvLimitVal = TextView(activity).apply {
            text = "15 条"
            setTextColor(0xFF22C55E.toInt())
            textSize = 12f
        }
        panel.addView(tvLimitVal)
        panel.addView(slider(5, 40, PanelState.dailyLimit) { v ->
            PanelState.dailyLimit = v
            tvLimitVal.text = "$v 条"
            PanelState.log("每日上限设为 $v 条")
        })

        // ---- 条间隔
        panel.addView(sectionLabel("条间隔"))
        tvGapVal = TextView(activity).apply {
            text = "45–90 秒"
            setTextColor(0xFF22C55E.toInt())
            textSize = 12f
        }
        panel.addView(tvGapVal)
        panel.addView(slider(20, 120, PanelState.gapMin) { v ->
            PanelState.gapMin = v
            PanelState.gapMax = minOf(240, v * 2)
            tvGapVal.text = "${PanelState.gapMin}–${PanelState.gapMax} 秒"
            PanelState.log("条间隔设为 ${PanelState.gapMin}–${PanelState.gapMax} 秒")
        })

        // ---- 运行控制
        val ctrl = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(10), 0, dp(8))
        }
        val btnCollect = actionButton("采集") { onCollect?.invoke() }
        tvRun = actionButton("▶  开始") {
            PanelState.running = !PanelState.running
            PanelState.log(if (PanelState.running) "开始执行（尚未接入执行器）" else "已暂停")
            updateUi()
        }
        val btnStop = actionButton("■  停止") {
            PanelState.running = false
            PanelState.batchDone = 0
            PanelState.batchTotal = 0
            PanelState.log("已停止，批次数清零")
            updateUi()
        }
        ctrl.addView(btnCollect, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        ctrl.addView(
            tvRun,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                leftMargin = dp(6)
            }
        )
        ctrl.addView(
            btnStop,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                leftMargin = dp(6)
            }
        )
        panel.addView(ctrl)

        // ---- 日志
        panel.addView(sectionLabel("运行日志"))
        tvLog = TextView(activity).apply {
            setTextColor(0xFFCBD5E1.toInt())
            textSize = 10.5f
            typeface = Typeface.MONOSPACE
            setPadding(0, dp(2), 0, 0)
        }
        scroll = ScrollView(activity).apply {
            addView(tvLog)
            background = rounded(0x33000000, dp(8))
            setPadding(dp(8), dp(6), dp(8), dp(6))
        }
        panel.addView(
            scroll,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        )

        return panel
    }

    private fun buildBubble(): LinearLayout {
        val b = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = rounded(0xFF0F172A.toInt(), dp(32))
        }
        tvBubble = TextView(activity).apply {
            text = "JP"
            setTextColor(Color.WHITE)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        b.addView(tvBubble)
        attachDrag(b) { toggleCollapse() }
        return b
    }

    private fun bubbleParams() = FrameLayout.LayoutParams(dp(64), dp(64)).apply {
        gravity = Gravity.CENTER
    }

    private fun buildResizeHandle(): TextView {
        val h = TextView(activity).apply {
            text = "◢"
            setTextColor(0xFFE2E8F0.toInt())
            textSize = 13f
            gravity = Gravity.CENTER
            background = rounded(0x55000000, dp(10))
        }
        var downX = 0f
        var downY = 0f
        var startW = 0
        var startH = 0
        h.setOnTouchListener { _, e ->
            val p = lp ?: return@setOnTouchListener false
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX; downY = e.rawY
                    startW = if (p.width > 0) p.width else dp(360)
                    startH = if (p.height > 0) p.height else dp(460)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    p.width = (startW + (e.rawX - downX)).toInt().coerceIn(dp(260), dp(760))
                    p.height = (startH + (e.rawY - downY)).toInt().coerceIn(dp(220), dp(1000))
                    clampInto(p)
                    root?.let { wm.updateViewLayout(it, p) }
                    true
                }
                MotionEvent.ACTION_UP -> { saveGeometry(); true }
                else -> false
            }
        }
        return h
    }

    private fun resizeParams() = FrameLayout.LayoutParams(dp(34), dp(34)).apply {
        gravity = Gravity.BOTTOM or Gravity.END
        setMargins(0, 0, dp(6), dp(6))
    }

    // ---------------------------------------------------------------- 交互

    /**
     * 同一个手势里区分"点"和"拖"：
     * 位移小于 touchSlop 就当点击，否则当拖动。
     * 不做这个区分的话，setOnClickListener 会被 OnTouchListener 吃掉（小球点了没反应）。
     */
    private fun attachDrag(v: View, onClick: (() -> Unit)? = null) {
        var downX = 0f
        var downY = 0f
        var startX = 0
        var startY = 0
        var moved = false
        val slop = dp(6)
        v.setOnTouchListener { _, e ->
            val p = lp ?: return@setOnTouchListener false
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX; downY = e.rawY
                    startX = p.x; startY = p.y
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = e.rawX - downX
                    val dy = e.rawY - downY
                    if (kotlin.math.abs(dx) > slop || kotlin.math.abs(dy) > slop) moved = true
                    if (moved) {
                        p.x = startX + dx.toInt()
                        p.y = startY + dy.toInt()
                        clampInto(p)
                        root?.let { wm.updateViewLayout(it, p) }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    saveGeometry()
                    if (!moved) onClick?.invoke()
                    true
                }
                else -> false
            }
        }
    }

    private fun toggleCollapse() {
        val p = lp ?: return
        val toCollapsed = expanded.visibility == View.VISIBLE
        expanded.visibility = if (toCollapsed) View.GONE else View.VISIBLE
        bubble.visibility = if (toCollapsed) View.VISIBLE else View.GONE
        resizeHandle.visibility = if (toCollapsed) View.GONE else View.VISIBLE
        if (toCollapsed) {
            p.width = dp(64); p.height = dp(64)
        } else {
            p.width = prefs.getInt("w", dp(360))
            p.height = prefs.getInt("h", dp(460))
        }
        clampInto(p)
        root?.let { wm.updateViewLayout(it, p) }
        prefs.edit().putBoolean("collapsed", toCollapsed).apply()
    }

    private fun saveGeometry() {
        val p = lp ?: return
        prefs.edit()
            .putInt("x", p.x).putInt("y", p.y)
            .putInt("w", p.width).putInt("h", p.height)
            .apply()
    }

    // ---------------------------------------------------------------- 刷新

    fun updateUi() {
        if (!isShowing) return
        tvStatus.text = if (PanelState.running) "运行中" else "待命"
        tvStatus.setTextColor(if (PanelState.running) 0xFF22C55E.toInt() else 0xFF94A3B8.toInt())
        tvRun.text = if (PanelState.running) "⏸  暂停" else "▶  开始"
        tvRun.background = rounded(
            if (PanelState.running) 0xFFF59E0B.toInt() else 0xFF22C55E.toInt(), dp(10)
        )
        tvLimitVal.text = "${PanelState.dailyLimit} 条"
        tvGapVal.text = "${PanelState.gapMin}–${PanelState.gapMax} 秒"
        chips.forEachIndexed { i, c ->
            val on = i == PanelState.tier
            c.setTextColor(if (on) Color.WHITE else 0xFF94A3B8.toInt())
            c.background = rounded(
                if (on) 0xFF22C55E.toInt() else 0x33FFFFFF, dp(8)
            )
        }
        if (::tvBubble.isInitialized) {
            tvBubble.text = "JP\n${PanelState.todayCount}"
            tvBubble.textSize = 11f
        }
    }

    private fun appendLog(line: String) {
        if (!::tvLog.isInitialized) return
        val cur = tvLog.text?.toString().orEmpty()
        val merged = if (cur.isEmpty()) line else "$cur\n$line"
        val trimmed = merged.lines().takeLast(60).joinToString("\n")
        tvLog.text = trimmed
        scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
    }

    // ---------------------------------------------------------------- 小工具

    private fun dp(v: Int): Int =
        (v * activity.resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, radius: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }

    private fun sectionLabel(t: String) = TextView(activity).apply {
        text = t
        setTextColor(0xFF64748B.toInt())
        textSize = 10.5f
        setPadding(0, dp(8), 0, dp(2))
    }

    private fun iconButton(t: String, onClick: () -> Unit) = TextView(activity).apply {
        text = t
        setTextColor(0xFF94A3B8.toInt())
        textSize = 14f
        gravity = Gravity.CENTER
        setPadding(dp(10), dp(4), dp(4), dp(4))
        setOnClickListener { onClick() }
    }

    private fun actionButton(t: String, onClick: () -> Unit) = TextView(activity).apply {
        text = t
        setTextColor(Color.WHITE)
        textSize = 13f
        gravity = Gravity.CENTER
        setPadding(0, dp(10), 0, dp(10))
        background = rounded(0xFF22C55E.toInt(), dp(10))
        setOnClickListener { onClick() }
    }

    private fun slider(min: Int, max: Int, current: Int, onChange: (Int) -> Unit): SeekBar =
        SeekBar(activity).apply {
            this.max = max - min
            progress = (current - min).coerceIn(0, max - min)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                    if (fromUser) onChange(min + value)
                }

                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }
}
