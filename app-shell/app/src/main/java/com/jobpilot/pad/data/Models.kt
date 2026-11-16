package com.jobpilot.pad.data

/** 一个岗位（清单里的一行） */
data class Job(
    val id: String,
    val title: String,
    val company: String,
    val salary: String,
    val area: String,
    val tags: String,
    val href: String,
    val keyword: String,
    val collectedAt: Long,
    val selected: Boolean = false,
    /** 采集来源：web（网页降级）/ app（原生控件树） */
    val source: String = "",
) {
    /** 手机上显示用的一行摘要 */
    fun summary(): String = listOf(company, salary, area)
        .filter { it.isNotBlank() }
        .joinToString(" · ")
}
