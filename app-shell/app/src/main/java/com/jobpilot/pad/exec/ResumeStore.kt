package com.jobpilot.pad.exec

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 简历存储：用户自己上传的那份优先，没上传才用打包进 assets 的默认简历。
 *
 * 打分、生成话术都读这里，所以「先上传简历才能打分」是硬约束。
 */
object ResumeStore {

    private const val FILE = "resume_user.txt"
    private const val PREF = "resume_meta"

    data class Info(val name: String, val chars: Int, val updatedAt: Long) {
        fun timeText(): String =
            SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(Date(updatedAt))
    }

    private fun file(ctx: Context) = File(ctx.filesDir, FILE)

    /** 当前生效的简历文本 */
    fun current(ctx: Context): String {
        val f = file(ctx)
        if (f.exists() && f.length() > 0) {
            return runCatching { f.readText() }.getOrDefault("")
        }
        return runCatching {
            ctx.assets.open("resume.txt").bufferedReader().use { it.readText() }
        }.getOrDefault("")
    }

    /** 简历版本号：用户上传那份的最后修改时间；没上传用内置的固定值 0 */
    fun version(ctx: Context): Long {
        val f = file(ctx)
        return if (f.exists()) f.lastModified() else 0L
    }

    fun isUserUploaded(ctx: Context): Boolean {
        val f = file(ctx)
        return f.exists() && f.length() > 0
    }

    fun save(ctx: Context, text: String, name: String) {
        runCatching {
            file(ctx).writeText(text)
            ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
                .putString("name", name)
                .putLong("at", System.currentTimeMillis())
                .apply()
        }
    }

    fun clear(ctx: Context) {
        runCatching { file(ctx).delete() }
        runCatching {
            ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply()
        }
    }

    fun info(ctx: Context): Info? {
        val text = current(ctx)
        if (text.isBlank()) return null
        val sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val name = sp.getString("name", null) ?: "内置简历 resume.txt"
        val at = sp.getLong("at", 0L)
        return Info(name, text.length, at)
    }
}
