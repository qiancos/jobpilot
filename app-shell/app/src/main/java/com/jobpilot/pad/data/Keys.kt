package com.jobpilot.pad.data

/**
 * 归一化与去重键。
 *
 * 为什么需要它：网页通道有 BOSS 的 jobId，原生 App 通道没有（控件树里拿不到），
 * 两条路进来的同一个岗位会存成两条。所以统一用「公司归一化名|岗位归一化名」做去重键，
 * 并且加 UNIQUE 索引，让 INSERT OR IGNORE 直接生效。
 */
object Keys {

    private val BRACKET = Regex("[（(【\\[].*?[)）】\\]]")
    private val COMPANY_TAIL = Regex(
        "(股份有限公司|有限责任公司|有限公司|集团有限公司|集团|公司|" +
            "信息技术|信息科技|网络科技|科技|网络|信息|技术|股份|控股|实业|" +
            "广州|深圳|佛山|东莞|北京|上海|杭州)"
    )
    private val SPACE = Regex("\\s+")
    private val PUNCT = Regex("[·•\\-—_/\\\\|，,。.、:：;；\"'`]")

    /** 公司名归一化："某某信息科技有限公司" -> "某某信息" */
    fun normCompany(raw: String): String {
        var t = raw.trim()
        t = BRACKET.replace(t, "")
        t = COMPANY_TAIL.replace(t, "")
        t = PUNCT.replace(t, "")
        t = SPACE.replace(t, "")
        return if (t.length >= 2) t else raw.trim()
    }

    /** 岗位名归一化："Java 后端开发（27 届校招）" -> "java后端开发" */
    fun normTitle(raw: String): String {
        var t = raw.trim()
        t = BRACKET.replace(t, "")
        t = PUNCT.replace(t, "")
        t = SPACE.replace(t, "")
        return t.lowercase()
    }

    /** 去重键 */
    fun dedupe(company: String, title: String): String =
        normCompany(company) + "|" + normTitle(title)
}
