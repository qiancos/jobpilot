package com.jobpilot.pad

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import rikka.shizuku.Shizuku

/** Shizuku 接线：检查授权 → 绑定 UserService → 执行命令 */
object ShizukuShell {

    @Volatile
    private var service: IShellService? = null

    private var pending: ((IShellService?) -> Unit)? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val s = if (binder != null && binder.pingBinder()) {
                IShellService.Stub.asInterface(binder)
            } else null
            service = s
            pending?.invoke(s)
            pending = null
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
        }
    }

    fun isRunning(): Boolean = runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    fun hasPermission(): Boolean = runCatching {
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }.getOrDefault(false)

    fun requestPermission(code: Int = 1001) {
        runCatching { Shizuku.requestPermission(code) }
    }

    fun ensure(context: Context, cb: (IShellService?) -> Unit) {
        service?.let { cb(it); return }
        if (!isRunning() || !hasPermission()) {
            cb(null)
            return
        }
        pending = cb
        runCatching {
            val args = Shizuku.UserServiceArgs(
                ComponentName(context.packageName, ShellService::class.java.name)
            )
                .daemon(false)
                .processNameSuffix("shell")
                .debuggable(BuildConfig.DEBUG)
                .version(1)
            Shizuku.bindUserService(args, connection)
        }.onFailure {
            pending = null
            cb(null)
        }
    }

    /** 同步执行（调用方负责放到子线程） */
    fun exec(context: Context, cmd: String): String {
        var out = "not ready"
        val latch = java.util.concurrent.CountDownLatch(1)
        ensure(context) { svc ->
            out = if (svc == null) "shizuku not ready"
            else runCatching { svc.exec(cmd) }.getOrElse { "EXEC FAIL: ${it.message}" }
            latch.countDown()
        }
        latch.await(15, java.util.concurrent.TimeUnit.SECONDS)
        return out
    }
}
