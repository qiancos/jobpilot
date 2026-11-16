package com.jobpilot.pad.collect

import android.view.accessibility.AccessibilityNodeInfo
import com.jobpilot.pad.A11yBridge
import com.jobpilot.pad.data.Job

/**
 * 从 BOSS **原生 App** 的控件树里采岗位。
 *
 * 实测结构（2026-11-16）：
 *   com.hpbr.bosszhipin:id/cl_card_container
 *   └ com.hpbr.bosszhipin:id/boss_job_card_view
 *     └ com.hpbr.bosszhipin:id/view_job_card   ← 一张卡
 *         └ 若干 TextView，按顺序是：
 *            岗位名 / 薪资 / 公司 / 融资·规模 / 学历 / 经验 / 福利标签… / HR·职位 / 活跃度 / 地区
 *
 * 因为 BOSS 没给每张卡一个稳定的 jobId，这里用「公司+岗位」哈希当主键做去重。
 */
object AppCollector {

    const val BOSS_PKG = "com.hpbr.bosszhipin"
    private const val ID_CARD = "$BOSS_PKG:id/view_job_card"

    private val SALARY = Regex("""(\d+\s*-\s*\d+\s*[Kk]|\d+\s*-\s*\d+\s*元/[天月]|面议|\d+\s*元/[天月])""")
    private val HR = Regex("""[男女]士\s*[·・]?|\bHR\b|招聘[专员经理]|总监|负责人""")
    private val ACTIVE = Regex("""活跃|刚刚回复|今日回复|本周回复|分钟内回复|小时前回复|\d+分钟前""")
    private val AREA = Regex("""^[\u4e00-\u9fa5]{2,6}\s+[\u4e00-\u9fa5]{2,}""")

    fun canCollect(): Boolean = A11yBridge.isBossForeground() && A11yBridge.root() != null

    fun currentPackage(): String = A11yBridge.lastPackage

    fun collect(keyword: String): Pair<List<Job>, String> {
        val diag = "svc=${A11yBridge.service != null} " +
            "lastPkg='${A11yBridge.lastPackage}' " +
            "svcConnected=${A11yBridge.service?.let { runCatching { it.rootInActiveWindow != null }.getOrNull() }}\n"
        val root = A11yBridge.root() ?: return emptyList<Job>() to (diag + "root=null")
        val cards = runCatching {
            root.findAccessibilityNodeInfosByViewId(ID_CARD)
        }.getOrNull() ?: return emptyList<Job>() to (diag + "找不到 $ID_CARD")

        val now = System.currentTimeMillis()
        val out = ArrayList<Job>(cards.size)
        val dump = StringBuilder(diag).append("cards=").append(cards.size).append('\n')

        for ((i, c) in cards.withIndex()) {
            val texts = ArrayList<String>()
            collectTexts(c, texts, 0)
            if (texts.isEmpty()) continue
            dump.append("#$i ").append(texts.joinToString(" | ")).append('\n')
            parse(texts, keyword, now)?.let { out.add(it) }
            runCatching { c.recycle() }
        }
        runCatching { root.recycle() }
        return out to dump.toString()
    }

    private fun collectTexts(n: AccessibilityNodeInfo?, out: MutableList<String>, depth: Int) {
        if (n == null || depth > 40 || out.size > 40) return
        val t = n.text?.toString()?.trim()
        if (!t.isNullOrEmpty()) out.add(t)
        for (i in 0 until n.childCount) {
            collectTexts(n.getChild(i), out, depth + 1)
        }
    }

    private fun parse(texts: List<String>, keyword: String, now: Long): Job? {
        if (texts.size < 3) return null
        val title = texts.firstOrNull { it.length in 2..40 && !SALARY.containsMatchIn(it) } ?: return null

        var salary = ""
        var salaryIdx = -1
        for ((i, t) in texts.withIndex()) {
            if (SALARY.containsMatchIn(t)) {
                salary = SALARY.find(t)?.value ?: t
                salaryIdx = i
                break
            }
        }

        val company = when {
            salaryIdx >= 0 && salaryIdx + 1 < texts.size -> texts[salaryIdx + 1]
            else -> texts.getOrNull(1) ?: ""
        }

        val hr = texts.firstOrNull { HR.containsMatchIn(it) && it.length < 20 } ?: ""
        val active = texts.firstOrNull { ACTIVE.containsMatchIn(it) } ?: ""
        val area = texts.lastOrNull { AREA.containsMatchIn(it) } ?: ""

        // 公司规模和融资：紧跟在公司后面、且不是 HR/地区/活跃度的短文本
        val companyExtra = texts.dropWhile { it != company }
            .drop(1)
            .take(3)
            .filter { it.length <= 8 && it != hr && it != area && !ACTIVE.containsMatchIn(it) }
            .joinToString("/")

        val tags = texts.filter {
            it != title && it != salary && it != company && it != hr &&
                it != area && !ACTIVE.containsMatchIn(it) && it.length <= 12 &&
                !companyExtra.split("/").contains(it)
        }.distinct().take(6).joinToString("/")

        val id = "app_" + (company + "|" + title).hashCode().toString().replace("-", "n")

        return Job(
            id = id,
            title = title,
            company = company,
            salary = salary,
            area = area,
            tags = tags.ifBlank { companyExtra },
            href = "boss://${company}/${title}",
            keyword = keyword,
            collectedAt = now,
            source = "app",
        )
    }
}
