package com.jobpilot.pad.exec

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 极简落盘日志。
 *
 * 为什么要它：跑在后台的任务，失败时界面看不见（BOSS 盖住我们的窗口），
 * 只能靠文件记录每一步。出问题直接 adb 拉出来看。
 */
object Log {

    private const val FILE = "jobpilot.log"
    private const val MAX_BYTES = 200_000

    private val fmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.CHINA)

    private fun file(ctx: Context) = File(ctx.filesDir, FILE)

    @Synchronized
    fun i(ctx: Context, tag: String, msg: String) {
        runCatching {
            val f = file(ctx)
            if (f.exists() && f.length() > MAX_BYTES) {
                // 简单截断：保留后半段
                val tail = f.readText().takeLast(MAX_BYTES / 2)
                f.writeText(tail)
            }
            f.appendText("${fmt.format(Date())} [$tag] $msg\n")
        }
    }

    fun read(ctx: Context): String = runCatching { file(ctx).readText() }.getOrDefault("")

    fun tail(ctx: Context, lines: Int = 80): String {
        val all = read(ctx).lines()
        return all.takeLast(lines).joinToString("\n")
    }

    fun clear(ctx: Context) {
        runCatching { file(ctx).delete() }
    }
}
