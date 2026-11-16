package com.jobpilot.pad

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import com.jobpilot.pad.PanelState.Phase

/**
 * 悬浮控制面板。两种形态：
 *
 *   配置态（空闲）：档位 / 日上限 / 间隔 / 采集 / 开始 / 全量日志
 *   执行态（批次中）：批次进度、当前岗位、步骤流、三态计数、暂停与终止 —— 对应原型 S08
 *
 * 原则：TYPE_APPLICATION_OVERLAY + FLAG_NOT_FOCUSABLE，浮在 BOSS 上但不抢焦点。
 */
class FloatingPanel(private val activity: Activity) {

    private val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val prefs = activity.getSharedPreferences("jobpilot_panel", Context.MODE_PRIVATE)
    private val main = Handler(Looper.getMainLooper())

    private var root: FrameLayout? = null
    private var lp: WindowManager.LayoutParams? = null

    private lateinit var configView: LinearLayout
    private lateinit var execView: LinearLayout
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

    // ---- 执行态控件
    private lateinit var tvExecTitle: TextView
    private lateinit var tvExecDot: TextView
    private lateinit var tvBatchChip: TextView
    private lateinit var tvDoneBig: TextView
    private lateinit var tvTotalBig: TextView
    private lateinit var tvTierGap: TextView
    private lateinit var tvEta: TextView
    private lateinit var execBar: ProgressBar
    private lateinit var tvCurCompany: TextView
    private lateinit var tvCurTitle: TextView
    private lateinit var tvCurIndex: TextView
    private lateinit var tvCurAction: TextView
    private lateinit var curBar: ProgressBar
    private lateinit var tvElapsed: TextView
    private lateinit var stepsBox: LinearLayout
    private lateinit var tvExecLog: TextView
    private lateinit var btnPause: TextView
    private lateinit var btnAbort: TextView
    private lateinit var tvFooter: TextView
    private lateinit var tvBreaker: TextView
    private lateinit var execCard: LinearLayout

    var isShowing = false
        private set

    /** 「采集」：读当前前台 App（BOSS） */
    var onCollect: (() -> Unit)? = null

    /** 「AI 扫列表」：在 BOSS 职位列表页逐个点进详情读 JD 打分 */
    var onScanList: (() -> Unit)? = null

    /** 「开始」：由 MainActivity 真正起批次 */
    var onStart: (() -> Unit)? = null

    /** 「终止批次」：由 MainActivity 停执行器 */
    var onAbort: (() -> Unit)? = null

    private val logLines = ArrayList<String>()
    private val logSink: (String) -> Unit = { line ->
        main.post {
            logLines.add(line)
            while (logLines.size > 60) logLines.removeAt(0)
            if (::tvLog.isInitialized) {
                tvLog.text = logLines.joinToString("\n")
                scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
            }
            if (::tvExecLog.isInitialized) {
                tvExecLog.text = logLines.takeLast(3).joinToString("\n")
            }
        }
    }
    private val uiSink: () -> Unit = { main.post { updateUi() } }

    private val ticker = object : Runnable {
        override fun run() {
            if (!isShowing) return
            if (PanelState.phase != Phase.IDLE) updateExec()
            main.postDelayed(this, 1000)
        }
    }

    // ---------------------------------------------------------------- 生命周期

    fun show(): Boolean {
        if (isShowing) return true
        if (!Settings.canDrawOverlays(activity)) return false
        build()
        runCatching { wm.addView(root, lp) }.onFailure { return false }
        isShowing = true
        prefs.edit().putBoolean("enabled", true).apply()
        PanelState.subscribe(logSink)
        PanelState.subscribeUi(uiSink)
        PanelState.log("面板已启动 · 拖标题移动，右下角 ◢ 缩放")
        updateUi()
        main.postDelayed(ticker, 1000)
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
        main.removeCallbacks(ticker)
        PanelState.unsubscribe(logSink)
        PanelState.unsubscribeUi(uiSink)
        root?.let { runCatching { wm.removeView(it) } }
        root = null
        isShowing = false
    }

    // ---------------------------------------------------------------- 构建

    private fun build() {
        configView = buildConfig()
        execView = buildExec()
        bubble = buildBubble()
        val collapsed = prefs.getBoolean("collapsed", false)

        val container = FrameLayout(activity)
        val fill = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        container.addView(configView, fill)
        container.addView(execView, fill)
        container.addView(bubble, bubbleParams())
        resizeHandle = buildResizeHandle()
        container.addView(resizeHandle, resizeParams())
        resizeHandle.visibility = if (collapsed) View.GONE else View.VISIBLE

        bubble.visibility = if (collapsed) View.VISIBLE else View.GONE
        if (collapsed) {
            // 折叠时两个展开视图都必须隐藏，否则会在小球后面抢触摸
            configView.visibility = View.GONE
            execView.visibility = View.GONE
        }

        root = container
        val w = if (collapsed) dp(64) else prefs.getInt("w", dp(372)).coerceAtLeast(dp(280))
        val h = if (collapsed) dp(64) else prefs.getInt("h", dp(560)).coerceAtLeast(dp(420))
        lp = WindowManager.LayoutParams(
            w, h,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = prefs.getInt("x", dp(24))
            y = prefs.getInt("y", dp(120))
            alpha = prefs.getFloat("alpha", 0.92f).coerceIn(0.4f, 1f)
            clampInto(this)
        }
        if (!collapsed) refreshMode()
    }

    // ---------------------------------------------------------------- 配置态

    private fun buildConfig(): LinearLayout {
        val panel = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(BG, dp(16))
            setPadding(dp(14), dp(10), dp(14), dp(12))
        }

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
            setTextColor(SUB)
            textSize = 11f
            setPadding(dp(10), 0, 0, 0)
        }
        header.addView(title)
        header.addView(tvStatus, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        header.addView(iconButton("—") { toggleCollapse() })
        header.addView(iconButton("✕") { hide() })
        attachDrag(header)
        panel.addView(header)

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
                    PanelState.persist(activity)
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

        panel.addView(sectionLabel("每日上限"))
        tvLimitVal = TextView(activity).apply {
            setTextColor(TEAL); textSize = 12f
        }
        panel.addView(tvLimitVal)
        panel.addView(
            slider(
                5, 40, PanelState.dailyLimit,
                onChange = { v ->
                    PanelState.dailyLimit = v
                    tvLimitVal.text = "$v 条"
                    PanelState.log("每日上限设为 $v 条")
                },
                onFinished = { PanelState.persist(activity) }
            )
        )

        panel.addView(sectionLabel("条间隔"))
        tvGapVal = TextView(activity).apply {
            setTextColor(TEAL); textSize = 12f
        }
        panel.addView(tvGapVal)
        panel.addView(
            slider(
                20, 120, PanelState.gapMin,
                onChange = { v ->
                    PanelState.gapMin = v
                    PanelState.gapMax = minOf(240, v * 2)
                    tvGapVal.text = "${PanelState.gapMin}–${PanelState.gapMax} 秒"
                    PanelState.log("条间隔设为 ${PanelState.gapMin}–${PanelState.gapMax} 秒")
                },
                onFinished = { PanelState.persist(activity) }
            )
        )

        val ctrl = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(10), 0, dp(8))
        }
        val btnCollect = actionButton("采集", LINE) { onCollect?.invoke() }
        val btnScan = actionButton("AI 扫列表", LINE) { onScanList?.invoke() }
        tvRun = actionButton("▶  开始", TEAL, 0xFF062A28.toInt()) {
            PanelState.log("请求开始批次…")
            onStart?.invoke()
        }
        val btnStop = actionButton("■  停止", 0xFF4A2A2A.toInt()) {
            onAbort?.invoke()
            PanelState.resetToIdle()
            updateUi()
        }
        listOf(btnCollect, btnScan, tvRun, btnStop).forEachIndexed { i, b ->
            ctrl.addView(b, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                if (i > 0) leftMargin = dp(6)
            })
        }
        panel.addView(ctrl)

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
        panel.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        return panel
    }

    // ---------------------------------------------------------------- 执行态（原型 S08）

    private fun buildExec(): LinearLayout {
        val panel = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(BG, dp(18), LINE)
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }

        // ---- 标题行：状态点 + 标题 + 批次号 + 折叠/关闭
        val header = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        tvExecDot = TextView(activity).apply {
            text = "●"; setTextColor(TEAL); textSize = 11f
        }
        tvExecTitle = TextView(activity).apply {
            text = "JobPilot 执行中"
            setTextColor(Color.WHITE); textSize = 13.5f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(8), 0, 0, 0)
        }
        tvBatchChip = TextView(activity).apply {
            textSize = 10f; setTextColor(FAINT)
            background = rounded(CARD, dp(5), BORDER)
            setPadding(dp(6), dp(2), dp(6), dp(2))
        }
        header.addView(tvExecDot)
        header.addView(tvExecTitle)
        header.addView(tvBatchChip, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { leftMargin = dp(8) })
        val spacer = View(activity)
        header.addView(spacer, LinearLayout.LayoutParams(0, 1, 1f))
        header.addView(iconButton("—") { toggleCollapse() })
        header.addView(iconButton("✕") { hide() })
        attachDrag(header)
        panel.addView(header)

        // ---- 大进度：6 / 8 + 档位间隔 + 预计完成
        val progRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.BOTTOM
            setPadding(0, dp(12), 0, 0)
        }
        tvDoneBig = TextView(activity).apply {
            text = "0"; setTextColor(Color.WHITE); textSize = 38f
            setTypeface(typeface, Typeface.BOLD)
        }
        tvTotalBig = TextView(activity).apply {
            text = "/ 0"; setTextColor(0xFF4A617C.toInt()); textSize = 18f
            setPadding(dp(6), 0, 0, dp(4))
        }
        tvTierGap = TextView(activity).apply {
            textSize = 11f; setTextColor(SUB)
            setPadding(dp(8), 0, 0, dp(6))
        }
        tvEta = TextView(activity).apply {
            textSize = 11f; setTextColor(TEAL)
            setPadding(0, 0, 0, dp(6))
        }
        progRow.addView(tvDoneBig)
        progRow.addView(tvTotalBig)
        progRow.addView(tvTierGap)
        progRow.addView(View(activity), LinearLayout.LayoutParams(0, 1, 1f))
        progRow.addView(tvEta)
        panel.addView(progRow)

        execBar = hBar(TEAL, 0xFF18283A.toInt())
        panel.addView(execBar, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(5)
        ).apply { topMargin = dp(10) })

        // ---- 当前岗位卡片
        execCard = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(CARD, dp(12), BORDER)
            setPadding(dp(12), dp(10), dp(12), dp(10))
        }
        val curTop = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        tvCurCompany = TextView(activity).apply {
            textSize = 12.5f; setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            maxLines = 1
            maxEms = 7
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        tvCurTitle = TextView(activity).apply {
            textSize = 10.5f; setTextColor(FAINT)
            setPadding(dp(8), 0, 0, 0)
            maxLines = 1
        }
        tvCurIndex = TextView(activity).apply {
            textSize = 10f; setTextColor(TEAL)
            background = rounded(0x1F22E2D8, dp(5))
            setPadding(dp(6), dp(2), dp(6), dp(2))
        }
        curTop.addView(tvCurCompany)
        curTop.addView(tvCurTitle, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        curTop.addView(tvCurIndex)
        execCard.addView(curTop)
        tvCurAction = TextView(activity).apply {
            textSize = 11.5f; setTextColor(0xFFA9BACD.toInt())
            setPadding(0, dp(8), 0, 0)
        }
        execCard.addView(tvCurAction)
        val curBarRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(10), 0, 0)
        }
        curBar = hBar(TEAL, LINE)
        curBarRow.addView(curBar, LinearLayout.LayoutParams(0, dp(3), 1f))
        tvElapsed = TextView(activity).apply {
            textSize = 11f; setTextColor(TEAL); typeface = Typeface.MONOSPACE
            setPadding(dp(8), 0, 0, 0)
        }
        curBarRow.addView(tvElapsed)
        execCard.addView(curBarRow)
        panel.addView(execCard, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(12) })

        // ---- 熔断提示（平时隐藏）
        tvBreaker = TextView(activity).apply {
            textSize = 11f; setTextColor(DANGER)
            background = rounded(0x33FF8A8A, dp(8), 0xFF4A2A2A.toInt())
            setPadding(dp(10), dp(8), dp(10), dp(8))
            visibility = View.GONE
        }
        panel.addView(tvBreaker, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(10) })

        // ---- 步骤流
        stepsBox = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(12), 0, 0)
        }
        panel.addView(stepsBox)

        // ---- 调试日志（最近 3 行）
        tvExecLog = TextView(activity).apply {
            textSize = 10f; setTextColor(FAINT); typeface = Typeface.MONOSPACE
            setPadding(0, dp(10), 0, 0)
        }
        panel.addView(tvExecLog)

        // ---- 暂停 / 终止
        val btns = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(12), 0, 0)
        }
        btnPause = actionButton("暂停", LINE) {
            if (PanelState.phase == Phase.DONE || PanelState.phase == Phase.BREAKER) {
                PanelState.resetToIdle()
            } else {
                PanelState.togglePause()
            }
            updateUi()
        }
        btnAbort = actionButton("终止批次", 0xFF4A2A2A.toInt(), DANGER) {
            PanelState.log("请求终止批次…")
            PanelState.curAction = "正在终止，当前步骤走完后停下…"
            onAbort?.invoke()
            updateUi()
        }
        btns.addView(btnPause, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        btns.addView(btnAbort, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
            leftMargin = dp(8)
        })
        panel.addView(btns)

        // ---- 底部三态计数
        tvFooter = TextView(activity).apply {
            textSize = 10.5f; setTextColor(FAINT)
            setPadding(0, dp(10), 0, 0)
        }
        panel.addView(tvFooter)
        return panel
    }

    private fun buildBubble(): LinearLayout {
        val b = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = rounded(BG, dp(32), LINE)
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
        var downX = 0f; var downY = 0f; var startW = 0; var startH = 0
        h.setOnTouchListener { _, e ->
            val p = lp ?: return@setOnTouchListener false
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX; downY = e.rawY
                    startW = if (p.width > 0) p.width else dp(372)
                    startH = if (p.height > 0) p.height else dp(560)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    p.width = (startW + (e.rawX - downX)).toInt().coerceIn(dp(280), dp(760))
                    p.height = (startH + (e.rawY - downY)).toInt().coerceIn(dp(320), dp(1200))
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

    // ---------------------------------------------------------------- 位置约束

    private val screenW: Int get() = activity.resources.displayMetrics.widthPixels
    private val screenH: Int get() = activity.resources.displayMetrics.heightPixels

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

    // ---------------------------------------------------------------- 交互

    private fun attachDrag(v: View, onClick: (() -> Unit)? = null) {
        var downX = 0f; var downY = 0f; var startX = 0; var startY = 0; var moved = false
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
        val toCollapsed = configView.visibility == View.VISIBLE || execView.visibility == View.VISIBLE
        configView.visibility = View.GONE
        execView.visibility = View.GONE
        bubble.visibility = if (toCollapsed) View.VISIBLE else View.GONE
        resizeHandle.visibility = if (toCollapsed) View.GONE else View.VISIBLE
        if (toCollapsed) {
            p.width = dp(64); p.height = dp(64)
        } else {
            p.width = prefs.getInt("w", dp(372)).coerceAtLeast(dp(280))
            p.height = prefs.getInt("h", dp(560)).coerceAtLeast(dp(420))
            clampInto(p)
            refreshMode()
        }
        clampInto(p)
        root?.let { wm.updateViewLayout(it, p) }
        prefs.edit().putBoolean("collapsed", toCollapsed).apply()
        if (!toCollapsed) updateUi()
    }

    private fun saveGeometry() {
        val p = lp ?: return
        val e = prefs.edit().putInt("x", p.x).putInt("y", p.y)
        // 折叠态的 64dp 不能写回 w/h，否则下次展开只剩一个小盒子
        if (bubble.visibility != View.VISIBLE) {
            e.putInt("w", p.width).putInt("h", p.height)
        }
        e.apply()
    }

    // ---------------------------------------------------------------- 刷新

    private fun refreshMode() {
        if (!::configView.isInitialized) return
        val idle = PanelState.phase == Phase.IDLE
        configView.visibility = if (idle) View.VISIBLE else View.GONE
        execView.visibility = if (idle) View.GONE else View.VISIBLE
        // 执行态内容比配置态高，避免挤在一起
        if (!idle) {
            val p = lp ?: return
            if (p.height in 1 until dp(520)) {
                p.height = dp(560)
                clampInto(p)
                root?.let { r -> runCatching { wm.updateViewLayout(r, p) } }
            }
        }
    }

    fun updateUi() {
        if (!isShowing) return
        if (bubble.visibility == View.VISIBLE) { updateBubble(); return }
        refreshMode()
        if (PanelState.phase == Phase.IDLE) updateConfig() else updateExec()
        updateBubble()
    }

    private fun updateConfig() {
        tvStatus.text = "待命 · 今日 ${PanelState.todayCount}/${PanelState.dailyLimit}"
        tvStatus.setTextColor(SUB)
        tvRun.text = "▶  开始"
        tvLimitVal.text = "${PanelState.dailyLimit} 条"
        tvGapVal.text = "${PanelState.gapMin}–${PanelState.gapMax} 秒"
        chips.forEachIndexed { i, c ->
            val on = i == PanelState.tier
            c.setTextColor(if (on) 0xFF062A28.toInt() else SUB)
            c.background = rounded(if (on) TEAL else 0x33FFFFFF, dp(8))
        }
    }

    private fun updateExec() {
        if (!::tvExecTitle.isInitialized) return
        val s = PanelState
        val phase = s.phase

        tvExecTitle.text = when (phase) {
            Phase.RUNNING -> "JobPilot 执行中"
            Phase.PAUSED -> "JobPilot 已暂停"
            Phase.BREAKER -> "JobPilot 已熔断"
            Phase.DONE -> "JobPilot 已完成"
            Phase.IDLE -> "JobPilot"
        }
        val accent = if (phase == Phase.BREAKER) DANGER else TEAL
        tvExecDot.setTextColor(accent)
        tvEta.setTextColor(accent)
        tvBatchChip.text = s.batchId.ifBlank { "未开始" }

        tvDoneBig.text = s.batchDone.toString()
        tvTotalBig.text = "/ ${s.batchTotal}"
        tvTierGap.text = "${PanelState.tierNames[s.tier]}档 · 每家 ${s.gapMin}–${s.gapMax}s"
        tvEta.text = when {
            phase == Phase.DONE -> "本批结束"
            phase == Phase.BREAKER -> "已停止"
            phase == Phase.PAUSED -> "已暂停"
            s.waitSec > 0 -> "冷却 ${s.waitSec}s 后下一家"
            s.etaSec > 0 -> "预计 ${fmt(s.etaSec)} 完成"
            else -> ""
        }
        execBar.progress = if (s.batchTotal > 0) s.batchDone * 100 / s.batchTotal else 0
        execBar.progressTintList = ColorStateList.valueOf(accent)

        // 当前岗位
        val idx = if (s.batchDone < s.batchTotal) s.batchDone + 1 else s.batchDone
        tvCurIndex.text = "第 $idx / ${s.batchTotal} 家"
        tvCurCompany.text = s.curCompany.ifBlank { "—" }
        tvCurTitle.text = s.curTitle
        tvCurAction.text = when {
            phase == Phase.DONE -> "本批结束：L3 送达 ${s.l3} · 失败 ${s.fail} · 重复 ${s.dup}"
            phase == Phase.BREAKER -> "已熔断，停止一切点击"
            s.waitSec > 0 -> "等待间隔，避免触发风控（剩 ${s.waitSec}s）"
            else -> s.curAction.ifBlank { "准备中…" }
        }
        val elapsed = if (s.curStartedAt > 0)
            ((System.currentTimeMillis() - s.curStartedAt) / 1000).toInt() else 0
        tvElapsed.text = if (phase == Phase.DONE || phase == Phase.BREAKER) "—" else fmt(elapsed.toLong())
        curBar.progress = when {
            phase == Phase.DONE -> 100
            elapsed <= 0 -> 0
            else -> (elapsed * 100 / 90).coerceIn(5, 95)
        }
        curBar.progressTintList = ColorStateList.valueOf(accent)

        // 熔断条
        if (phase == Phase.BREAKER) {
            tvBreaker.visibility = View.VISIBLE
            tvBreaker.text = "熔断原因：${s.breakerReason}\n已停止一切点击，不会继续发送。排查后点「返回」。"
            execCard.background = rounded(CARD, dp(12), 0xFF4A2A2A.toInt())
        } else {
            tvBreaker.visibility = View.GONE
            execCard.background = rounded(CARD, dp(12), BORDER)
        }

        // 步骤流
        stepsBox.removeAllViews()
        val steps = s.steps
        steps.forEach { st ->
            val row = TextView(activity).apply {
                textSize = 11.5f
                setPadding(0, dp(3), 0, dp(3))
                val mark = when (st.ok) {
                    true -> "✓ "
                    false -> "✕ "
                    null -> "◌ "
                }
                text = mark + st.name + if (st.detail.isBlank()) "" else "  ·  ${st.detail.take(28)}"
                setTextColor(
                    when (st.ok) {
                        true -> SUB
                        false -> DANGER
                        null -> Color.WHITE
                    }
                )
            }
            stepsBox.addView(row)
        }
        if (steps.isEmpty()) {
            stepsBox.addView(TextView(activity).apply {
                text = "等待执行器上报步骤…"
                textSize = 11.5f; setTextColor(FAINT)
            })
        }

        btnPause.text = when (phase) {
            Phase.DONE, Phase.BREAKER -> "返回"
            Phase.PAUSED -> "继续"
            else -> "暂停"
        }
        btnAbort.isEnabled = phase == Phase.RUNNING || phase == Phase.PAUSED
        btnAbort.alpha = if (btnAbort.isEnabled) 1f else 0.4f

        tvFooter.text = "L3 已核验 ${s.l3}   失败 ${s.fail}   重复 ${s.dup}     熔断阈值 ${PanelState.BREAKER_THRESHOLD} 次"
    }

    private fun updateBubble() {
        if (!::tvBubble.isInitialized) return
        val s = PanelState
        if (s.phase == Phase.IDLE) {
            tvBubble.text = "JP\n${s.todayCount}"
            tvBubble.textSize = 11f
            bubble.background = rounded(BG, dp(32), LINE)
        } else {
            tvBubble.text = "${s.batchDone}/${s.batchTotal}"
            tvBubble.textSize = 15f
            val stroke = if (s.phase == Phase.BREAKER) DANGER else TEAL
            bubble.background = rounded(BG, dp(32), stroke)
            tvBubble.setTextColor(if (s.phase == Phase.BREAKER) DANGER else Color.WHITE)
        }
    }

    // ---------------------------------------------------------------- 小工具

    private fun fmt(sec: Long): String = String.format("%02d:%02d", sec / 60, sec % 60)

    private fun dp(v: Int): Int = (v * activity.resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, radius: Int, stroke: Int? = null): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
            if (stroke != null) setStroke(dp(1), stroke)
        }

    private fun hBar(fg: Int, bg: Int): ProgressBar =
        ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progressTintList = ColorStateList.valueOf(fg)
            progressBackgroundTintList = ColorStateList.valueOf(bg)
        }

    private fun sectionLabel(t: String) = TextView(activity).apply {
        text = t
        setTextColor(0xFF64748B.toInt())
        textSize = 10.5f
        setPadding(0, dp(8), 0, dp(2))
    }

    private fun iconButton(t: String, onClick: () -> Unit) = TextView(activity).apply {
        text = t
        setTextColor(SUB)
        textSize = 14f
        gravity = Gravity.CENTER
        setPadding(dp(10), dp(4), dp(4), dp(4))
        setOnClickListener { onClick() }
    }

    private fun actionButton(t: String, bg: Int, fg: Int = Color.WHITE, onClick: () -> Unit) =
        TextView(activity).apply {
            text = t
            setTextColor(fg)
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, dp(10))
            background = rounded(bg, dp(10))
            setOnClickListener { onClick() }
        }

    private fun slider(
        min: Int,
        max: Int,
        current: Int,
        onChange: (Int) -> Unit,
        onFinished: (() -> Unit)? = null,
    ): SeekBar =
        SeekBar(activity).apply {
            this.max = max - min
            progress = (current - min).coerceIn(0, max - min)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                    if (fromUser) onChange(min + value)
                }

                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) { onFinished?.invoke() }
            })
        }

    companion object {
        private val BG = 0xFF0B1522.toInt()
        private val CARD = 0xFF111F31.toInt()
        private val BORDER = 0xFF22334A.toInt()
        private val LINE = 0xFF1B2A3D.toInt()
        private val TEAL = 0xFF22E2D8.toInt()
        private val SUB = 0xFF8FA3BC.toInt()
        private val FAINT = 0xFF63788F.toInt()
        private val DANGER = 0xFFFF8A8A.toInt()
    }
}
