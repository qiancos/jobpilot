package com.jobpilot.pad.exec

import android.content.Context
import com.jobpilot.pad.data.Db

/**
 * 自动评分：简历换了就重算，刷新也重算。
 *
 * 判断「简历换了」用文件最后修改时间，存一个版本号，避免每次刷新都白算一遍。
 */
object AutoScorer {

    private const val PREF = "score_state"
    private const val KEY = "resume_ver"

    private fun lastVersion(ctx: Context): Long =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getLong(KEY, -1L)

    private fun markVersion(ctx: Context) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putLong(KEY, ResumeStore.version(ctx)).apply()
    }

    /** 简历是否变了（变了就要重算） */
    fun needRescore(ctx: Context): Boolean = lastVersion(ctx) != ResumeStore.version(ctx)

    /**
     * @param force true = 用户点了刷新，强制重算
     * @return 这次评了多少个岗位（0 = 没算）
     */
    fun run(ctx: Context, db: Db, force: Boolean, autoSelectIfNone: Boolean = true): Int {
        if (!force && !needRescore(ctx)) return 0

        val resume = ResumeProfile.load(ctx)
        if (!resume.loaded) return 0

        val jobs = db.listJobs(false)
        if (jobs.isEmpty()) {
            markVersion(ctx)
            return 0
        }

        val noneSelected = db.countSelected() == 0
        for (j in jobs) {
            val s = Scorer.score(j, resume)
            db.saveScore(
                j.id, s.total,
                s.hit.joinToString(","), s.miss.joinToString(","),
                s.strengths, s.gaps, s.advice
            )
            if (autoSelectIfNone && noneSelected && s.recommended) {
                db.setSelected(j.id, true)
            }
        }
        markVersion(ctx)
        return jobs.size
    }
}
