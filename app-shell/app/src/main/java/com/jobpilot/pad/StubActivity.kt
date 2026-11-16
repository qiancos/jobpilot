package com.jobpilot.pad

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobpilot.pad.data.Db
import com.jobpilot.pad.exec.LlmClient
import com.jobpilot.pad.exec.Log
import com.jobpilot.pad.exec.ResumeParser
import com.jobpilot.pad.exec.ResumeProfile
import com.jobpilot.pad.exec.ResumeStore
import com.jobpilot.pad.exec.SecureFix
import com.jobpilot.pad.ui.NavDest
import com.jobpilot.pad.ui.NavRail
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 话术 / 台账 / 设置 */
class StubActivity : ComponentActivity() {

    /** 侧栏切换会用 REORDER_TO_FRONT，必须能在同一个实例里改页面 */
    private val destState = mutableStateOf(NavDest.SKILL)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        destState.value = parseDest(intent)
        PanelState.load(this)

        setContent {
            MaterialTheme {
                Row(Modifier.fillMaxSize().background(BG)) {
                    NavRail(destState.value)
                    if (destState.value == NavDest.SETTINGS) {
                        SettingsScreen(Modifier.weight(1f))
                    } else {
                        StubScreen(destState.value, Modifier.weight(1f))
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        destState.value = parseDest(intent)
    }

    private fun parseDest(intent: Intent?): NavDest = runCatching {
        NavDest.valueOf(intent?.getStringExtra("dest") ?: NavDest.SKILL.name)
    }.getOrDefault(NavDest.SKILL)
}

// ---------------------------------------------------------------- 话术 / 台账

@Composable
private fun StubScreen(dest: NavDest, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val db = remember { Db.get(ctx) }

    val total = remember { db.count() }
    val selected = remember { db.countSelected() }
    val applied = remember { db.countApplications() }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 22.dp)
    ) {
        Text(dest.label, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = INK)
        Spacer(Modifier.height(6.dp))

        when (dest) {
            NavDest.SKILL -> {
                Text("话术工作台 · M3", fontSize = 12.sp, color = SUB)
                Spacer(Modifier.height(18.dp))
                InfoCard("话术怎么来的", "按「你的简历 ∩ 岗位 JD」生成，一条一版；发之前过 9 条闸门")
                InfoCard("当前数据", "已选 $selected 个岗位等待生成话术")
                InfoCard("依赖", "需要先上传简历——在「设置」里传")
            }

            NavDest.LEDGER -> {
                Text("台账与复盘 · M4", fontSize = 12.sp, color = SUB)
                Spacer(Modifier.height(18.dp))
                InfoCard("漏斗", "候选 $total → 审批 $selected → 送达 $applied")
                InfoCard("明细", "每条投递记录了点击/进会话/送达三个时间戳和失败原因")
                InfoCard("导出", "在「设置 → 数据与隐私」里导出 CSV")
            }

            else -> Unit
        }
        Spacer(Modifier.height(30.dp))
    }
}

// ---------------------------------------------------------------- 设置与自检

@Composable
private fun SettingsScreen(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val db = remember { Db.get(ctx) }
    var refresh by remember { mutableIntStateOf(0) }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        SettingsHeader(refresh)
        Spacer(Modifier.height(16.dp))

        BoxWithConstraints(Modifier.fillMaxWidth()) {
            // 原型是 1fr 1fr 392px；竖屏放不下三栏时把悬浮面板挪到下一行
            val wide = maxWidth >= 900.dp
            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        TierCard(refresh) { refresh++ }
                        PrivacyCard(refresh) { refresh++ }
                        LlmSection(refresh) { refresh++ }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        SelfCheckCard(refresh) { refresh++ }
                        VersionCard()
                        ResumeSection(refresh) { refresh++ }
                    }
                    Column(Modifier.width(392.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        PanelCard(refresh) { refresh++ }
                        LogCard(refresh) { refresh++ }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            TierCard(refresh) { refresh++ }
                            PrivacyCard(refresh) { refresh++ }
                            LlmSection(refresh) { refresh++ }
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            SelfCheckCard(refresh) { refresh++ }
                            VersionCard()
                            ResumeSection(refresh) { refresh++ }
                        }
                    }
                    PanelCard(refresh) { refresh++ }
                    LogCard(refresh) { refresh++ }
                }
            }
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun SettingsHeader(refresh: Int) {
    val ctx = LocalContext.current
    val a11y = remember(refresh) { a11yEnabled(ctx) }
    val battery = remember(refresh) { ignoringBattery(ctx) }
    val issues = (if (a11y) 0 else 1) + (if (battery) 0 else 1)
    val dm = ctx.resources.displayMetrics

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("设置与自检", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = INK)
            Text(
                "${Build.MODEL} · Android ${Build.VERSION.RELEASE} · JobPilot v${BuildConfig.VERSION_NAME} · " +
                    "${dm.widthPixels}×${dm.heightPixels} @${dm.densityDpi}dpi",
                fontSize = 11.5.sp, color = SUB, modifier = Modifier.padding(top = 3.dp)
            )
        }
        Pill(if (issues == 0) "全部正常" else "$issues 项待处理", issues == 0)
        Spacer(Modifier.width(10.dp))
        OutlinedButton(
            onClick = { exportDiag(ctx) },
            shape = RoundedCornerShape(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
        ) { Text("导出诊断包", fontSize = 12.sp) }
    }
}

@Composable
private fun TierCard(refresh: Int, onChanged: () -> Unit) {
    val ctx = LocalContext.current
    val db = remember { Db.get(ctx) }
    val today = remember(refresh) { db.countDeliveredToday() }
    val limit = PanelState.dailyLimit

    SCard("执行档位", "改动立即写入 PanelState", trailing = { Pill("即时生效", true) }) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PanelState.tierNames.forEachIndexed { i, name ->
                val on = PanelState.tier == i
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (on) GREEN_SOFT else Color.White)
                        .border(
                            if (on) 1.5.dp else 1.dp,
                            if (on) GREEN else LINE2,
                            RoundedCornerShape(11.dp)
                        )
                        .clickable {
                            PanelState.applyTier(i)
                            PanelState.persist(ctx)
                            onChanged()
                        }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = if (on) GREEN else INK
                    )
                    Text(
                        PanelState.tierLimits[i].toString(),
                        fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        color = if (on) GREEN else INK,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        if (i == 1) "条 / 日 · 默认" else "条 / 日",
                        fontSize = 9.5.sp, color = if (on) GREEN else FAINT
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("每日上限", fontSize = 11.5.sp, color = SUB)
            Spacer(Modifier.weight(1f))
            Text("$limit 条", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = INK)
        }
        Slider(
            value = limit.toFloat(),
            onValueChange = {
                PanelState.dailyLimit = it.toInt().coerceIn(5, 40)
            },
            valueRange = 5f..40f,
            steps = 33,
            onValueChangeFinished = {
                PanelState.persist(ctx)
                PanelState.log("每日上限设为 ${PanelState.dailyLimit} 条")
                onChanged()
            },
            colors = SliderDefaults.colors(
                thumbColor = GREEN, activeTrackColor = GREEN, inactiveTrackColor = LINE2
            )
        )
        Row {
            Text("5", fontSize = 10.sp, color = FAINT)
            Spacer(Modifier.weight(1f))
            Text("40", fontSize = 10.sp, color = FAINT)
        }

        Divider()
        InfoRow("条间隔", "${PanelState.gapMin} – ${PanelState.gapMax} 秒")
        InfoRow("单批上限", "${PanelState.batchCap} 家")
        InfoRow(
            "夜间静默",
            String.format("%02d:00 – %02d:00", PanelState.nightStart, PanelState.nightEnd)
        )

        Divider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("今日已用", fontSize = 11.5.sp, color = SUB)
            Spacer(Modifier.weight(1f))
            Text("$today / $limit", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = INK)
        }
        Spacer(Modifier.height(7.dp))
        Bar(
            if (limit > 0) (today.toFloat() / limit).coerceIn(0f, 1f) else 0f,
            GREEN, 40.dp
        )
        Text(
            "超限直接拒绝新批次；不会静默截断已在跑的批次。",
            fontSize = 10.sp, color = FAINT, modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun SelfCheckCard(refresh: Int, onChanged: () -> Unit) {
    val ctx = LocalContext.current
    val cfg = LocalConfiguration.current
    val dm = ctx.resources.displayMetrics

    val a11y = remember(refresh) { a11yEnabled(ctx) }
    val bossPkg = remember(refresh) {
        runCatching { A11yBridge.service?.rootInActiveWindow?.packageName?.toString() }.getOrNull()
    }
    val battery = remember(refresh) { ignoringBattery(ctx) }

    SCard("环境自检", "启动时 + 每 30 分钟", trailing = {
        Pill(
            "${listOf(a11y, battery).count { it }} / 2 通过",
            a11y && battery
        )
    }) {
        CheckRow("无障碍服务", if (a11y) "已开启" else "未开启", if (a11y) GREEN else RED)
        AmberHint("vivo 每次更新 App 会重置授权。若变灰，请到「设置 → 无障碍 → 已下载的服务 → JobPilot」重新开启。")
        CheckRow(
            "BOSS 前台",
            if (bossPkg == "com.hpbr.bosszhipin") "com.hpbr.bosszhipin" else "未在前台",
            if (bossPkg == "com.hpbr.bosszhipin") GREEN else FAINT
        )
        CheckRow("控件命中率", "暂无统计（首批投递后累计）", FAINT)
        CheckRow(
            "屏幕与方向",
            "${dm.heightPixels}×${dm.widthPixels} " +
                if (cfg.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) "横屏" else "竖屏",
            GREEN
        )
        CheckRow(
            "应用保活",
            if (battery) "已加入电池白名单" else "未加入白名单",
            if (battery) GREEN else AMBER
        )

        Divider()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallButton("重新自检", Modifier.weight(1f)) { onChanged() }
            SmallButton("Shizuku 修复", Modifier.weight(1f)) {
                Thread {
                    val r = SecureFix.run(ctx)
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        AlertDialog.Builder(ctx)
                            .setTitle(if (r.ok) "已用 Shizuku 修好" else "没修上")
                            .setMessage(r.msg)
                            .setPositiveButton("知道了") { _, _ -> onChanged() }
                            .show()
                    }
                }.start()
            }
            SmallButton("去系统设置", Modifier.weight(1f)) {
                runCatching {
                    ctx.startActivity(
                        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }
        }
    }
}

@Composable
private fun VersionCard() {
    SCard("版本与选择器", "改版只改一处") {
        InfoRow("App", "v${BuildConfig.VERSION_NAME} · ${BuildConfig.VERSION_CODE}", INK)
        InfoRow("无障碍控件 id", "内置 · 随版本", INK)
        InfoRow("网页选择器", "未接入（网页降级还没做）", FAINT)
        InfoRow("闸门", "gate v1 · 9 条", INK)
        Divider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("熔断条件", fontSize = 10.5.sp, color = FAINT)
            Spacer(Modifier.weight(1f))
            Text("连续 ${PanelState.BREAKER_THRESHOLD} 次未送达即停", fontSize = 12.sp, color = INK)
        }
    }
}

@Composable
private fun PrivacyCard(refresh: Int, onChanged: () -> Unit) {
    val ctx = LocalContext.current
    val db = remember { Db.get(ctx) }
    val cfg = remember(refresh) { LlmClient.cfg(ctx) }
    val sizeText = remember(refresh) {
        val kb = Db.fileBytes(ctx) / 1024.0
        if (kb > 1024) String.format("%.1f MB", kb / 1024) else String.format("%.0f KB", kb)
    }
    val keyMask = remember(refresh) {
        if (cfg.key.length <= 8) "未配置"
        else cfg.key.take(5) + "****" + cfg.key.takeLast(4)
    }

    SCard("数据与隐私", null, trailing = { Pill("全本地", true) }) {
        DotRow("台账 SQLite 本地存储", sizeText)
        DotRow("简历与附件不出设备", "0 上传")
        DotRow("LLM 只传脱敏 JD + 技能摘要", "已脱敏")
        DotRow("LLM Key", keyMask)
        Divider()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallButton("导出 CSV", Modifier.weight(1f)) {
                val dir = ctx.getExternalFilesDir(null) ?: ctx.filesDir
                val f = File(dir, "jobpilot-ledger-${stamp()}.csv")
                runCatching { f.writeText(db.ledgerCsv()) }
                    .onSuccess {
                        Toast.makeText(ctx, "已导出：${f.absolutePath}", Toast.LENGTH_LONG).show()
                    }
                    .onFailure {
                        Toast.makeText(ctx, "导出失败：${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            SmallButton("清空台账", Modifier.weight(1f), danger = true) {
                AlertDialog.Builder(ctx)
                    .setTitle("清空投递台账？")
                    .setMessage("会删掉全部投递记录和公司联系记录，岗位清单保留。这个操作不能撤销。")
                    .setPositiveButton("清空") { _, _ ->
                        db.clearLedger()
                        Toast.makeText(ctx, "台账已清空", Toast.LENGTH_SHORT).show()
                        onChanged()
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        }
    }
}

@Composable
private fun PanelCard(refresh: Int, onChanged: () -> Unit) {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("jobpilot_panel", Context.MODE_PRIVATE) }
    var autoShow by remember(refresh) { mutableStateOf(prefs.getBoolean("enabled", false)) }
    var collapsed by remember(refresh) { mutableStateOf(prefs.getBoolean("collapsed", false)) }
    var alpha by remember(refresh) {
        mutableIntStateOf((prefs.getFloat("alpha", 0.92f) * 100).toInt().coerceIn(40, 100))
    }

    SCard("悬浮面板", "三种形态") {
        // 展开态
        PreviewBox("展开态", "执行中") {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0B1522))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF22E2D8)))
                    Text(
                        "JobPilot 执行中", fontSize = 11.sp, color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    Text("— ✕", fontSize = 10.sp, color = Color(0xFF7E93AB))
                }
                Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.Bottom) {
                    Text("6", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("/ 8", fontSize = 11.sp, color = Color(0xFF5B718C), modifier = Modifier.padding(start = 3.dp, bottom = 2.dp))
                    Text(
                        "某科技 · 第 4/11 步", fontSize = 9.5.sp, color = Color(0xFF8FA3BC),
                        modifier = Modifier.padding(start = 8.dp, bottom = 3.dp)
                    )
                }
                Spacer(Modifier.height(7.dp))
                Bar(0.75f, Color(0xFF22E2D8), 4.dp, Color(0xFF18283A))
                Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MiniAction("暂停", Color(0xFF1B2A3D), Color(0xFFC9D6E6), Modifier.weight(1f))
                    MiniAction("终止", Color.Transparent, Color(0xFFFF8A8A), Modifier.weight(1f), Color(0xFF4A2A2A))
                }
            }
        }

        // 折叠态
        PreviewBox("折叠态", "拖到边缘吸附") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0B1522))
                        .border(1.dp, Color(0xFF22334A), CircleShape),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("6/8", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("6 送达", fontSize = 8.sp, color = Color(0xFF7E93AB))
                }
                Text(
                    "点击展开；长按拖动；执行中显示进度，空闲时显示今日数。",
                    fontSize = 10.5.sp, color = FAINT, lineHeight = 16.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }

        // 熔断态
        PreviewBox("熔断态", "已停止", warn = true) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2A1114))
                    .border(1.dp, Color(0xFF5A2A2A), RoundedCornerShape(10.dp))
                    .padding(horizontal = 11.dp, vertical = 10.dp)
            ) {
                Text("连续 3 次未送达，已停止", fontSize = 10.5.sp, color = Color(0xFFFFD5D5), fontWeight = FontWeight.SemiBold)
                Text(
                    "已停止一切点击 · 不会继续发送", fontSize = 9.sp, color = Color(0xFFD79A9A),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Text(
                "异常即停，宁可当天不投；熔断原因写进运行日志。",
                fontSize = 10.sp, color = FAINT, modifier = Modifier.padding(top = 8.dp)
            )
        }

        Divider()

        // 三个开关
        ToggleRow("自动弹出", autoShow) {
            autoShow = it
            prefs.edit().putBoolean("enabled", it).apply()
            onChanged()
        }
        ToggleRow("默认折叠", collapsed) {
            collapsed = it
            prefs.edit().putBoolean("collapsed", it).apply()
            onChanged()
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("浮层透明度", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = INK)
            Spacer(Modifier.weight(1f))
            Text("$alpha%", fontSize = 10.5.sp, color = FAINT)
        }
        Slider(
            value = alpha.toFloat(),
            onValueChange = { alpha = it.toInt().coerceIn(40, 100) },
            valueRange = 40f..100f,
            steps = 59,
            onValueChangeFinished = {
                prefs.edit().putFloat("alpha", alpha / 100f).apply()
                onChanged()
            },
            colors = SliderDefaults.colors(
                thumbColor = GREEN, activeTrackColor = GREEN, inactiveTrackColor = LINE2
            )
        )
        Text(
            "透明度与折叠状态在下次打开面板时生效。",
            fontSize = 10.sp, color = FAINT
        )
    }
}

// ---------------------------------------------------------------- 设置页小组件

@Composable
private fun SCard(
    title: String,
    sub: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = INK)
            if (sub != null) {
                Text(
                    sub, fontSize = 10.sp, color = FAINT,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            trailing?.invoke()
        }
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun Pill(text: String, good: Boolean) {
    Text(
        text,
        fontSize = 10.5.sp,
        color = if (good) GREEN else AMBER,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (good) GREEN_SOFT else AMBER_SOFT)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    )
}

@Composable
private fun Divider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .height(1.dp)
            .background(LINE2)
    )
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color = INK) {
    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 10.5.sp, color = FAINT)
        Spacer(Modifier.weight(1f))
        Text(value, fontSize = 12.sp, color = valueColor)
    }
}

@Composable
private fun DotRow(label: String, value: String) {
    Row(
        Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(TEAL2))
        Text(
            label, fontSize = 11.5.sp, color = INK,
            modifier = Modifier.padding(start = 8.dp).weight(1f)
        )
        Text(value, fontSize = 10.sp, color = FAINT)
    }
}

@Composable
private fun CheckRow(label: String, value: String, valueColor: Color) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(LINE2))
        Text(
            label, fontSize = 12.sp, color = INK,
            modifier = Modifier.padding(start = 8.dp).weight(1f)
        )
        Text(value, fontSize = 10.sp, color = valueColor)
    }
}

@Composable
private fun AmberHint(text: String) {
    Text(
        text,
        fontSize = 10.sp, color = Color(0xFF8A6A22), lineHeight = 16.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AMBER_SOFT)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    )
}

@Composable
private fun SmallButton(
    text: String,
    modifier: Modifier = Modifier,
    danger: Boolean = false,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(10.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp)
    ) {
        Text(
            text, fontSize = 11.5.sp,
            color = if (danger) RED else INK
        )
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = INK)
        Spacer(Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = GREEN,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCBD5E1)
            )
        )
    }
}

@Composable
private fun PreviewBox(
    label: String,
    pill: String,
    warn: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (warn) RED_SOFT else Color(0xFFF7F9FC))
            .border(
                1.dp,
                if (warn) Color(0xFFF3C9C9) else LINE2,
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 10.5.sp, color = FAINT)
            Spacer(Modifier.weight(1f))
            Pill(pill, !warn)
        }
        Spacer(Modifier.height(9.dp))
        content()
    }
}

@Composable
private fun MiniAction(
    text: String,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier,
    stroke: Color? = null,
) {
    Box(
        modifier
            .height(24.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(bg)
            .then(
                if (stroke != null) Modifier.border(1.dp, stroke, RoundedCornerShape(5.dp))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 9.sp, color = fg)
    }
}

@Composable
private fun Bar(
    fraction: Float,
    color: Color,
    height: androidx.compose.ui.unit.Dp,
    track: Color = LINE2,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(track)
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(color)
        )
    }
}

// ---------------------------------------------------------------- 诊断 / 大模型 / 简历 / 日志

private fun exportDiag(ctx: Context) {
    val text = runCatching { Log.read(ctx) }.getOrDefault("")
    val dir = ctx.getExternalFilesDir(null) ?: ctx.filesDir
    val f = File(dir, "jobpilot-diag-${stamp()}.txt")
    runCatching { f.writeText(text) }
        .onSuccess { Toast.makeText(ctx, "已导出：${f.absolutePath}", Toast.LENGTH_LONG).show() }
        .onFailure { Toast.makeText(ctx, "导出失败：${it.message}", Toast.LENGTH_SHORT).show() }
}

private fun stamp(): String =
    SimpleDateFormat("MMdd-HHmm", Locale.CHINA).format(Date())

private fun ignoringBattery(ctx: Context): Boolean = runCatching {
    val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
    pm.isIgnoringBatteryOptimizations(ctx.packageName)
}.getOrDefault(false)

@Composable
private fun LogCard(refresh: Int, onChanged: () -> Unit) {
    val ctx = LocalContext.current
    val n = remember(refresh) { Log.read(ctx).lines().size }
    SCard("诊断日志", "files/jobpilot.log") {
        Text("$n 行", fontSize = 12.sp, color = SUB)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallButton("查看", Modifier.weight(1f)) {
                val txt = Log.tail(ctx, 120).ifBlank { "（还没有日志）" }
                AlertDialog.Builder(ctx)
                    .setTitle("最近 120 行")
                    .setMessage(txt)
                    .setPositiveButton("知道了", null)
                    .show()
            }
            SmallButton("清空", Modifier.weight(1f)) { Log.clear(ctx); onChanged() }
        }
    }
}

@Composable
private fun LlmSection(refresh: Int, onChanged: () -> Unit) {
    val ctx = LocalContext.current
    val cfg = remember(refresh) { LlmClient.cfg(ctx) }
    var key by remember(refresh) { mutableStateOf(cfg.key) }
    var base by remember(refresh) { mutableStateOf(cfg.baseUrl) }
    var model by remember(refresh) { mutableStateOf(cfg.model) }
    var testing by remember(refresh) { mutableStateOf(false) }

    SCard(
        "大模型",
        if (cfg.ready) "已配置：${cfg.model}" else "没配 Key —— 只能用规则版",
        trailing = { Pill(if (cfg.ready) "已就绪" else "未配置", cfg.ready) }
    ) {
        Field("API Key", key, "sk-...") { key = it }
        Spacer(Modifier.height(6.dp))
        Field("接口地址", base, "https://api.deepseek.com/v1") { base = it }
        Spacer(Modifier.height(6.dp))
        Field("模型", model, "deepseek-flash") { model = it }

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallButton("保存", Modifier.weight(1f)) {
                LlmClient.save(ctx, LlmClient.Cfg(key, base, model))
                onChanged()
                Toast.makeText(ctx, "已保存", Toast.LENGTH_SHORT).show()
            }
            SmallButton(if (testing) "测试中…" else "测试连接", Modifier.weight(1f)) {
                LlmClient.save(ctx, LlmClient.Cfg(key, base, model))
                testing = true
                Thread {
                    val r = LlmClient.ping(ctx)
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        testing = false
                        AlertDialog.Builder(ctx)
                            .setTitle(if (r.startsWith("模型=")) "连接正常" else "连接失败")
                            .setMessage(r)
                            .setPositiveButton("知道了", null)
                            .show()
                    }
                }.start()
            }
        }
        Text(
            "默认接 DeepSeek（OpenAI 兼容）。Key 只存在本机，不上传。",
            fontSize = 10.sp, color = FAINT, lineHeight = 15.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun Field(label: String, value: String, hint: String, onChange: (String) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 10.5.sp, color = SUB)
        androidx.compose.material3.OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(hint, fontSize = 12.sp) },
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
        )
    }
}

@Composable
private fun ResumeSection(refresh: Int, onChanged: () -> Unit) {
    val ctx = LocalContext.current
    val info = remember(refresh) { ResumeStore.info(ctx) }
    val skills = remember(refresh) { ResumeProfile.load(ctx).skills.size }
    val uploaded = remember(refresh) { ResumeStore.isUserUploaded(ctx) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val bytes = runCatching {
            ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        }.getOrNull()
        val parsed = ResumeParser.parse(ctx, bytes, uri.lastPathSegment ?: "")
        if (!parsed.ok) {
            AlertDialog.Builder(ctx)
                .setTitle("这份文件解析不了")
                .setMessage(parsed.msg)
                .setPositiveButton("知道了", null)
                .show()
        } else {
            val name = runCatching { uri.lastPathSegment ?: "resume" }.getOrDefault("resume")
            ResumeStore.save(ctx, parsed.text, name)
            ResumeProfile.invalidate()
            Toast.makeText(ctx, parsed.msg, Toast.LENGTH_SHORT).show()
            onChanged()
        }
    }

    SCard("简历", "话术与打分的输入") {
        Text(
            if (info == null) "还没有简历，打分和话术都用不了"
            else "${if (uploaded) "已上传" else "内置"}：${info.name}",
            fontSize = 12.5.sp, color = INK
        )
        if (info != null) {
            Text(
                "${info.chars} 字 · 识别到 $skills 个技能" +
                    if (info.updatedAt > 0) " · ${info.timeText()}" else "",
                fontSize = 10.5.sp, color = SUB, modifier = Modifier.padding(top = 3.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallButton("上传简历文件", Modifier.weight(1f)) {
                picker.launch(
                    arrayOf(
                        "text/plain", "application/pdf",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/octet-stream"
                    )
                )
            }
            SmallButton("粘贴", Modifier.weight(1f)) {
                val et = EditText(ctx).apply {
                    hint = "把简历全文粘贴到这里"
                    setText(ResumeStore.current(ctx))
                    setPadding(24, 24, 24, 24)
                }
                AlertDialog.Builder(ctx)
                    .setTitle("粘贴简历")
                    .setView(et)
                    .setPositiveButton("保存") { _, _ ->
                        val t = et.text.toString()
                        if (t.length < 100) {
                            Toast.makeText(ctx, "太短了", Toast.LENGTH_SHORT).show()
                        } else {
                            ResumeStore.save(ctx, t, "手动粘贴")
                            ResumeProfile.invalidate()
                            onChanged()
                        }
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        }
        Text(
            "支持 .txt / .md / .pdf。本机解析，不上传任何东西。",
            fontSize = 10.sp, color = FAINT,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(title, fontSize = 12.sp, color = SUB)
        Spacer(Modifier.height(6.dp))
        Text(body, fontSize = 14.sp, color = INK, lineHeight = 22.sp)
    }
}

// ---------------------------------------------------------------- 色板

private val INK = Color(0xFF0F172A)
private val SUB = Color(0xFF64748B)
private val FAINT = Color(0xFF94A3B8)
private val LINE2 = Color(0xFFE5E9F0)
private val GREEN = Color(0xFF16A34A)
private val GREEN_SOFT = Color(0xFFE8F7EE)
private val AMBER = Color(0xFFD97706)
private val AMBER_SOFT = Color(0xFFFEF3E2)
private val RED = Color(0xFFDC2626)
private val RED_SOFT = Color(0xFFFEF2F2)
private val TEAL2 = Color(0xFF0EA5A5)
private val BG = Color(0xFFF4F6FA)
