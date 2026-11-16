package com.jobpilot.pad.exec

import com.jobpilot.pad.data.Job

/**
 * 岗位匹配度：JD 技能词 ∩ 简历技能词。
 *
 * 和 UPCV 一致的地方：分不是拍脑袋给的，是「岗位要什么 vs 你会什么」算出来的，
 * 所以「优势」「短板」都能指着具体词说，而不是一句「匹配度 86」。
 */
object Scorer {

    data class Score(
        val total: Int,
        val excluded: Boolean,
        val reason: String,
        val hit: List<String>,
        val miss: List<String>,
        val strengths: String,
        val gaps: String,
        val advice: String,
    ) {
        val recommended: Boolean get() = !excluded && total >= 65
    }

    /** 直接排除 */
    private val HARD_REJECT = listOf(
        "销售", "客服", "话务", "电话", "中介", "猎头", "讲师", "培训",
        "催收", "地推", "招商", "主播", "带货",
    )

    /** 非目标方向 */
    private val OFF_TRACK = listOf("产品经理", "运营", "ui", "设计", "行政", "人事")

    /** 目标城市 */
    private val CITIES = listOf("广州", "深圳", "佛山", "东莞")

    fun score(job: Job, resume: ResumeProfile.P, jd: String = ""): Score {
        // 有真 JD 就用真 JD，没有就退回列表卡片上的字段
        val blob = if (jd.length > 60) jd.lowercase()
                   else (job.title + " " + job.tags + " " + job.keyword + " " + job.company).lowercase()

        // 1. 硬排除
        val rejected = HARD_REJECT.firstOrNull { blob.contains(it) }
        if (rejected != null) {
            return Score(
                8, true, "含排除词「$rejected」",
                emptyList(), emptyList(), "", "方向不符", "跳过"
            )
        }

        // 2. JD 要什么
        val jdSkills = ResumeProfile.extract(blob)

        // 3. 你会什么 ∩ 它要什么
        val hit = jdSkills.filter { resume.skills.contains(it) }.sorted()
        val miss = jdSkills.filterNot { resume.skills.contains(it) }.sorted()

        // 4. 分 = 命中比例为主
        val ratio = when {
            jdSkills.isEmpty() -> 0.45          // JD 没写技术要求，给中性分
            else -> hit.size.toDouble() / jdSkills.size
        }

        var total = 32 + (ratio * 48).toInt()   // 32–80

        // 5. 城市
        val cityOk = CITIES.any { job.area.contains(it) }
        total += if (cityOk) 10 else -14

        // 6. 薪资
        val salary = parseSalary(job.salary)
        total += when {
            salary == null -> 0
            salary >= 10_000 -> 10
            salary >= 8_000 -> 7
            salary >= 5_000 -> 3
            else -> 0
        }

        // 7. 方向偏移
        val off = OFF_TRACK.firstOrNull { job.title.lowercase().contains(it) }
        if (off != null) total -= 20

        // 8. 年限过高
        if (job.tags.contains("5-10年") || job.tags.contains("10年以上")) total -= 16

        total = total.coerceIn(0, 100)

        // ---- 三件套 ----
        val strengths = when {
            hit.isNotEmpty() && cityOk ->
                "简历里有 ${hit.take(4).joinToString("、")}，正是这个岗位要的；城市也对口。"
            hit.isNotEmpty() ->
                "简历里有 ${hit.take(4).joinToString("、")}，技术栈对得上。"
            cityOk -> "城市对口，但技术栈没有明显重合。"
            else -> "和你的方向没有明显重合。"
        }

        val gaps = when {
            miss.isNotEmpty() -> "JD 里出现 ${miss.take(4).joinToString("、")}，简历里没有写。"
            jdSkills.isEmpty() -> "JD 没写具体技术栈，判断依据不足。"
            !cityOk -> "不在目标城市（${job.area.take(12)}）。"
            else -> "没有明显短板。"
        }

        val advice = when {
            total >= 80 -> "直接投，话术里点出上面那几个命中词。"
            total >= 65 -> "可以投，补一句和 ${hit.firstOrNull() ?: "相关技术"} 有关的项目。"
            total >= 45 -> "优先级放后面，先投高分的。"
            else -> "建议跳过。"
        }

        return Score(total, false, "", hit, miss, strengths, gaps, advice)
    }

    private fun parseSalary(s: String): Int? {
        if (s.isBlank()) return null
        val nums = Regex("\\d+(\\.\\d+)?").findAll(s).mapNotNull { it.value.toDoubleOrNull() }.toList()
        if (nums.isEmpty()) return null
        val daily = s.contains("天")
        return if (daily) (nums.first() * 22).toInt() else (nums.first() * 1000).toInt()
    }
}
