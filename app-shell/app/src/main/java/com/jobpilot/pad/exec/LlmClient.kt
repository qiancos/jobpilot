package com.jobpilot.pad.exec

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * 大模型调用（OpenAI 兼容协议）。
 *
 * 和 UPCV 一样：skill（Markdown 提示词）+ API Key 调模型，答案由模型给，不是写死的规则。
 * 默认接 DeepSeek，也能改成任何 OpenAI 兼容端点。
 */
object LlmClient {

    private const val PREF = "llm_cfg"

    // 这台机器上的默认配置（用户自己的 Key，只存在于本机 APK 内）
    private const val DEFAULT_KEY = "sk-c750df1c421f466792950bb23540a5f2"

    data class Cfg(
        val key: String = DEFAULT_KEY,
        val baseUrl: String = "https://api.deepseek.com/v1",
        val model: String = "deepseek-flash",
    ) {
        val ready: Boolean get() = key.isNotBlank()
    }

    fun cfg(ctx: Context): Cfg {
        val sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return Cfg(
            key = sp.getString("key", DEFAULT_KEY).orEmpty(),
            baseUrl = sp.getString("base", "https://api.deepseek.com/v1").orEmpty(),
            model = sp.getString("model", "deepseek-flash").orEmpty(),
        )
    }

    fun save(ctx: Context, c: Cfg) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putString("key", c.key.trim())
            .putString("base", c.baseUrl.trim().trimEnd('/'))
            .putString("model", c.model.trim())
            .apply()
    }

    /** 读 skill 文件（assets/skills/xxx.md） */
    fun skill(ctx: Context, name: String): String = runCatching {
        ctx.assets.open("skills/$name.md").bufferedReader().use { it.readText() }
    }.getOrDefault("")

    /**
     * 发一次 chat completion，返回 assistant 的正文。
     * 调用方必须在后台线程。
     */
    fun chat(ctx: Context, system: String, user: String, maxTokens: Int = 3000): String? {
        val c = cfg(ctx)
        if (!c.ready) return null

        val body = JSONObject().apply {
            put("model", c.model)
            put("temperature", 0.2)
            put("max_tokens", maxTokens)
            put(
                "messages", JSONArray().apply {
                    put(JSONObject().put("role", "system").put("content", system))
                    put(JSONObject().put("role", "user").put("content", user))
                }
            )
        }

        var conn: HttpURLConnection? = null
        return try {
            val url = URL(c.baseUrl.trimEnd('/') + "/chat/completions")
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 90_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Authorization", "Bearer ${c.key}")
            }
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) return "ERR:$code:" + text.take(200)

            val json = JSONObject(text)
            json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        } catch (t: Throwable) {
            null
        } finally {
            runCatching { conn?.disconnect() }
        }
    }

    /**
     * 连通性测试：返回「服务端回显的模型名 + 回复内容」，
     * 这样能在 App 里直接确认到底调的是哪个模型。
     */
    fun ping(ctx: Context): String {
        val c = cfg(ctx)
        if (!c.ready) return "没配 Key"
        val body = JSONObject().apply {
            put("model", c.model)
            put("max_tokens", 300)
            put("messages", JSONArray().apply {
                put(JSONObject().put("role", "user").put("content", "只回四个字：接口正常"))
            })
        }
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(c.baseUrl.trimEnd('/') + "/chat/completions")
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 60_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Authorization", "Bearer ${c.key}")
            }
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) return "HTTP $code：" + text.take(150)
            val j = JSONObject(text)
            val model = j.optString("model", "?")
            val content = j.optJSONArray("choices")?.optJSONObject(0)
                ?.optJSONObject("message")?.optString("content", "").orEmpty()
            "模型=$model ｜ 回复=${content.take(20)} ｜ 地址=${c.baseUrl}"
        } catch (t: Throwable) {
            "异常：" + (t.message ?: "unknown")
        } finally {
            runCatching { conn?.disconnect() }
        }
    }

    /**
     * 从模型输出里抽 JSON。模型可能包 ```json，也可能因 max_tokens 被截断，
     * 所以解析失败时从后往前收缩到最后一个能解析的 }。
     */
    fun extractJson(raw: String): JSONObject? {
        val start = raw.indexOf('{')
        if (start < 0) return null

        val end = raw.lastIndexOf('}')
        if (end > start) {
            runCatching { JSONObject(raw.substring(start, end + 1)) }.getOrNull()?.let { return it }
        }

        // 截断修复：从后往前找可行的闭合位置
        var cut = raw.lastIndexOf('}', if (end > start) end - 1 else raw.length - 1)
        var tries = 0
        while (cut > start && tries < 60) {
            runCatching { JSONObject(raw.substring(start, cut + 1)) }.getOrNull()?.let { return it }
            cut = raw.lastIndexOf('}', cut - 1)
            tries++
        }
        return null
    }
}
