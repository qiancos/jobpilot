package com.jobpilot.pad.exec

import com.jobpilot.pad.data.Job

/**
 * 按岗位 + 简历生成打招呼语。
 *
 * 不用大模型也能做到「一条一版」：
 *   1. 岗位名进正文
 *   2. 从简历里挑一条和 JD 命中技能相关的真实经历
 *   3. 点出 2 个命中技能
 *
 * 结构固定但事实来自简历，所以不会出现「编经历」。
 */
object GreetingGen {

    fun build(job: Job, resume: ResumeProfile.P): String {
        val jd = ResumeProfile.extract(
            (job.title + " " + job.tags + " " + job.keyword).lowercase()
        )
        val hit = jd.filter { resume.skills.contains(it) }
            .sortedByDescending { if (it == "java" || it == "spring" || it == "mysql") 3 else 1 }
            .take(2)

        val fact = pickFact(resume.text, hit)
        val title = job.title.take(18)

        val sb = StringBuilder()
        sb.append("您好，我是 2027 届计算机应届生")
        if (fact.isNotBlank()) sb.append("，$fact")
        sb.append("。")

        if (hit.isNotEmpty()) {
            sb.append("看到贵司「$title」要求")
            sb.append(hit.joinToString("、") { it.uppercase() })
            sb.append("，这些我都在项目里用过。")
        } else {
            sb.append("看到贵司「$title」这个岗位，和我的方向比较接近。")
        }
        sb.append("方便聊聊吗？")

        return polish(sb.toString())
    }

    /** 从简历里找一条和某个技能相关的句子，压到 40 字以内 */
    private fun pickFact(resumeText: String, skills: List<String>): String {
        if (resumeText.isBlank()) return ""
        val sentences = resumeText
            .split('。', '\n', '；')
            .map { it.trim() }
            .filter { it.length in 12..80 }

        val keys = if (skills.isEmpty()) listOf("实习", "项目") else skills + listOf("实习", "项目")
        for (k in keys) {
            val s = sentences.firstOrNull { it.lowercase().contains(k) } ?: continue
            val cleaned = s.replace(Regex("^[\\d\\.、\\s]+"), "").trim()
            if (cleaned.startsWith("▎")) continue
            return if (cleaned.length > 40) cleaned.take(38) + "等" else cleaned
        }
        return ""
    }

    /** 去空格、去连续标点、压到 120 字内 */
    private fun polish(s: String): String {
        var t = s.replace(Regex("\\s+"), " ")
            .replace("。。", "。")
            .replace("，，", "，")
            .trim()
        if (t.length > 120) {
            val cut = t.take(117)
            val stop = cut.lastIndexOfAny(charArrayOf('，', '。', '、'))
            t = if (stop > 60) cut.take(stop) + "。方便聊聊吗？" else cut + "…"
        }
        return t
    }
}
