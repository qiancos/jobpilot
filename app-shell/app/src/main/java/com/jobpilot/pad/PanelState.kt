package com.jobpilot.pad

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 面板与执行器共享的运行时状态。
 *
 * 现在只是个"接线板"：面板改这里，执行器（还没写）读这里。
 * 一旦执行器写出来，它只需要 subscribe 日志、轮询/订阅 running。
 */
object PanelState {

    @Volatile var running: Boolean = false

    /** 0 保守 / 1 标准 / 2 激进 */
    @Volatile var tier: Int = 1
    @Volatile var dailyLimit: Int = 15
    @Volatile var gapMin: Int = 45
    @Volatile var gapMax: Int = 120

    var todayCount: Int = 0
    var batchDone: Int = 0
    var batchTotal: Int = 0

    val tierNames = arrayOf("保守", "标准", "激进")
    val tierLimits = intArrayOf(8, 15, 30)

    private val listeners = mutableListOf<(String) -> Unit>()
    private val stamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun subscribe(fn: (String) -> Unit) {
        synchronized(listeners) { listeners.add(fn) }
    }

    fun unsubscribe(fn: (String) -> Unit) {
        synchronized(listeners) { listeners.remove(fn) }
    }

    fun log(msg: String) {
        val line = "[${stamp.format(Date())}] $msg"
        val snapshot = synchronized(listeners) { listeners.toList() }
        snapshot.forEach { runCatching { it(line) } }
    }

    fun applyTier(t: Int) {
        tier = t.coerceIn(0, 2)
        dailyLimit = tierLimits[tier]
    }

    fun summary(): String =
        "今日 $todayCount/$dailyLimit · 本批 $batchDone/$batchTotal · ${tierNames[tier]}"
}
