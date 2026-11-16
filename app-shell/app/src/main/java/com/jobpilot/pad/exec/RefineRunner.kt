package com.jobpilot.pad.exec

import android.content.Context
import com.jobpilot.pad.data.Db
import com.jobpilot.pad.data.Job

/**
 * 读 JD 精算：逐个进岗位详情，读真正的职位描述，再按「简历 ∩ JD」重新打分。
 *
 * 比列表打分的准确度高，但慢——每读一个岗位要两三秒，50 个岗位大概两分钟。
 */
object RefineRunner {

    @Volatile
    private var stopFlag = false

    fun stop() {
        stopFlag = true
    }

    data class P(val index: Int, val total: Int, val company: String, val phase: String, val done: Int)

    fun run(
        ctx: Context,
        jobs: List<Job>,
        db: Db,
        resume: ResumeProfile.P,
        onP: (P) -> Unit,
    ): Int {
        val useLlm = LlmClient.cfg(ctx).ready
        stopFlag = false
        var done = 0

        // 等用户切到 BOSS（最多 30 秒），否则 rootInActiveWindow 会是我们自己的界面
        var waited = 0
        while (waited < 30_000) {
            val pkg = runCatching {
                com.jobpilot.pad.A11yBridge.service?.rootInActiveWindow?.packageName
            }.getOrNull()
            if (pkg == "com.hpbr.bosszhipin") break
            Thread.sleep(500); waited += 500
        }
        for ((i, job) in jobs.withIndex()) {
            if (stopFlag) break
            onP(P(i, jobs.size, job.company, "打开详情", done))
            if (!BatchRunner.navigateToDetail(job)) {
                onP(P(i, jobs.size, job.company, "没找到岗位，跳过", done))
                BatchRunner.backToList()
                continue
            }
            Thread.sleep(400)
            val jd = JdReader.read()
            onP(P(i, jobs.size, job.company, "读 JD ${jd.length} 字", done))
            if (jd.length >= 80) db.saveJd(job.id, jd)
            if (useLlm) {
                val v = LlmScorer.score(ctx, job, resume.text, jd)
                if (v.ok) {
                    val need = if (v.verify.isEmpty()) "" else "【待核实】" + v.verify.joinToString("；")
                    db.saveScore(
                        job.id, v.score, "", "",
                        v.pros.joinToString("；"),
                        (v.gaps + listOfNotNull(need.ifBlank { null })).joinToString("；"),
                        v.advice
                    )
                    done++
                    BatchRunner.backToList()
                    continue
                }
            }
            val sc = Scorer.score(job, resume, jd)
            db.saveScore(
                job.id, sc.total,
                sc.hit.joinToString(","), sc.miss.joinToString(","),
                sc.strengths, sc.gaps, sc.advice
            )
            done++
            BatchRunner.backToList()
        }
        return done
    }
}
