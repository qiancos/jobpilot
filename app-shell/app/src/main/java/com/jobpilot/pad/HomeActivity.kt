package com.jobpilot.pad

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobpilot.pad.data.Db
import com.jobpilot.pad.exec.LlmClient
import com.jobpilot.pad.exec.ResumeStore
import com.jobpilot.pad.ui.NavDest
import com.jobpilot.pad.ui.NavRail
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** 01 今日驾驶舱 */
class HomeActivity : ComponentActivity() {

    private val refresh = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PanelState.load(this)
        setContent {
            MaterialTheme {
                Row(Modifier.fillMaxSize().background(BG)) {
                    NavRail(NavDest.HOME)
                    Cockpit(refresh.intValue, Modifier.weight(1f))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refresh.intValue++
    }
}

@Composable
private fun Cockpit(refreshToken: Int, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val db = remember { Db.get(ctx) }

    val total = remember(refreshToken) { db.count() }
    val selected = remember(refreshToken) { db.countSelected() }
    val collectedToday = remember(refreshToken) { db.countCollectedToday() }
    val applied = remember(refreshToken) { db.countApplications() }
    val delivered = remember(refreshToken) { db.countDelivered() }
    val replied = remember(refreshToken) { db.countReplied() }
    val todayApplied = remember(refreshToken) { db.countDeliveredToday() }
    val failedToday = remember(refreshToken) { db.countFailedToday() }
    val attemptsToday = remember(refreshToken) { db.countAttemptsToday() }
    val firstCollected = remember(refreshToken) { db.firstCollectedToday() }
    val lastDelivered = remember(refreshToken) { db.lastDeliveredAt() }
    val recent = remember(refreshToken) { db.recentApplications(3) }

    val a11y = remember(refreshToken) { a11yEnabled(ctx) }
    val battery = remember(refreshToken) { ignoringBattery(ctx) }
    val llmReady = remember(refreshToken) { LlmClient.cfg(ctx).ready }
    val resume = remember(refreshToken) { ResumeStore.info(ctx) }
    val bossFront = remember(refreshToken) {
        runCatching { A11yBridge.service?.rootInActiveWindow?.packageName?.toString() }
            .getOrNull() == "com.hpbr.bosszhipin"
    }

    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            tick++
        }
    }
    val phase = remember(tick) { PanelState.phase }
    val batchDone = remember(tick) { PanelState.batchDone }
    val batchTotal = remember(tick) { PanelState.batchTotal }
    val curCompany = remember(tick) { PanelState.curCompany }
    val curTitle = remember(tick) { PanelState.curTitle }
    val curAction = remember(tick) { PanelState.curAction }
    val waitSec = remember(tick) { PanelState.waitSec }
    val etaSec = remember(tick) { PanelState.etaSec }
    val l3 = remember(tick) { PanelState.l3 }
    val failInBatch = remember(tick) { PanelState.fail }
    val batchId = remember(tick) { PanelState.batchId }

    val limit = PanelState.dailyLimit
    val running = phase != PanelState.Phase.IDLE
    val doneNow = if (running) batchDone else todayApplied
    val totalNow = if (running && batchTotal > 0) batchTotal else limit
    val ring = if (totalNow > 0) doneNow.toFloat() / totalNow else 0f

    BoxWithConstraints(modifier.fillMaxSize()) {
        val wide = maxWidth >= 1000.dp

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            AppBar(
                a11y = a11y,
                bossFront = bossFront,
                todayApplied = todayApplied,
                limit = limit,
                tier = PanelState.tierNames[PanelState.tier],
                gapMin = PanelState.gapMin,
                gapMax = PanelState.gapMax
            )

            Spacer(Modifier.height(14.dp))

            CmdBar { ctx.startActivity(Intent(ctx, MainActivity::class.java)) }

            Spacer(Modifier.height(14.dp))

            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    BatchCard(
                        modifier = Modifier.weight(1f),
                        phase = phase,
                        done = doneNow,
                        total = totalNow,
                        todayApplied = todayApplied,
                        limit = limit,
                        remaining = (totalNow - doneNow).coerceAtLeast(0),
                        etaSec = etaSec,
                        curCompany = curCompany,
                        curTitle = curTitle,
                        curAction = curAction,
                        waitSec = waitSec,
                        batchTotal = batchTotal,
                        batchId = batchId,
                        ring = ring,
                        collectedToday = collectedToday,
                        selected = selected,
                        firstCollected = firstCollected,
                        lastDelivered = lastDelivered,
                        l3 = l3,
                        failInBatch = failInBatch,
                        onOpen = { ctx.startActivity(Intent(ctx, MainActivity::class.java)) },
                        onReport = { ctx.startActivity(stub(ctx, NavDest.LEDGER)) },
                        onAdjust = { ctx.startActivity(Intent(ctx, CockpitActivity::class.java)) }
                    )
                    RhythmCard(
                        modifier = Modifier.width(396.dp),
                        collectedToday = collectedToday,
                        selected = selected,
                        todayApplied = todayApplied,
                        failedToday = failedToday,
                        firstCollected = firstCollected,
                        lastDelivered = lastDelivered,
                        curCompany = curCompany,
                        curAction = curAction,
                        running = running
                    )
                }
            } else {
                BatchCard(
                    modifier = Modifier.fillMaxWidth(),
                    phase = phase,
                    done = doneNow,
                    total = totalNow,
                    todayApplied = todayApplied,
                    limit = limit,
                    remaining = (totalNow - doneNow).coerceAtLeast(0),
                    etaSec = etaSec,
                    curCompany = curCompany,
                    curTitle = curTitle,
                    curAction = curAction,
                    waitSec = waitSec,
                    batchTotal = batchTotal,
                    batchId = batchId,
                    ring = ring,
                    collectedToday = collectedToday,
                    selected = selected,
                    firstCollected = firstCollected,
                    lastDelivered = lastDelivered,
                    l3 = l3,
                    failInBatch = failInBatch,
                    onOpen = { ctx.startActivity(Intent(ctx, MainActivity::class.java)) },
                    onReport = { ctx.startActivity(stub(ctx, NavDest.LEDGER)) },
                    onAdjust = { ctx.startActivity(Intent(ctx, CockpitActivity::class.java)) }
                )
                Spacer(Modifier.height(14.dp))
                RhythmCard(
                    modifier = Modifier.fillMaxWidth(),
                    collectedToday = collectedToday,
                    selected = selected,
                    todayApplied = todayApplied,
                    failedToday = failedToday,
                    firstCollected = firstCollected,
                    lastDelivered = lastDelivered,
                    curCompany = curCompany,
                    curAction = curAction,
                    running = running
                )
            }

            Spacer(Modifier.height(14.dp))

            val rate = if (attemptsToday > 0) todayApplied * 100 / attemptsToday else 0
            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Kpi("候选岗位", total.toString(), "条", "今日新增 $collectedToday 条", GREEN, Modifier.weight(1f))
                    Kpi("待投清单", selected.toString(), "家", "已勾选 · 投前过 9 条闸门", SUB, Modifier.weight(1f))
                    Kpi("今日送达", todayApplied.toString(), "家", "送达率 $rate% · L3 核验", GREEN, Modifier.weight(1f))
                    Kpi("真人回复", replied.toString(), "条", if (replied > 0) "北极星指标" else "等 HR 消息", TEAL, Modifier.weight(1f))
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Kpi("候选岗位", total.toString(), "条", "今日新增 $collectedToday 条", GREEN, Modifier.weight(1f))
                    Kpi("待投清单", selected.toString(), "家", "已勾选", SUB, Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Kpi("今日送达", todayApplied.toString(), "家", "送达率 $rate%", GREEN, Modifier.weight(1f))
                    Kpi("真人回复", replied.toString(), "条", "累计", TEAL, Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(14.dp))

            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    FunnelCard(
                        modifier = Modifier.weight(1f),
                        total = total,
                        selected = selected,
                        delivered = delivered,
                        replied = replied,
                        l3 = l3,
                        batchTotal = batchTotal,
                        failedToday = failedToday
                    )
                    FollowUpCard(
                        modifier = Modifier.weight(1f),
                        replied = replied,
                        onOpenLedger = { ctx.startActivity(stub(ctx, NavDest.LEDGER)) }
                    )
                    RiskCard(
                        modifier = Modifier.width(380.dp),
                        a11y = a11y,
                        battery = battery,
                        onOpenSettings = { ctx.startActivity(stub(ctx, NavDest.SETTINGS)) }
                    )
                }
            } else {
                FunnelCard(
                    modifier = Modifier.fillMaxWidth(),
                    total = total,
                    selected = selected,
                    delivered = delivered,
                    replied = replied,
                    l3 = l3,
                    batchTotal = batchTotal,
                    failedToday = failedToday
                )
                Spacer(Modifier.height(14.dp))
                FollowUpCard(
                    modifier = Modifier.fillMaxWidth(),
                    replied = replied,
                    onOpenLedger = { ctx.startActivity(stub(ctx, NavDest.LEDGER)) }
                )
                Spacer(Modifier.height(14.dp))
                RiskCard(
                    modifier = Modifier.fillMaxWidth(),
                    a11y = a11y,
                    battery = battery,
                    onOpenSettings = { ctx.startActivity(stub(ctx, NavDest.SETTINGS)) }
                )
            }

            Spacer(Modifier.height(14.dp))

            RecentCard(recent) {
                ctx.startActivity(stub(ctx, NavDest.LEDGER))
            }

            Spacer(Modifier.height(18.dp))
            Text(
                "所有数据保存在本机 · 简历不上传 · 日上限 $limit 家 · 夜间静默 ${PanelState.nightStart}:00–${PanelState.nightEnd}:00",
                fontSize = 10.5.sp, color = FAINT
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}

// ---------------------------------------------------------------- 顶部

@Composable
private fun AppBar(
    a11y: Boolean,
    bossFront: Boolean,
    todayApplied: Int,
    limit: Int,
    tier: String,
    gapMin: Int,
    gapMax: Int,
) {
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greet = when (hour) {
        in 5..10 -> "早上好"
        in 11..13 -> "中午好"
        in 14..18 -> "下午好"
        else -> "晚上好"
    }
    val now = remember { Calendar.getInstance() }
    val curHour = now.get(Calendar.HOUR_OF_DAY)
    val nightStart = PanelState.nightStart
    val nightEnd = PanelState.nightEnd
    val inNight = if (nightStart > nightEnd) curHour >= nightStart || curHour < nightEnd
    else curHour in nightStart until nightEnd
    val minsToNight = if (inNight) -1
    else ((nightStart - curHour) * 60 - now.get(Calendar.MINUTE)).coerceAtLeast(0)
    val nightText = if (inNight) "夜间静默中 · ${nightEnd}:00 后恢复"
    else "距夜间静默 ${nightStart}:00 还有 ${minsToNight / 60}h ${minsToNight % 60}m"

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("$greet，今天也稳稳地投", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = INK)
                Text(
                    "${SimpleDateFormat("yyyy-MM-dd EEEE", Locale.CHINA).format(Date())} · $nightText",
                    fontSize = 11.5.sp, color = SUB, modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusText(if (bossFront) "BOSS 前台" else "BOSS 未在前台", bossFront)
            StatusText(if (a11y) "无障碍 已开启" else "无障碍 未开启", a11y)
            StatusText("今日配额 $todayApplied / $limit", todayApplied < limit)
            StatusText("${tier}档 · ${gapMin}–${gapMax}s", true)
        }
    }
}

@Composable
private fun StatusText(text: String, ok: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (ok) GREEN else AMBER)
        )
        Text(
            text, fontSize = 10.5.sp, color = if (ok) SUB else AMBER,
            modifier = Modifier.padding(start = 5.dp)
        )
    }
}

@Composable
private fun CmdBar(onOpen: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CARD)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("✦", fontSize = 15.sp, color = TEAL)
        Text(
            "试试说：帮我找 Java 实习，广州或深圳，本周活跃",
            fontSize = 12.sp, color = FAINT,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 10.dp)
        )
        Text(
            "自然语言任务", fontSize = 9.5.sp, color = SUB,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF1F4F9))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = onOpen,
            modifier = Modifier.height(34.dp),
            shape = RoundedCornerShape(9.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GREEN)
        ) { Text("去采集", fontSize = 11.5.sp) }
    }
}

// ---------------------------------------------------------------- 批次卡

@Composable
private fun BatchCard(
    modifier: Modifier,
    phase: PanelState.Phase,
    done: Int,
    total: Int,
    todayApplied: Int,
    limit: Int,
    remaining: Int,
    etaSec: Long,
    curCompany: String,
    curTitle: String,
    curAction: String,
    waitSec: Long,
    batchTotal: Int,
    batchId: String,
    ring: Float,
    collectedToday: Int,
    selected: Int,
    firstCollected: Long,
    lastDelivered: Long,
    l3: Int,
    failInBatch: Int,
    onOpen: () -> Unit,
    onReport: () -> Unit,
    onAdjust: () -> Unit,
) {
    val running = phase != PanelState.Phase.IDLE
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CARD)
            .padding(18.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill(if (batchId.isNotBlank() && running) batchId else "今日批次", NEUTRAL)
            Pill("${if (running && batchTotal > 0) batchTotal else total} 家 · ${PanelState.tierNames[PanelState.tier]}档", NEUTRAL)
            Pill(
                when (phase) {
                    PanelState.Phase.RUNNING -> "执行中"
                    PanelState.Phase.PAUSED -> "已暂停"
                    PanelState.Phase.BREAKER -> "已熔断"
                    PanelState.Phase.DONE -> "已完成"
                    PanelState.Phase.IDLE -> "待命"
                },
                when (phase) {
                    PanelState.Phase.BREAKER -> RED
                    PanelState.Phase.RUNNING -> GREEN
                    else -> SUB
                }
            )
        }

        Row(Modifier.padding(top = 12.dp)) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(done.toString(), fontSize = 46.sp, fontWeight = FontWeight.Bold, color = INK)
                    Text(
                        "/ $total", fontSize = 21.sp, color = FAINT,
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )
                    Text(
                        if (running) "已 L3 送达" else "今日已送达",
                        fontSize = 12.5.sp, color = SUB,
                        modifier = Modifier.padding(start = 10.dp, bottom = 8.dp)
                    )
                }
                Text(
                    when {
                        phase == PanelState.Phase.DONE -> "本批结束 · L3 送达 $l3 · 失败 $failInBatch"
                        running && remaining > 0 ->
                            "剩余 $remaining 家" +
                                (if (etaSec > 0) " · 预计 ${fmtSec(etaSec)}" else "") +
                                (if (curCompany.isNotBlank()) " · 当前 $curCompany" else "")
                        running -> "正在收尾…"
                        todayApplied == 0 -> "今天还没有送达记录，开一批试试"
                        else -> "今日 ${todayApplied}/${limit} · 还有 ${(limit - todayApplied).coerceAtLeast(0)} 个额度"
                    },
                    fontSize = 12.sp, color = SUB, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Ring(ring)
        }

        Row(
            Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onOpen,
                modifier = Modifier.weight(1f).height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GREEN),
                shape = RoundedCornerShape(11.dp)
            ) {
                Text(
                    if (running) "继续执行 $remaining 家" else "开始投递",
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                )
            }
            OutlinedButton(
                onClick = onReport,
                modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(11.dp)
            ) { Text("查看核验报告", fontSize = 12.5.sp) }
            OutlinedButton(
                onClick = onAdjust,
                modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(11.dp)
            ) { Text("调整批次", fontSize = 12.5.sp) }
        }

        Divider()

        Row(verticalAlignment = Alignment.CenterVertically) {
            Step("${fmtClock(firstCollected)} 采集", "$collectedToday 条", GREEN)
            StepLine()
            Step("审阅", "勾选 $selected 家", SUB)
            StepLine()
            Step(
                when (phase) {
                    PanelState.Phase.RUNNING -> "执行中"
                    PanelState.Phase.PAUSED -> "已暂停"
                    PanelState.Phase.BREAKER -> "已熔断"
                    PanelState.Phase.DONE -> "已结束"
                    PanelState.Phase.IDLE -> "待开始"
                },
                if (running) "$done / $total" else "未开始",
                if (phase == PanelState.Phase.BREAKER) RED else GREEN
            )
            StepLine()
            Step(if (lastDelivered > 0) "${fmtClock(lastDelivered)} 核验" else "核验", if (lastDelivered > 0) "已生成" else "待生成", SUB)
        }
    }
}

@Composable
private fun Ring(fraction: Float) {
    Box(Modifier.size(92.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 8.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = LINE, startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = GREEN, startAngle = -90f, sweepAngle = 360f * fraction.coerceIn(0f, 1f),
                useCenter = false, topLeft = Offset(inset, inset), size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${(fraction * 100).toInt()}%", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = INK)
            Text("批次完成度", fontSize = 8.5.sp, color = FAINT)
        }
    }
}

@Composable
private fun Step(label: String, value: String, color: Color) {
    Column {
        Text(label, fontSize = 9.5.sp, color = FAINT)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun StepLine() {
    Box(
        Modifier
            .padding(horizontal = 8.dp)
            .width(20.dp)
            .height(1.dp)
            .background(LINE)
    )
}

// ---------------------------------------------------------------- 今日节奏

@Composable
private fun RhythmCard(
    modifier: Modifier,
    collectedToday: Int,
    selected: Int,
    todayApplied: Int,
    failedToday: Int,
    firstCollected: Long,
    lastDelivered: Long,
    curCompany: String,
    curAction: String,
    running: Boolean,
) {
    PCard(modifier, "今日节奏", "后台只读", trailing = { Pill("1 项待办", NEUTRAL) }) {
        RhythmRow(
            fmtClock(firstCollected), collectedToday > 0,
            "采集${if (collectedToday > 0) "完成" else "未开始"}",
            if (collectedToday > 0) "新增 $collectedToday 条" else "去 BOSS 列表页点「采集」"
        )
        RhythmRow("—", selected > 0, "审阅清单",
            if (selected > 0) "已勾选 $selected 家" else "还没勾选岗位")
        RhythmRow(
            if (running) "进行中" else fmtClock(lastDelivered), running,
            if (running) "批量执行中" else "投递记录",
            if (running) "已送达 $todayApplied · 失败 $failedToday" +
                if (curCompany.isNotBlank()) " · $curCompany" else ""
            else "今日送达 $todayApplied · 失败 $failedToday"
        )
        RhythmRow(
            if (lastDelivered > 0) fmtClock(lastDelivered) else "—",
            lastDelivered > 0, "会话核验",
            if (lastDelivered > 0) {
                if (curAction.isNotBlank() && running) curAction else "最近一条已核验"
            } else "送达后自动核验"
        )
    }
}

@Composable
private fun RhythmRow(time: String, active: Boolean, title: String, desc: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            time, fontSize = 9.5.sp, color = FAINT, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            modifier = Modifier.width(46.dp).padding(top = 2.dp)
        )
        Box(
            Modifier
                .padding(top = 5.dp, end = 8.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(if (active) GREEN else LINE)
        )
        Column {
            Text(title, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = INK)
            Text(desc, fontSize = 10.sp, color = FAINT, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

// ---------------------------------------------------------------- KPI / 漏斗 / 跟进 / 风险

@Composable
private fun Kpi(
    label: String,
    value: String,
    unit: String,
    delta: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CARD)
            .padding(horizontal = 15.dp, vertical = 12.dp)
    ) {
        Text(label, fontSize = 11.5.sp, color = SUB)
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 5.dp)) {
            Text(value, fontSize = 25.sp, fontWeight = FontWeight.Bold, color = INK)
            Text(unit, fontSize = 10.5.sp, color = FAINT, modifier = Modifier.padding(start = 3.dp, bottom = 5.dp))
        }
        Text(delta, fontSize = 9.5.sp, color = color, modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun FunnelCard(
    modifier: Modifier,
    total: Int,
    selected: Int,
    delivered: Int,
    replied: Int,
    l3: Int,
    batchTotal: Int,
    failedToday: Int,
) {
    val max = maxOf(total, 1)
    PCard(modifier, "今日漏斗", "候选 → 约面", trailing = { Pill("近 7 天", NEUTRAL) }) {
        FunnelRow("候选", total, max, BLUE)
        FunnelRow("审批", selected, max, GREEN)
        FunnelRow("送达", delivered, max, GREEN)
        FunnelRow("回复", replied, max, AMBER)
        Text(
            "送达率 ${if (total > 0) delivered * 100 / total else 0}%（$delivered/$total）" +
                " · 回复率 ${if (delivered > 0) replied * 100 / delivered else 0}%",
            fontSize = 10.sp, color = FAINT, modifier = Modifier.padding(top = 8.dp)
        )
        Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricBox(
                "本批 L3", if (batchTotal > 0) "$l3/$batchTotal" else "—",
                "按家计", Modifier.weight(1f)
            )
            MetricBox("今日失败", failedToday.toString(), "未送达", Modifier.weight(1f))
        }
    }
}

@Composable
private fun FunnelRow(label: String, value: Int, max: Int, color: Color) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 10.5.sp, color = SUB, modifier = Modifier.width(34.dp))
        Box(
            Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(LINE)
        ) {
            Box(
                Modifier
                    .fillMaxWidth((if (max > 0) value.toFloat() / max else 0f).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(color)
            )
        }
        Text(
            value.toString(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = INK,
            modifier = Modifier.width(32.dp).padding(start = 8.dp)
        )
    }
}

@Composable
private fun MetricBox(label: String, value: String, hint: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF7F9FC))
            .padding(horizontal = 11.dp, vertical = 9.dp)
    ) {
        Text(label, fontSize = 9.5.sp, color = FAINT)
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 3.dp)) {
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = INK)
            Text(hint, fontSize = 9.sp, color = FAINT, modifier = Modifier.padding(start = 5.dp, bottom = 2.dp))
        }
    }
}

@Composable
private fun FollowUpCard(modifier: Modifier, replied: Int, onOpenLedger: () -> Unit) {
    PCard(modifier, "待跟进", null, trailing = { Pill("$replied 条", if (replied > 0) AMBER else NEUTRAL) }) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF7F9FC))
                .padding(14.dp)
        ) {
            Column {
                Text(
                    if (replied > 0) "有 $replied 条 HR 回复" else "暂无需要跟进的会话",
                    fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = INK
                )
                Text(
                    if (replied > 0) "去台账看回复内容，决定要不要发简历"
                    else "HR 回复后，这里会出现「要简历 / 邀面 / 问薪」",
                    fontSize = 10.5.sp, color = FAINT, lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("共 $replied 个会话", fontSize = 10.sp, color = FAINT)
            Spacer(Modifier.weight(1f))
            Text(
                "查看全部 →", fontSize = 10.5.sp, color = GREEN,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GREEN_BG)
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        OutlinedButton(
            onClick = onOpenLedger,
            modifier = Modifier.fillMaxWidth().height(38.dp),
            shape = RoundedCornerShape(10.dp)
        ) { Text("打开台账", fontSize = 12.sp) }
    }
}

@Composable
private fun RiskCard(modifier: Modifier, a11y: Boolean, battery: Boolean, onOpenSettings: () -> Unit) {
    val pass = (if (a11y) 1 else 0) + (if (battery) 1 else 0)
    PCard(
        modifier, "风险与自检", null,
        trailing = { Pill(if (pass == 2) "整体正常" else "$pass / 2", if (pass == 2) GREEN else AMBER) }
    ) {
        if (pass < 2) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(AMBER_BG)
                    .padding(horizontal = 10.dp, vertical = 9.dp)
            ) {
                Box(Modifier.padding(top = 5.dp, end = 8.dp).size(6.dp).clip(CircleShape).background(AMBER))
                Column {
                    Text(
                        if (!a11y) "无障碍服务未开启" else "未加入电池白名单",
                        fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF8A5600)
                    )
                    Text(
                        if (!a11y) "采集、读 JD、投递都会失效" else "后台可能被系统杀掉，面板会消失",
                        fontSize = 9.5.sp, color = Color(0xFF9A6A20), modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        RiskRow(
            "无障碍服务${if (a11y) "运行中" else "未开启"}",
            if (a11y) "控件 id 内置 · 随版本更新" else "去设置重新开启"
        )
        RiskRow("熔断规则", "连续 ${PanelState.BREAKER_THRESHOLD} 次未送达才停")
        RiskRow("今日闸门", "发送前核对公司名，对不上就放弃")
        RiskRow("应用保活", if (battery) "已加入电池白名单" else "未加入白名单")
        Divider()
        OutlinedButton(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth().height(38.dp),
            shape = RoundedCornerShape(10.dp)
        ) { Text("打开环境自检", fontSize = 12.sp) }
    }
}

@Composable
private fun RiskRow(title: String, desc: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
        Box(
            Modifier
                .padding(top = 5.dp, end = 8.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(LINE)
        )
        Column {
            Text(title, fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = INK)
            Text(desc, fontSize = 9.5.sp, color = FAINT, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

// ---------------------------------------------------------------- 最近投递

@Composable
private fun RecentCard(recent: List<com.jobpilot.pad.data.AppRow>, onOpenLedger: () -> Unit) {
    PCard(
        Modifier.fillMaxWidth(), "最近投递", "台账里最新 3 条", null
    ) {
        if (recent.isEmpty()) {
            Text(
                "还没有投递记录。第一条送达后，这里会显示公司、岗位和核验状态。",
                fontSize = 12.sp, color = FAINT, lineHeight = 18.sp
            )
        } else {
            recent.forEachIndexed { i, r ->
                if (i > 0) Divider()
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(stateColor(r.state)))
                    Column(Modifier.weight(1f).padding(start = 9.dp)) {
                        Text(
                            r.company.ifBlank { "未知公司" },
                            fontSize = 13.sp, fontWeight = FontWeight.Medium, color = INK,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            r.title, fontSize = 10.5.sp, color = FAINT,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(stateLabel(r.state), fontSize = 10.5.sp, color = stateColor(r.state))
                        Text(fmtTime(r.at), fontSize = 9.5.sp, color = FAINT)
                    }
                }
            }
        }
        Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            Text(
                "打开台账 →", fontSize = 10.5.sp, color = GREEN,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

// ---------------------------------------------------------------- 通用组件

@Composable
private fun PCard(
    modifier: Modifier,
    title: String,
    sub: String?,
    trailing: @Composable (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CARD)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = INK)
            if (sub != null) {
                Text(sub, fontSize = 9.5.sp, color = FAINT, modifier = Modifier.padding(start = 7.dp))
            }
            Spacer(Modifier.weight(1f))
            trailing?.invoke()
        }
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun Pill(text: String, color: Color) {
    val bg = when (color) {
        GREEN -> GREEN_BG
        AMBER -> AMBER_BG
        RED -> RED_BG
        else -> Color(0xFFF1F4F9)
    }
    Text(
        text, fontSize = 9.5.sp, color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        maxLines = 1
    )
}

@Composable
private fun Divider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .height(1.dp)
            .background(LINE)
    )
}

// ---------------------------------------------------------------- 工具

private fun stub(ctx: Context, dest: NavDest): Intent =
    Intent(ctx, StubActivity::class.java).putExtra("dest", dest.name)

private fun fmtClock(ts: Long): String =
    if (ts <= 0) "—" else SimpleDateFormat("HH:mm", Locale.CHINA).format(Date(ts))

private fun fmtSec(sec: Long): String = String.format("%d 分 %02d 秒", sec / 60, sec % 60)

private fun fmtTime(ts: Long): String =
    if (ts <= 0) "—" else SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(Date(ts))

private fun stateLabel(state: String): String = when (state) {
    "delivered", "verified" -> "已送达"
    "replied" -> "已回复"
    "bound" -> "进了会话"
    "clicked" -> "点了沟通"
    "failed", "none" -> "失败"
    else -> state.ifBlank { "待确认" }
}

private fun stateColor(state: String): Color = when (state) {
    "delivered", "verified" -> GREEN
    "replied" -> TEAL
    "bound", "clicked" -> AMBER
    "failed", "none" -> RED
    else -> FAINT
}

private fun ignoringBattery(ctx: Context): Boolean = runCatching {
    val pm = ctx.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
    pm.isIgnoringBatteryOptimizations(ctx.packageName)
}.getOrDefault(false)

fun a11yEnabled(ctx: Context): Boolean = runCatching {
    val s = Settings.Secure.getString(ctx.contentResolver, "enabled_accessibility_services")
    s != null && s.contains(ctx.packageName)
}.getOrDefault(false)

// ---------------------------------------------------------------- 色板

private val BG = Color(0xFFF4F6FA)
private val CARD = Color.White
private val INK = Color(0xFF0F172A)
private val SUB = Color(0xFF64748B)
private val FAINT = Color(0xFF94A3B8)
private val GREEN = Color(0xFF16A34A)
private val GREEN_BG = Color(0xFFE8F7EE)
private val BLUE = Color(0xFF2563EB)
private val AMBER = Color(0xFFD97706)
private val AMBER_BG = Color(0xFFFEF3E2)
private val RED = Color(0xFFDC2626)
private val RED_BG = Color(0xFFFEF2F2)
private val TEAL = Color(0xFF0EA5A5)
private val NEUTRAL = Color(0xFF64748B)
private val LINE = Color(0xFFE5E9F0)
