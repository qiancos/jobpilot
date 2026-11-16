package com.jobpilot.pad

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 面板与执行器共享的运行时状态。
 *
 * 面板只读这里，执行器（BatchRunner / AppExecutor）只写这里。
 * 改任何字段后调 notifyUi()，面板会在主线程刷新。
 */
object PanelState {

    enum class Phase { IDLE, RUNNING, PAUSED, BREAKER, DONE }

    @Volatile var phase: Phase = Phase.IDLE
    val running: Boolean get() = phase == Phase.RUNNING

    /** 0 保守 / 1 标准 / 2 激进 */
    @Volatile var tier: Int = 1
    @Volatile var dailyLimit: Int = 15
    @Volatile var gapMin: Int = 45
    @Volatile var gapMax: Int = 120
    /** 单批最多投多少家（设置页与面板共用） */
    @Volatile var batchCap: Int = 8
    /** 夜间静默时段，跨零点 */
    @Volatile var nightStart: Int = 23
    @Volatile var nightEnd: Int = 8

    @Volatile var todayCount: Int = 0
    @Volatile var batchDone: Int = 0
    @Volatile var batchTotal: Int = 0
    @Volatile var batchId: String = ""

    /** 当前这一条 */
    @Volatile var curCompany: String = ""
    @Volatile var curTitle: String = ""
    @Volatile var curAction: String = ""
    @Volatile var curStartedAt: Long = 0L
    /** 冷却剩余秒数；0 表示不在冷却 */
    @Volatile var waitSec: Long = 0L
    @Volatile var etaSec: Long = 0L

    /** 三态核验计数：只有 l3 算真送达 */
    @Volatile var l1: Int = 0
    @Volatile var l2: Int = 0
    @Volatile var l3: Int = 0
    @Volatile var fail: Int = 0
    @Volatile var dup: Int = 0
    @Volatile var consecutiveFail: Int = 0
    @Volatile var breakerReason: String = ""

    /** 熔断阈值：连续失败这么多次就停 */
    const val BREAKER_THRESHOLD = 3

    val tierNames = arrayOf("保守", "标准", "激进")
    val tierLimits = intArrayOf(8, 15, 30)

    // ---------------------------------------------------------------- 步骤流

    /** ok = null 表示进行中，true 通过，false 失败 */
    data class StepUi(val name: String, val ok: Boolean?, val detail: String = "")

    private val stepList = ArrayList<StepUi>()
    val steps: List<StepUi> get() = synchronized(stepList) { stepList.toList() }

    fun resetSteps() {
        synchronized(stepList) { stepList.clear() }
        notifyUi()
    }

    /** 追加一条已完成的步骤（执行器回调用） */
    fun addStep(name: String, ok: Boolean?, detail: String = "") {
        synchronized(stepList) {
            stepList.add(StepUi(name, ok, detail))
            while (stepList.size > 6) stepList.removeAt(0)
        }
        notifyUi()
    }

    fun beginStep(name: String) = addStep(name, null)

    /** 把最近一条"进行中"的步骤收尾；没有进行中的就补一条 */
    fun endStep(ok: Boolean, detail: String = "") {
        synchronized(stepList) {
            val i = stepList.indexOfLast { it.ok == null }
            if (i >= 0) {
                stepList[i] = stepList[i].copy(ok = ok, detail = detail)
            } else {
                stepList.add(StepUi(detail, ok))
            }
            while (stepList.size > 6) stepList.removeAt(0)
        }
        notifyUi()
    }

    // ---------------------------------------------------------------- 批次

    fun beginBatch(total: Int) {
        batchId = "B-" + SimpleDateFormat("MMdd-HHmm", Locale.getDefault()).format(Date())
        batchTotal = total
        batchDone = 0
        l1 = 0; l2 = 0; l3 = 0; fail = 0; dup = 0
        consecutiveFail = 0
        breakerReason = ""
        curCompany = ""; curTitle = ""; curAction = ""
        waitSec = 0L
        curStartedAt = 0L
        resetSteps()
        phase = Phase.RUNNING
        log("批次 $batchId 开始，共 $total 家")
        notifyUi()
    }

    fun finishBatch() {
        if (phase != Phase.BREAKER) phase = Phase.DONE
        waitSec = 0L
        curAction = ""
        log("批次结束：L3 送达 $l3 / $batchTotal，失败 $fail，重复 $dup")
        notifyUi()
    }

    fun tripBreaker(reason: String) {
        breakerReason = reason
        phase = Phase.BREAKER
        log("熔断：$reason")
        notifyUi()
    }

    fun togglePause() {
        phase = when (phase) {
            Phase.RUNNING -> Phase.PAUSED
            Phase.PAUSED -> Phase.RUNNING
            else -> phase
        }
        log(if (phase == Phase.PAUSED) "已暂停" else "继续执行")
        notifyUi()
    }

    /** 终止后回到配置态，但保留计数供报告页读取 */
    fun resetToIdle() {
        phase = Phase.IDLE
        waitSec = 0L
        curAction = ""
        notifyUi()
    }

    // ---------------------------------------------------------------- 订阅

    private val listeners = mutableListOf<(String) -> Unit>()
    private val uiListeners = mutableListOf<() -> Unit>()
    private val stamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun subscribe(fn: (String) -> Unit) {
        synchronized(listeners) { listeners.add(fn) }
    }

    fun unsubscribe(fn: (String) -> Unit) {
        synchronized(listeners) { listeners.remove(fn) }
    }

    fun subscribeUi(fn: () -> Unit) {
        synchronized(uiListeners) { uiListeners.add(fn) }
    }

    fun unsubscribeUi(fn: () -> Unit) {
        synchronized(uiListeners) { uiListeners.remove(fn) }
    }

    fun notifyUi() {
        val snapshot = synchronized(uiListeners) { uiListeners.toList() }
        snapshot.forEach { runCatching { it() } }
    }

    fun log(msg: String) {
        val line = "[${stamp.format(Date())}] $msg"
        val snapshot = synchronized(listeners) { listeners.toList() }
        snapshot.forEach { runCatching { it(line) } }
    }

    fun applyTier(t: Int) {
        tier = t.coerceIn(0, 2)
        dailyLimit = tierLimits[tier]
        notifyUi()
    }

    // ---------------------------------------------------------------- 持久化

    private const val PREFS = "jobpilot_config"
    @Volatile private var loaded = false

    /** 读回上次的设置。只生效一次，避免覆盖运行中的改动。 */
    fun load(ctx: Context) {
        if (loaded) return
        loaded = true
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        tier = p.getInt("tier", tier).coerceIn(0, 2)
        dailyLimit = p.getInt("dailyLimit", tierLimits[tier]).coerceIn(5, 40)
        gapMin = p.getInt("gapMin", 45).coerceIn(20, 120)
        gapMax = p.getInt("gapMax", minOf(240, gapMin * 2)).coerceIn(gapMin, 240)
        batchCap = p.getInt("batchCap", 8).coerceIn(1, 40)
        nightStart = p.getInt("nightStart", 23).coerceIn(0, 23)
        nightEnd = p.getInt("nightEnd", 8).coerceIn(0, 23)
        notifyUi()
    }

    fun persist(ctx: Context) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt("tier", tier)
            .putInt("dailyLimit", dailyLimit)
            .putInt("gapMin", gapMin)
            .putInt("gapMax", gapMax)
            .putInt("batchCap", batchCap)
            .apply()
    }

    fun summary(): String =
        "今日 $todayCount/$dailyLimit · 本批 $batchDone/$batchTotal · ${tierNames[tier]}"
}
