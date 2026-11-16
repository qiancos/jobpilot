package com.jobpilot.pad.exec

import android.content.Context
import android.provider.Settings
import com.jobpilot.pad.ShizukuShell
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.ByteArrayOutputStream
import java.util.zip.Inflater

/**
 * 用 Shizuku 把无障碍服务「钉」住。
 *
 * 为什么手动开会被关：vivo 对侧载 App 的无障碍有额外策略，界面里打开的开关
 * 过一会儿会被系统改回去。直接用 shell 写两个 secure 设置，能稳定生效：
 *   1. enabled_accessibility_services  —— 指定启用哪个服务
 *   2. accessibility_enabled           —— 总开关（只设第 1 个不够）
 */
object SecureFix {

    data class R(val ok: Boolean, val msg: String)

    fun run(ctx: Context): R {
        if (!ShizukuShell.isRunning()) {
            return R(false, "Shizuku 没运行。先打开 Shizuku App 并点「启动」。")
        }
        if (!ShizukuShell.hasPermission()) {
            return R(false, "还没给 JobPilot 授权。点一下「申请 Shizuku 权限」。")
        }
        val comp = ctx.packageName + "/" + ctx.packageName + ".DumpService"
        val out1 = ShizukuShell.exec(
            ctx, "settings put secure enabled_accessibility_services $comp"
        )
        val out2 = ShizukuShell.exec(ctx, "settings put secure accessibility_enabled 1")
        Thread.sleep(1200)

        val now = runCatching {
            Settings.Secure.getString(ctx.contentResolver, "enabled_accessibility_services")
        }.getOrNull().orEmpty()
        val on = runCatching {
            Settings.Secure.getInt(ctx.contentResolver, "accessibility_enabled", 0)
        }.getOrDefault(0)

        return if (now.contains(ctx.packageName) && on == 1) {
            R(true, "已启用并锁定：$comp")
        } else {
            R(
                false,
                "写入没生效。\n当前值：${now.ifBlank { "(空)" }}\n总开关：$on\n" +
                    "输出：${(out1 + out2).take(120)}"
            )
        }
    }
}

/**
 * 简历解析：txt / docx / pdf。
 *
 * docx = zip 里的 word/document.xml，可靠。
 * pdf  = 解压 stream 后抽 Tj/TJ 文本，尽力而为；抽不出可读文字会明确报错。
 */
object ResumeParser {

    data class R(val ok: Boolean, val text: String, val msg: String)

    fun parse(ctx: Context, bytes: ByteArray?, name: String = ""): R {
        if (bytes == null || bytes.isEmpty()) return R(false, "", "文件是空的")
        val head = String(bytes, 0, minOf(8, bytes.size), Charsets.ISO_8859_1)

        return when {
            head.startsWith("%PDF") -> {
                var t = pdfBox(ctx, bytes)
                if (t.length < 100) t = pdf(bytes)
                if (t.length >= 100) R(true, t, "PDF 已解析 ${t.length} 字")
                else R(
                    false, "",
                    "这个 PDF 里的文字是压缩/图片形式的，抽不出来。\n" +
                        "请在 WPS/Word 里「另存为 .docx」或复制成文本再上传。"
                )
            }
            head.startsWith("PK") -> {
                val t = docx(bytes)
                if (t.length >= 100) R(true, t, "Word 已解析 ${t.length} 字")
                else R(false, "", "这个 docx 里没读到文字")
            }
            else -> {
                val t = String(bytes, Charsets.UTF_8).trim()
                if (t.length >= 100) R(true, t, "文本 ${t.length} 字")
                else R(false, "", "内容太短（${t.length} 字），不像是简历")
            }
        }
    }

    // ---------------------------------------------------------------- docx

    private fun docx(bytes: ByteArray): String = runCatching {
        java.util.zip.ZipInputStream(bytes.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == "word/document.xml") {
                    val xml = zip.readBytes().toString(Charsets.UTF_8)
                    return@runCatching xml
                        .replace(Regex("</w:p>"), "\n")
                        .replace(Regex("<[^>]+>"), "")
                        .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
                        .replace(Regex("\n{3,}"), "\n\n")
                        .trim()
                }
                entry = zip.nextEntry
            }
            ""
        }
    }.getOrDefault("")

    // ---------------------------------------------------------------- pdf

    /** PDFBox 抽文字：能处理中文 CID 字体，失败再退回手写版本 */
    private fun pdfBox(ctx: Context, bytes: ByteArray): String = runCatching {
        PDFBoxResourceLoader.init(ctx.applicationContext)
        PDDocument.load(bytes).use { doc ->
            PDFTextStripper().apply { sortByPosition = true }.getText(doc).trim()
        }
    }.getOrDefault("")

    private fun pdf(bytes: ByteArray): String = runCatching {
        val sb = StringBuilder()
        var i = 0
        while (i < bytes.size) {
            val s = indexOf(bytes, "stream", i)
            if (s < 0) break
            var start = s + 6
            if (start < bytes.size && bytes[start].toInt() == 13) start++
            if (start < bytes.size && bytes[start].toInt() == 10) start++
            val e = indexOf(bytes, "endstream", start)
            if (e < 0) break
            val raw = bytes.copyOfRange(start, e)
            val data = runCatching { inflate(raw) }.getOrDefault(raw)
            extractOps(data, sb)
            i = e + 9
        }
        val text = sb.toString()
        // 可读性检查：中文/ASCII 占比太低就当失败
        if (text.isEmpty()) return@runCatching ""
        val printable = text.count { it.code in 32..126 || it.code in 0x4E00..0x9FFF }
        if (printable.toDouble() / text.length < 0.5) "" else text
    }.getOrDefault("")

    private fun extractOps(data: ByteArray, sb: StringBuilder) {
        val s = String(data, Charsets.ISO_8859_1)
        val re = Regex("\\((?:\\\\.|[^()\\\\])*\\)")
        for (m in re.findAll(s)) {
            val raw = m.value.substring(1, m.value.length - 1)
            val t = raw.replace("\\(", "(").replace("\\)", ")")
                .replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "")
            if (t.any { it.code in 32..126 || it.code in 0x4E00..0x9FFF }) sb.append(t)
        }
        sb.append('\n')
    }

    private fun inflate(raw: ByteArray): ByteArray {
        val inf = Inflater()
        inf.setInput(raw)
        val out = ByteArrayOutputStream(raw.size * 4)
        val buf = ByteArray(8192)
        while (!inf.finished()) {
            val n = inf.inflate(buf)
            if (n <= 0) break
            out.write(buf, 0, n)
        }
        inf.end()
        return out.toByteArray()
    }

    private fun indexOf(hay: ByteArray, needle: String, from: Int): Int {
        val n = needle.toByteArray(Charsets.ISO_8859_1)
        var i = from
        while (i <= hay.size - n.size) {
            var j = 0
            while (j < n.size && hay[i + j] == n[j]) j++
            if (j == n.size) return i
            i++
        }
        return -1
    }
}
