package com.jobpilot.pad.exec

import android.content.Context

/**
 * 简历画像：把简历文本变成一组「会什么」的关键词。
 *
 * 打分逻辑照 UPCV 的思路：拿 JD 里的技能词，和简历里的技能词做交集。
 * 交集多 = 匹配度高；JD 里有、简历里没有 = 短板。
 */
object ResumeProfile {

    data class P(
        val text: String,
        val skills: Set<String>,
        val cities: Set<String>,
    ) {
        val loaded: Boolean get() = text.isNotBlank()
    }

    /** 技能词典：只收「招聘 JD 里会写、简历里也会写」的词 */
    private val DICT = listOf(
        "java", "spring", "springboot", "spring boot", "springcloud", "spring boot",
        "mybatis", "mybatis-plus", "hibernate", "jpa",
        "mysql", "oracle", "postgresql", "sql", "redis", "mongodb", "es", "elasticsearch",
        "kafka", "rabbitmq", "rocketmq", "mq",
        "docker", "kubernetes", "k8s", "linux", "git", "maven", "gradle", "nginx", "jenkins",
        "vue", "vue3", "element", "elementplus", "javascript", "typescript", "html", "css",
        "python", "node", "go", "c++", "c#",
        "微服务", "高并发", "分布式", "多线程", "jvm", "设计模式", "数据结构", "算法",
        "restful", "接口设计", "电商", "支付", "订单", "权限", "后台管理", "管理系统",
        "小程序", "爬虫", "数据可视化", "ai", "大模型", "llm",
    )

    @Volatile
    private var cached: P? = null

    /** 上传新简历后调用，让下次重新解析 */
    fun invalidate() {
        cached = null
    }

    fun load(ctx: Context): P {
        cached?.let { return it }
        val text = ResumeStore.current(ctx)
        val p = P(
            text = text,
            skills = extract(text),
            cities = listOf("广州", "深圳", "佛山", "东莞").filter { text.contains(it) }.toSet(),
        )
        cached = p
        return p
    }

    /** 从任意文本里抽出命中的技能词 */
    fun extract(text: String): Set<String> {
        if (text.isBlank()) return emptySet()
        val low = text.lowercase()
        return DICT.filter { low.contains(it) }.toSet()
    }

    /** 简历里有没有这一段经历（给「优势」引用用） */
    fun snippetFor(skill: String, ctx: Context): String {
        val text = load(ctx).text
        val idx = text.lowercase().indexOf(skill)
        if (idx < 0) return ""
        val start = (idx - 30).coerceAtLeast(0)
        val end = (idx + 60).coerceAtMost(text.length)
        return text.substring(start, end).replace("\n", " ").trim()
    }
}
