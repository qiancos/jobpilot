package com.jobpilot.pad

import android.view.accessibility.AccessibilityNodeInfo

/**
 * 无障碍服务的全局入口。
 *
 * 采集器（AppCollector）需要从 Service 外部拿 rootInActiveWindow，
 * 所以用这个桥把 Service 实例暴露出来。
 */
object A11yBridge {

    @Volatile
    var service: DumpService? = null

    /** 最后一次收到事件的包名，用来判断"现在前台是不是 BOSS" */
    @Volatile
    var lastPackage: String = ""

    fun root(): AccessibilityNodeInfo? = runCatching {
        service?.rootInActiveWindow
    }.getOrNull()

    fun accessible(): Boolean = service != null

    fun isBossForeground(): Boolean = lastPackage == "com.hpbr.bosszhipin"
}
