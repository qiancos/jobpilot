package com.jobpilot.pad

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.jobpilot.pad.collect.AppCollector
import com.jobpilot.pad.collect.Collector
import com.jobpilot.pad.data.Db

/**
 * 环境自检壳（M0 用）。
 *
 * 只做三件事：
 *   1. 用桌面 UA 打开 BOSS 网页版，看能不能登录、会不会跳安全页；
 *   2. 点「运行自检」注入 JS，把 navigator.webdriver / 岗位卡片数 / 选择器命中数打出来；
 *   3. 什么都不自动点——这一版绝不可能误发消息。
 */
class MainActivity : Activity() {

    private lateinit var web: WebView
    private lateinit var status: TextView
    private lateinit var a11yChip: TextView
    private var panel: FloatingPanel? = null
    private var awaitingOverlay = false
    @Volatile private var scanning = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置页与面板共用这份配置（档位、日上限、条间隔、单批上限）
        PanelState.load(this)

        // 打开 WebView 远程调试：PC 上 chrome://inspect 可直接看这个壳里的页面
        WebView.setWebContentsDebuggingEnabled(true)

        runCatching {
            rikka.shizuku.Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#111827"))
            setPadding(32, 20, 32, 20)
        }

        status = TextView(this).apply {
            text = "就绪 — 页面加载后点右侧「运行自检」"
            setTextColor(Color.WHITE)
            textSize = 13f
        }

        val probeBtn = Button(this).apply {
            text = "自检"
            setOnClickListener { runProbe() }
        }

        val reloadBtn = Button(this).apply {
            text = "重载"
            setOnClickListener { web.reload() }
        }

        val panelBtn = Button(this).apply {
            text = "面板"
            setOnClickListener { togglePanel() }
        }

        val shBtn = Button(this).apply {
            text = "SH"
            // 延迟 8 秒执行：给用户时间切到 BOSS App，这样 dump 到的就是 BOSS
            setOnClickListener { doShellTest(8000L) }
        }

        val collectBtn = Button(this).apply {
            text = "采集"
            setOnClickListener { startCollect() }
        }

        val listBtn = Button(this).apply {
            text = "清单"
            setOnClickListener {
                // 进清单前先把悬浮面板摘掉，否则它盖在列表上
                panel?.destroy()
                startActivity(Intent(this@MainActivity, CockpitActivity::class.java))
            }
        }

        // 无障碍状态芯片：没开的时候是琥珀色，点一下跳设置
        a11yChip = TextView(this).apply {
            textSize = 11f
            setTextColor(Color.WHITE)
            setPadding(dp(9), dp(5), dp(9), dp(5))
            setOnClickListener {
                runCatching {
                    startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
            }
        }

        val menuBtn = TextView(this).apply {
            text = "☰"
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(dp(10), dp(2), dp(10), dp(2))
            setOnClickListener {
                startActivity(Intent(this@MainActivity, HomeActivity::class.java))
            }
        }

        bar.addView(menuBtn)
        bar.addView(a11yChip)
        bar.addView(
            status,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
        bar.addView(collectBtn)
        bar.addView(listBtn)
        bar.addView(probeBtn)
        bar.addView(reloadBtn)
        bar.addView(panelBtn)
        bar.addView(shBtn)

        web = WebView(this)
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            userAgentString = DESKTOP_UA
        }

        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                status.text = "已加载 " + (url ?: "").take(70)
            }
        }

        root.addView(
            bar,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        root.addView(
            web,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        )

        setContentView(root)
        // 支持 adb 指定起始页：
        //   adb shell am start -n com.jobpilot.pad/.MainActivity -e url "https://..."
        // 这样验证任何页面都不用重新打包。
        val startUrl = intent?.getStringExtra("url")?.takeIf { it.isNotBlank() } ?: JOBS_URL
        Log.i(TAG, "load: $startUrl")
        web.loadUrl(startUrl)
    }

    private fun runProbe() {
        status.text = "自检中…"
        web.evaluateJavascript(PROBE_JS) { result ->
            val text = (result ?: "null").replace("\\u003C", "<").trim('"')
            Log.i(TAG, "PROBE=$text")
            // 诊断输出较长，写文件比挤在顶栏可靠
            runCatching {
                val f = java.io.File(getExternalFilesDir(null), "probe.txt")
                f.writeText(text)
                status.text = "诊断已写到 ${f.absolutePath}"
            }.onFailure {
                status.text = text.take(400)
            }
        }
    }

    // ------------------------------------------------------------ Shizuku 通道

    private val shizukuPermissionListener =
        rikka.shizuku.Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                runOnUiThread { status.text = "Shizuku 已授权，读取中…" }
                doShellTest()
            } else {
                runOnUiThread { status.text = "Shizuku 授权被拒绝" }
            }
        }

    private fun doShellTest(delayMs: Long = 0L) {
        if (!ShizukuShell.isRunning()) {
            status.text = "Shizuku 服务没在跑（需要先用 adb 或无线调试启动它）"
            return
        }
        if (!ShizukuShell.hasPermission()) {
            status.text = "正在请求 Shizuku 授权…"
            ShizukuShell.requestPermission(1001)
            return
        }
        status.text = if (delayMs > 0) "${delayMs / 1000} 秒后读取前台界面…" else "通过 Shizuku 读取前台界面…"
        Thread {
            if (delayMs > 0) Thread.sleep(delayMs)
            // 关键：从 App 里 spawn 出来的进程会继承 CLASSPATH / BOOTCLASSPATH，
            // uiautomator 脚本内部又要用 app_process 启动，继承的 CLASSPATH 会让它拿不到 root node。
            // 必须先把这些环境变量清掉。
            val clean = "unset CLASSPATH; unset BOOTCLASSPATH; unset LD_LIBRARY_PATH; " +
                "export ANDROID_DATA=/data; export ANDROID_ROOT=/system; " +
                "export PATH=/system/bin:/system/xbin:/system/sbin; "
            val xml = ShizukuShell.exec(
                this,
                clean +
                    "uiautomator dump /data/local/tmp/jp.xml 2>&1; echo EXIT=\$?; " +
                    "ls -l /data/local/tmp/jp.xml 2>&1; cat /data/local/tmp/jp.xml 2>/dev/null"
            )
            val texts = Regex("text=\"([^\"]{2,24})\"")
                .findAll(xml).map { it.groupValues[1] }.toList()
            val pkg = Regex("package=\"([^\"]+)\"").find(xml)?.groupValues?.get(1) ?: "?"
            runCatching {
                java.io.File(getExternalFilesDir(null), "shizuku.xml").writeText(xml)
            }
            val summary = "pkg=$pkg 节点文本=${texts.size}\n" +
                texts.take(14).joinToString(" / ")
            runOnUiThread {
                status.text = summary.replace("\n", "  ")
                PanelState.log("Shizuku 读取成功：${texts.size} 条文本，pkg=$pkg")
                Toast.makeText(this, "已读到 ${texts.size} 条文本", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    // ------------------------------------------------------------ 采集

    private var collecting = false

    private fun startCollect() {
        if (collecting) return
        collecting = true
        // 前台是 BOSS App → 走无障碍读控件树；否则退回网页 DOM
        if (AppCollector.currentPackage() == AppCollector.BOSS_PKG) {
            collectFromApp()
        } else {
            collectFromWeb()
        }
    }

    /** 从 BOSS 原生 App 读卡片（必须在 BOSS 处于前台时调用） */
    private fun collectFromApp() {
        val db = Db.get(this)
        status.text = "从 BOSS App 读取卡片…"
        PanelState.log("开始采集（BOSS App）")
        Thread {
            val (jobs, dump) = AppCollector.collect("app")
            runCatching {
                java.io.File(getExternalFilesDir(null), "cards.txt")
                    .writeText("package=${AppCollector.currentPackage()}\n$dump")
            }
            val n = db.upsertJobs(jobs)
            val total = db.count()
            collecting = false
            runOnUiThread {
                status.text = "App 采集：读到 ${jobs.size} 张卡，新增 $n，库内 $total"
                PanelState.log("App 采集完成 读到${jobs.size} 新增$n 共$total")
                updatePanel()
            }
        }.start()
    }

    private fun collectFromWeb() {
        status.text = "采集中：滚动加载 + 解析卡片…"
        PanelState.log("开始采集（网页）")
        Collector.collect(web, "Java", rounds = 3) { jobs ->
            val n = Db.get(this).upsertJobs(jobs)
            val total = Db.get(this).count()
            collecting = false
            status.text = "采集完成：抓到 ${jobs.size} 条，新增 $n 条，库内 $total 条"
            PanelState.log("采集完成 +$n（共 $total）")
            Toast.makeText(this, "新增 $n 条，清单里共 $total 条", Toast.LENGTH_LONG).show()
            updatePanel()
        }
    }

    private fun updatePanel() {
        panel?.updateUi()
    }

    // ------------------------------------------------------------ 悬浮面板

    private var lastToggleAt = 0L

    private fun togglePanel() {
        val now = System.currentTimeMillis()
        if (now - lastToggleAt < 600) return   // 防连点：一次点击只切一次
        lastToggleAt = now

        val p = ensurePanel()
        if (p.isShowing) {
            p.hide()
            status.text = "面板已收起"
            return
        }
        if (Settings.canDrawOverlays(this)) {
            val ok = p.show()
            status.text = if (ok) "面板已开启" else "面板启动失败"
        } else {
            awaitingOverlay = true
            Toast.makeText(this, "请允许「显示在其他应用上层」", Toast.LENGTH_LONG).show()
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        updateA11yChip()
        if (awaitingOverlay && Settings.canDrawOverlays(this)) {
            awaitingOverlay = false
            val ok = panel?.show() ?: false
            status.text = if (ok) "面板已开启" else "面板启动失败"
            return
        }
        // 上次开着面板 → 重新进 App 自动恢复（位置/大小/参数都在 prefs 里）
        val prefs = getSharedPreferences("jobpilot_panel", MODE_PRIVATE)
        if (prefs.getBoolean("enabled", false) && Settings.canDrawOverlays(this)) {
            val p = ensurePanel()
            if (!p.isShowing) {
                p.show()
                status.text = "面板已恢复"
            }
        }
    }

    private fun ensurePanel(): FloatingPanel =
        panel ?: FloatingPanel(this).also {
            it.onCollect = { collectFromApp() }
            it.onScanList = { scanBossList() }
            it.onStart = { startBatchFromPanel() }
            it.onAbort = { com.jobpilot.pad.exec.BatchRunner.stop() }
            panel = it
        }

    /**
     * 面板上的「开始」：真正起批次。
     * 清单取「已勾选」的岗位；没勾选就取全部，让 Safety 去拦不该投的。
     */
    private fun startBatchFromPanel() {
        if (PanelState.phase == PanelState.Phase.PAUSED) {
            PanelState.togglePause()
            return
        }
        if (PanelState.phase == PanelState.Phase.RUNNING) return
        if (com.jobpilot.pad.A11yBridge.service == null) {
            PanelState.log("无障碍未连接，先去系统设置里打开 JobPilot 无障碍服务")
            android.widget.Toast.makeText(this, "请先开启无障碍服务", android.widget.Toast.LENGTH_LONG).show()
            return
        }
        val db = com.jobpilot.pad.data.Db.get(this)
        val selected = db.listJobs(onlySelected = true)
        val all = if (selected.isNotEmpty()) selected else db.listJobs()
        val list = all.take(PanelState.batchCap)
        if (list.isEmpty()) {
            PanelState.log("清单是空的：先切到 BOSS 职位列表页点「采集」")
            android.widget.Toast.makeText(this, "清单为空，先采集岗位", android.widget.Toast.LENGTH_LONG).show()
            return
        }
        PanelState.todayCount = runCatching { db.countDeliveredToday() }.getOrDefault(0)
        PanelState.log("起批次：${list.size} 家（${if (selected.isNotEmpty()) "已勾选" else "全部"}）")
        Thread {
            runCatching {
                com.jobpilot.pad.exec.BatchRunner.run(
                    list, db,
                    { job ->
                        com.jobpilot.pad.exec.GreetingGen.build(
                            job, com.jobpilot.pad.exec.ResumeProfile.load(this)
                        )
                    }
                ) { /* 进度已由 BatchRunner 写进 PanelState */ }
            }.onFailure { e ->
                PanelState.log("批次异常中断：${e.message}")
                PanelState.finishBatch()
            }
        }.start()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    /** 面板上的「AI 扫列表」：BOSS 停在职位列表页时，逐个点进详情读 JD 打分 */
    private fun scanBossList() {
        if (scanning) return
        scanning = true
        PanelState.log("开始扫列表：逐个点进详情读 JD…")
        Thread {
            val db = com.jobpilot.pad.data.Db.get(this)
            val ok = com.jobpilot.pad.exec.ListScanner.scanAndScore(this, db, 8) { p ->
                PanelState.log("${p.index + 1}/${p.total} ${p.company}｜${p.phase}｜成功 ${p.ok} 失败 ${p.fail}")
            }
            PanelState.log("扫列表结束：成功 $ok 条")
            scanning = false
        }.start()
    }

    private fun updateA11yChip() {
        if (!::a11yChip.isInitialized) return
        val on = a11yEnabled(this)
        a11yChip.text = if (on) "无障碍 ✓" else "无障碍未开"
        a11yChip.setBackgroundColor(if (on) 0xFF16A34A.toInt() else 0xFFD97706.toInt())
    }

    override fun onDestroy() {
        panel?.destroy()
        runCatching {
            rikka.shizuku.Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener)
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (this::web.isInitialized && web.canGoBack()) {
            web.goBack()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    companion object {
        private const val TAG = "JobPilot"

        private const val JOBS_URL =
            "https://www.zhipin.com/web/geek/jobs?query=Java&city=101280100"

        /** 桌面 UA：平板横屏时网页版布局才完整 */
        private const val DESKTOP_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"

        private val PROBE_JS: String = """
            (function () {
              try {
                var w = document.querySelector('div.job-card-wrap');
                if (!w) return 'NO_CARD';
                var s = [];
                var all = w.querySelectorAll('*');
                for (var i = 0; i < all.length && s.length < 45; i++) {
                  var e = all[i];
                  var t = (e.innerText || '').replace(/\s+/g, ' ').trim();
                  if (t.length > 0 && t.length < 30) {
                    s.push(e.tagName.toLowerCase() + '.' + String(e.className || '-') + '=' + t);
                  }
                }
                return s.join(' ¦ ');
              } catch (e) {
                return 'ERR: ' + e.message;
              }
            })()
        """.trimIndent()
    }
}
