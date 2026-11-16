package com.jobpilot.pad

import android.os.IBinder

/**
 * 跑在 Shizuku 的 shell 进程里（uid 2000）。
 * 我们只借它执行 uiautomator / input / screencap 这几条命令。
 */
class ShellService : IShellService.Stub() {
    override fun exec(cmd: String): String {
        return try {
            // 合并 stderr，避免两条流互相阻塞、也避免"报错看不见"
            val pb = ProcessBuilder("sh", "-c", cmd)
            pb.redirectErrorStream(true)
            val p = pb.start()
            val out = p.inputStream.bufferedReader().readText()
            p.waitFor()
            out
        } catch (t: Throwable) {
            "ERR: ${t.javaClass.simpleName}: ${t.message}"
        }
    }
}

/** empty binder placeholder, keeps IDE happy about unused import */
internal fun IBinder.noop() = Unit
