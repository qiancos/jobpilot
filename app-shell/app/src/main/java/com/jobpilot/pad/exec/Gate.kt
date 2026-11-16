package com.jobpilot.pad.exec

import com.jobpilot.pad.data.Job

/**
 * 话术闸门（9 条）。从 Python 版 lint.py 移植过来的规则版。
 *
 * 目的不是把话术写漂亮，是**拦住会被 HR 一眼看穿是群发的那种句子**。
 */
object Gate {

    data class Report(
        val pass: Boolean,
        val fails: List<String>,
        val score: Int,
    ) {
        val summary: String get() = if (pass) "$score/9" else "待改（$score/9）"
    }

    private val CLICHES = listOf(
        "贵公司很感兴趣", "非常感兴趣", "希望能有机会", "感谢您百忙之中",
        "期待您的回复", "如有机会", "万分感谢", "祝工作顺利", "祝您生活愉快",
    )

    private val SENSITIVE = listOf(
        "内推", "薪资多少", "多少钱", "加微信", "打电话", "电话联系",
        "包吃住", "双休吗", "五险一金吗",
    )

    private val FAKE_FACT = listOf(
        "简历已发送", "简历已投递", "已投递", "期待面试", "随时可到岗"
    )

    fun check(text: String, job: Job): Report {
        val fails = ArrayList<String>()
        val t = text.trim()
        var score = 0

        // 1. 长度
        if (t.length in 50..120) score++ else fails.add("长度 ${t.length} 字，应在 50–120 之间")

        // 2. 提到岗位
        val titleKey = job.title.take(6)
        if (t.contains(titleKey) || t.contains(job.title.take(10))) score++
        else fails.add("没有提到岗位名「${job.title.take(12)}」")

        // 3. 至少一个技术词
        val techHit = ResumeProfile.extract(t)
        if (techHit.isNotEmpty()) score++ else fails.add("没有任何具体技术词")

        // 4. 空话套话
        val cliche = CLICHES.firstOrNull { t.contains(it) }
        if (cliche == null) score++ else fails.add("有套话「$cliche」")

        // 5. 敏感词
        val sens = SENSITIVE.firstOrNull { t.contains(it) }
        if (sens == null) score++ else fails.add("有敏感词「$sens」")

        // 6. 未发生的事实
        val fake = FAKE_FACT.firstOrNull { t.contains(it) }
        if (fake == null) score++ else fails.add("写了还没发生的事「$fake」")

        // 7. emoji / 特殊符号
        if (!t.contains(Regex("[\\uD800-\\uDBFF\\uDC00-\\uDFFF\\u2600-\\u27BF]"))) score++
        else fails.add("含表情符号，BOSS 里显得不正式")

        // 8. 感叹号不超过 1 个
        val exclaims = t.count { it == '!' || it == '！' }
        if (exclaims <= 1) score++ else fails.add("感叹号 $exclaims 个，太热情")

        // 9. 结尾是问句或明确下一步
        if (t.endsWith("吗？") || t.endsWith("？") || t.endsWith("沟通吗？")) score++
        else fails.add("结尾没有问句，HR 没有回的钩子")

        return Report(fails.isEmpty(), fails, score)
    }
}
