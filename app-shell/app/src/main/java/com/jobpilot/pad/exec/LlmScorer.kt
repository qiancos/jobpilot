package com.jobpilot.pad.exec

import android.content.Context
import com.jobpilot.pad.data.Db
import com.jobpilot.pad.data.Job
import org.json.JSONArray
import org.json.JSONObject

/**
 * 用 skill + 大模型给岗位打分。
 *
 * 流程和 UPCV 一致：读 skill 当 system prompt → 把简历和 JD 塞进 user →
 * 要模型返回严格 JSON → 落库。没配 Key 就回退规则打分。
 */
object LlmScorer {

    data class V(
        val ok: Boolean,
        val score: Int = 0,
        val level: String = "",
        val pros: List<String> = emptyList(),
        val gaps: List<String> = emptyList(),
        val verify: List<String> = emptyList(),
        val advice: String = "",
        val err: String = "",
    )

    fun score(ctx: Context, job: Job, resumeText: String, jdText: String): V {
        val cfg = LlmClient.cfg(ctx)
        if (!cfg.ready) return V(false, err = "没配 API Key")

        val skill = LlmClient.skill(ctx, "job-match")
        if (skill.isBlank()) return V(false, err = "缺少 skills/job-match.md")

        val resume = if (resumeText.length > 6000) resumeText.take(6000) else resumeText
        val jd = if (jdText.length > 4000) jdText.take(4000) else jdText

        val user = buildString {
            append("## 岗位\n")
            append("公司：${job.company}\n")
            append("岗位：${job.title}\n")
            append("薪资：${job.salary}\n")
            append("地点：${job.area}\n")
            append("标签：${job.tags}\n\n")
            append("## JD\n")
            append(if (jd.isBlank()) "（未取到 JD 正文，仅根据岗位名与标签判断，并在 advice 里说明依据不足）" else jd)
            append("\n\n## 求职者简历\n")
            append(if (resume.isBlank()) "（简历为空）" else resume)
            append("\n\n按 skill 要求输出严格 JSON。")
        }

        val raw = LlmClient.chat(ctx, skill, user, 6000)
            ?: return V(false, err = "请求失败（网络或 Key 无效）")
        if (raw.startsWith("ERR:")) return V(false, err = raw.take(160))

        val obj = LlmClient.extractJson(raw) ?: return V(false, err = "模型没返回 JSON：" + raw.take(80))

        val score = obj.optInt("score", -1)
        if (score < 0) return V(false, err = "JSON 里没有 score")

        return V(
            ok = true,
            score = score.coerceIn(0, 100),
            level = obj.optString("level", ""),
            pros = arr(obj, "pros"),
            gaps = arr(obj, "gaps"),
            verify = arr(obj, "verify"),
            advice = obj.optString("advice", ""),
        )
    }

    private fun arr(o: JSONObject, key: String): List<String> {
        val a = o.optJSONArray(key) ?: return emptyList()
        val out = ArrayList<String>(a.length())
        for (i in 0 until a.length()) {
            a.optString(i).takeIf { it.isNotBlank() }?.let { out.add(it) }
        }
        return out
    }

    /**
     * 批量用模型打分。每条要一次请求，所以默认只算勾选的。
     * @return 成功条数
     */
    fun run(
        ctx: Context,
        db: Db,
        jobs: List<Job>,
        jdProvider: (Job) -> String,
        onP: (Int, Int, String, String) -> Unit,
    ): Int {
        var done = 0
        val resume = ResumeStore.current(ctx)
        for ((i, j) in jobs.withIndex()) {
            onP(i, jobs.size, j.company, "读 JD")
            val jd = runCatching { jdProvider(j) }.getOrDefault("")
            onP(i, jobs.size, j.company, "模型打分")
            val v = score(ctx, j, resume, jd)
            if (v.ok) {
                val strengths = if (v.pros.isEmpty()) "" else v.pros.joinToString("；")
                val gaps = buildList {
                    addAll(v.gaps)
                    if (v.verify.isNotEmpty()) add("【待核实】" + v.verify.joinToString("；"))
                }.joinToString("；")
                db.saveScore(j.id, v.score, "", "", strengths, gaps, v.advice)
                done++
            }
            onP(i + 1, jobs.size, j.company, if (v.ok) "${v.score} 分" else "跳过：${v.err.take(20)}")
        }
        return done
    }
}
