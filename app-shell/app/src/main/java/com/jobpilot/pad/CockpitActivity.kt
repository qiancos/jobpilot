package com.jobpilot.pad

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobpilot.pad.data.Db
import com.jobpilot.pad.data.Job
import com.jobpilot.pad.exec.AppExecutor
import com.jobpilot.pad.ui.NavDest
import com.jobpilot.pad.ui.NavRail

/** 待投清单（M1-06）+ 执行器入口（M2） */
class CockpitActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Row(Modifier.fillMaxSize().background(Color(0xFFF4F6FA))) {
                    NavRail(NavDest.LIST)
                    Box(Modifier.weight(1f)) { CockpitScreen() }
                }
            }
        }
    }
}

/** 默认话术（M3 会换成 AI 起稿 + 闸门） */
private const val DRAFT_GREETING =
    "您好，我是 2027 届计算机应届生，实习做过电商商城的商品/订单模块，" +
        "熟悉 Spring Boot + MyBatis + MySQL。看到贵司这个岗位很匹配，方便聊聊吗？"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CockpitScreen() {
    val ctx = LocalContext.current
    val db = remember { Db.get(ctx) }

    var jobs by remember { mutableStateOf<List<Job>>(emptyList()) }
    var onlySelected by remember { mutableStateOf(false) }
    var tick by remember { mutableStateOf(0) }
    var execBusy by remember { mutableStateOf(false) }

    LaunchedEffect(onlySelected, tick) {
        jobs = db.listJobs(onlySelected)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (onlySelected) "清单 · 已选 ${jobs.size}" else "清单 · 共 ${jobs.size}",
                        fontSize = 17.sp
                    )
                },
                actions = {
                    TextButton(onClick = { tick++ }) { Text("刷新") }
                    TextButton(onClick = { db.selectAll(false); tick++ }) { Text("清空勾选") }
                    TextButton(onClick = { onlySelected = !onlySelected }) {
                        Text(if (onlySelected) "看全部" else "只看已选")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "已选 ${db.countSelected()} / 共 ${db.count()}\n今日已投 ${db.countDeliveredToday()} / 15",
                    fontSize = 11.5.sp, lineHeight = 16.sp
                )
                Spacer(Modifier.weight(1f))

                OutlinedButton(
                    enabled = !execBusy,
                    onClick = {
                        val target = db.listJobs(true).firstOrNull()
                        if (target == null) {
                            Toast.makeText(ctx, "先在清单里勾一个岗位", Toast.LENGTH_SHORT).show()
                        } else {
                            execBusy = true
                            toastSwitch(ctx, target)
                            Thread {
                                val r = AppExecutor.deliver(
                                    target, DRAFT_GREETING, dryRun = true, waitBossMs = 30_000L
                                )
                                showResult(ctx, "试跑（未发送）", r) { execBusy = false }
                            }.start()
                        }
                    }
                ) { Text(if (execBusy) "执行中…" else "试跑", fontSize = 13.sp) }

                Spacer(Modifier.width(8.dp))

                Button(
                    enabled = !execBusy,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF16A34A)
                    ),
                    onClick = {
                        val target = db.listJobs(true).firstOrNull()
                        if (target == null) {
                            Toast.makeText(ctx, "先在清单里勾一个岗位", Toast.LENGTH_SHORT).show()
                        } else {
                            confirmSend(ctx, db, target) { confirmed ->
                                if (!confirmed) return@confirmSend
                                execBusy = true
                                toastSwitch(ctx, target)
                                Thread {
                                    val r = AppExecutor.deliver(
                                        target, DRAFT_GREETING, dryRun = false, waitBossMs = 30_000L
                                    )
                                    val detail = r.steps.lastOrNull { !it.ok }?.detail
                                        ?: r.steps.lastOrNull()?.detail
                                    db.recordDelivery(target, r.level, detail)
                                    showResult(ctx, "投递结果", r) { execBusy = false; tick++ }
                                }.start()
                            }
                        }
                    }
                ) { Text(if (execBusy) "执行中…" else "发送", fontSize = 13.sp) }
            }
        }
    ) { pad ->
        if (jobs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("还没有采集到岗位", fontSize = 15.sp)
                    Spacer(Modifier.padding(6.dp))
                    Text(
                        "点左侧「采集」→ 打开 BOSS 职位列表 → 点悬浮面板的「采集」",
                        fontSize = 12.sp, color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(pad)) {
                items(jobs, key = { it.id }) { job ->
                    JobRow(job) { checked ->
                        db.setSelected(job.id, checked)
                        tick++
                    }
                    Divider()
                }
            }
        }
    }
}

// ------------------------------------------------------------------ 对话框

private fun toastSwitch(ctx: android.content.Context, target: Job) {
    Toast.makeText(
        ctx,
        "30 秒内切到 BOSS 并打开「${target.company}」的岗位详情页…",
        Toast.LENGTH_LONG
    ).show()
}

/** 发送前最后一次人工确认（对应原型 22 屏） */
private fun confirmSend(
    ctx: android.content.Context,
    db: Db,
    target: Job,
    onResult: (Boolean) -> Unit
) {
    AlertDialog.Builder(ctx)
        .setTitle("确认发送给「${target.company}」")
        .setMessage(
            """
            岗位：${target.title}

            话术（${DRAFT_GREETING.length} 字）：
            $DRAFT_GREETING

            今日额度：${db.countDeliveredToday()} / 15
            投递动作：进入会话 + 发送上面这条
            保护：发送前会核对会话里的公司名，对不上就放弃
            """.trimIndent()
        )
        .setPositiveButton("确认发送") { _, _ -> onResult(true) }
        .setNegativeButton("取消") { _, _ -> onResult(false) }
        .show()
}

private fun showResult(
    ctx: android.content.Context,
    title: String,
    r: AppExecutor.Result,
    onDone: () -> Unit
) {
    val msg = r.steps.joinToString("\n") {
        (if (it.ok) "✓ " else "✗ ") + it.name + (if (it.detail.isNotBlank()) "：" + it.detail else "")
    }
    Handler(Looper.getMainLooper()).post {
        onDone()
        val levelText = when (r.level) {
            "delivered" -> "L3 已送达"
            "bound" -> "L2 已进会话"
            "clicked" -> "L1 只点到了按钮"
            else -> "未成交"
        }
        AlertDialog.Builder(ctx)
            .setTitle(if (r.ok) "$title · $levelText" else "$title · 中断（$levelText）")
            .setMessage(msg)
            .setPositiveButton("知道了", null)
            .show()
    }
}

@Composable
private fun JobRow(job: Job, onToggle: (Boolean) -> Unit) {
    var checked by remember(job.id) { mutableStateOf(job.selected) }
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { checked = !checked; onToggle(checked) }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = { checked = it; onToggle(it) })
        Column(Modifier.weight(1f)) {
            Text(job.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(job.summary(), fontSize = 12.sp, color = Color.Gray, maxLines = 1)
            if (job.tags.isNotBlank()) {
                Text(job.tags, fontSize = 11.sp, color = Color(0xFF2563EB), maxLines = 1)
            }
        }
        Text(job.keyword, fontSize = 10.sp, color = Color.LightGray)
    }
}
