package com.jobpilot.pad.exec

import com.jobpilot.pad.PanelState
import com.jobpilot.pad.data.Db
import com.jobpilot.pad.data.Job
import java.util.Calendar

/**
 * 投递前的四道刹车。
 *
 * 规则来自 PRD §9.4 / §9.5，阈值取自 PanelState（设置页可改）：
 *   1. 同一岗位投过就不再投（防重复骚扰）
 *   2. 同一家公司当天只投一个岗（防连投）
 *   3. 两次投递之间按设置里的条间隔随机等待（防机器特征）
 *   4. 每日上限按档位 8 / 15 / 30 条（防封号）
 *   5. 夜间静默 23:00–08:00 不投
 */
object Safety {

    data class Gate(val ok: Boolean, val reason: String = "") {
        companion object {
            val PASS = Gate(true)
        }
    }

    /** 基于时间戳的伪随机间隔：同一时刻算出来的值稳定，方便复现 */
    private fun gapSec(seed: Long): Long {
        val min = PanelState.gapMin.toLong()
        val max = PanelState.gapMax.toLong().coerceAtLeast(min)
        return min + (Math.abs(seed) % (max - min + 1))
    }

    /** 现在是否在夜间静默时段（支持跨零点） */
    fun inNightSilence(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val s = PanelState.nightStart
        val e = PanelState.nightEnd
        return if (s > e) hour >= s || hour < e else hour in s until e
    }

    fun check(db: Db, job: Job): Gate {
        // 1. 这个岗位投过没有
        if (db.deliveredFor(job.id)) {
            return Gate(false, "「${job.company} · ${job.title}」这个岗位已经投过了。")
        }

        // 2. 同公司今天投过没有
        if (db.companyContactedToday(job.company)) {
            return Gate(false, "「${job.company}」今天已经投过一个岗位了。\n同公司当日只投一个，避免连投。")
        }

        // 3. 每日额度
        val limit = PanelState.dailyLimit
        val today = db.countDeliveredToday()
        if (today >= limit) {
            return Gate(false, "今日额度已用完（$today / $limit）。\n明天再来，或者现在只做试跑。")
        }

        // 4. 夜间静默
        if (inNightSilence()) {
            return Gate(
                false,
                "现在是夜间静默时段（${PanelState.nightStart}:00–${PanelState.nightEnd}:00）。\n" +
                    "这个时段不投递，避免异常行为；到点后自动恢复。"
            )
        }

        // 5. 两次之间的间隔
        val last = db.lastDeliveredAt()
        if (last > 0) {
            val need = gapSec(last)
            val elapsed = (System.currentTimeMillis() - last) / 1000
            if (elapsed < need) {
                val left = need - elapsed
                return Gate(
                    false,
                    "太快了：上一次投递在 ${elapsed} 秒前，\n" +
                        "按 ${PanelState.gapMin}–${PanelState.gapMax} 秒随机间隔还要等 ${left} 秒。\n\n" +
                        "这个间隔是故意留的——连续秒发是最容易被判成机器人的特征。"
                )
            }
        }

        return Gate.PASS
    }

    /** 这条与下一条之间该等多少秒 */
    fun nextGapSec(): Long = gapSec(System.currentTimeMillis())

    /** 给界面显示用：还要等多少秒才能投下一条 */
    fun waitRemainSec(db: Db): Long {
        val last = db.lastDeliveredAt()
        if (last <= 0) return 0
        val need = gapSec(last)
        val elapsed = (System.currentTimeMillis() - last) / 1000
        return (need - elapsed).coerceAtLeast(0)
    }
}
