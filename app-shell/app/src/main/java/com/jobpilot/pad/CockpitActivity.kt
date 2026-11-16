package com.jobpilot.pad

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobpilot.pad.data.Db
import com.jobpilot.pad.data.Job
import com.jobpilot.pad.data.ScoreRow
import com.jobpilot.pad.exec.AppExecutor
import com.jobpilot.pad.exec.AutoScorer
import com.jobpilot.pad.exec.BatchRunner
import com.jobpilot.pad.exec.Gate
import com.jobpilot.pad.exec.GreetingGen
import com.jobpilot.pad.exec.OneShot
import com.jobpilot.pad.exec.RefineRunner
import com.jobpilot.pad.exec.ResumeProfile
import com.jobpilot.pad.exec.Safety
import com.jobpilot.pad.exec.Scorer
import com.jobpilot.pad.ui.NavDest
import com.jobpilot.pad.ui.NavRail
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** 02 待投清单（对齐原型 s3）+ 单条/批量投递 */
class CockpitActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PanelState.load(this)
        setContent {
            MaterialTheme {
                Row(Modifier.fillMaxSize().background(BG)) {
                    NavRail(NavDest.LIST)
                    CockpitScreen(Modifier.weight(1f))
                }
            }
        }
    }
}

/** 按岗位 + 简历生成话术（一条一版，不是同一段模板） */
private fun greetingFor(ctx: android.content.Context, job: Job): String =
    GreetingGen.build(job, ResumeProfile.load(ctx))

@Composable
fun CockpitScreen(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val db = remember { Db.get(ctx) }

    var jobs by remember { mutableStateOf<List<Job>>(emptyList()) }
    var scores by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var appliedIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    var query by remember { mutableStateOf("") }
    var onlySelected by remember { mutableStateOf(false) }
    var todayOnly by remember { mutableStateOf(false) }
    var only27 by remember { mutableStateOf(false) }
    var hideApplied by remember { mutableStateOf(false) }

    var tick by remember { mutableIntStateOf(0) }
    var execBusy by remember { mutableStateOf(false) }
    var aiMenu by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<Job?>(null) }

    // 进来先按简历给没分过的岗位补分
    LaunchedEffect(Unit) {
        val n = AutoScorer.run(ctx, db, force = false)
        if (n > 0) Toast.makeText(ctx, "已按你的简历给 $n 个岗位评分", Toast.LENGTH_SHORT).show()
        tick++
    }

    LaunchedEffect(tick) {
        jobs = db.listJobs(false)
        scores = db.scoreMap()
        appliedIds = db.deliveredJobIds()
    }

    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val selectedCount = jobs.count { it.selected }
    val todayCount = jobs.count { it.collectedAt >= todayStart }
    val count27 = jobs.count { it.title.contains("27") || it.tags.contains("27") }
    val hitCount = jobs.count { (scores[it.id] ?: -1) >= 65 }
    val keyword = query.trim()

    val visible = jobs.filter { j ->
        (!onlySelected || j.selected) &&
            (!todayOnly || j.collectedAt >= todayStart) &&
            (!only27 || j.title.contains("27") || j.tags.contains("27")) &&
            (!hideApplied || j.id !in appliedIds) &&
            (keyword.isBlank() ||
                j.title.contains(keyword, true) ||
                j.company.contains(keyword, true) ||
                j.tags.contains(keyword, true) ||
                j.keyword.contains(keyword, true))
    }.sortedWith(
        compareByDescending<Job> { scores[it.id] ?: -1 }.thenByDescending { it.collectedAt }
    )

    val lastAt = jobs.maxOfOrNull { it.collectedAt } ?: 0L
    val collectText = when {
        lastAt <= 0L -> "还没有采集记录"
        lastAt >= todayStart ->
            "最近采集 今天 " + SimpleDateFormat("HH:mm", Locale.CHINA).format(Date(lastAt))
        else -> "最近采集 " + SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(Date(lastAt))
    }
    val deliveredToday = db.countDeliveredToday()

    val emptyHint = if (jobs.isEmpty()) {
        "点左侧「采集」→ 在 BOSS 列表里点悬浮面板的「采集」"
    } else {
        "试试清掉筛选条件或换个关键词"
    }

    // 全选只作用于当前可见的行，别把筛掉的一起勾上
    val toggleAll: (Boolean) -> Unit = { on ->
        visible.forEach { if (it.selected != on) db.setSelected(it.id, on) }
        tick++
    }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val wide = maxWidth >= 860.dp
        val detailWide = maxWidth >= 900.dp

        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
            val open = detail
            if (open != null) {
                val jd = remember(open.id, tick) { db.jobJd(open.id) }
                val sc = remember(open.id, tick) { db.scoreRow(open.id) }
                val idx = visible.indexOfFirst { it.id == open.id }
                JobDetail(
                    modifier = Modifier.weight(1f),
                    job = open,
                    jd = jd,
                    score = sc,
                    index = if (idx >= 0) idx + 1 else 1,
                    wide = detailWide,
                    onBack = { detail = null },
                    onIgnore = {
                        db.setSelected(open.id, false)
                        detail = null
                        tick++
                    },
                    onBlacklist = {
                        db.setBlacklisted(open.company, true)
                        db.setSelected(open.id, false)
                        Toast.makeText(ctx, "已加入黑名单：${open.company}", Toast.LENGTH_SHORT).show()
                        detail = null
                        tick++
                    },
                    onSelect = {
                        db.setSelected(open.id, true)
                        detail = null
                        tick++
                        runCatching {
                            ctx.startActivity(
                                Intent(ctx, StubActivity::class.java)
                                    .putExtra("dest", NavDest.SKILL.name)
                            )
                        }
                    }
                )
            } else {
            HeaderRow(
                total = jobs.size,
                selected = selectedCount,
                sub = collectText,
                onlySelected = onlySelected,
                onToggleSelected = { onlySelected = !onlySelected },
                onRecollect = { ctx.startActivity(Intent(ctx, MainActivity::class.java)) },
                onAi = { aiMenu = true }
            )

            Spacer(Modifier.height(12.dp))

            FilterBar(
                query = query,
                onQuery = { query = it },
                compact = !wide,
                todayCount = todayCount,
                count27 = count27,
                todayOnly = todayOnly,
                onToday = { todayOnly = !todayOnly },
                only27 = only27,
                onOnly27 = { only27 = !only27 },
                hideApplied = hideApplied,
                onHideApplied = { hideApplied = !hideApplied },
                onlySelected = onlySelected,
                onOnlySelected = { onlySelected = !onlySelected },
                appliedCount = appliedIds.size
            )

            Spacer(Modifier.height(12.dp))

            if (wide) {
                Row(
                    Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    JobTable(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        jobs = visible,
                        scores = scores,
                        applied = appliedIds,
                        emptyHint = emptyHint,
                        onToggle = { job, on -> db.setSelected(job.id, on); tick++ },
                        onToggleAll = toggleAll,
                        onOpen = { detail = it }
                    )
                    Column(
                        Modifier.width(330.dp).fillMaxHeight().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        BatchSummary(
                            total = jobs.size,
                            hit = hitCount,
                            selected = selectedCount,
                            todayApplied = deliveredToday,
                            at = lastAt
                        )
                        QuotaCard(
                            modifier = Modifier.fillMaxWidth().height(190.dp),
                            todayApplied = deliveredToday,
                            limit = PanelState.dailyLimit,
                            selected = selectedCount
                        )
                        NoteCard(selectedCount)
                    }
                }
            } else {
                JobTable(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    jobs = visible,
                    scores = scores,
                    applied = appliedIds,
                    emptyHint = emptyHint,
                    onToggle = { job, on -> db.setSelected(job.id, on); tick++ },
                    onToggleAll = toggleAll,
                    onOpen = { detail = it }
                )
            }

            Spacer(Modifier.height(14.dp))

            BatchBar(
                selected = selectedCount,
                estSec = selectedCount * ((PanelState.gapMin + PanelState.gapMax) / 2 + 20),
                deliveredToday = deliveredToday,
                limit = PanelState.dailyLimit,
                busy = execBusy,
                onDryRun = {
                    val target = db.listJobs(true).firstOrNull()
                    if (target == null) {
                        Toast.makeText(ctx, "先在清单里勾一个岗位", Toast.LENGTH_SHORT).show()
                    } else {
                        execBusy = true
                        toastSwitch(ctx, target)
                        Thread {
                            val r = AppExecutor.deliver(
                                target, greetingFor(ctx, target), dryRun = true, waitBossMs = 30_000L
                            )
                            showResult(ctx, "试跑（未发送）", r) { execBusy = false }
                        }.start()
                    }
                },
                onSend = {
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
                                    target, greetingFor(ctx, target), dryRun = false, waitBossMs = 30_000L
                                )
                                val note = r.steps.lastOrNull { !it.ok }?.detail
                                    ?: r.steps.lastOrNull()?.detail
                                db.recordDelivery(target, r.level, note)
                                showResult(ctx, "投递结果", r) { execBusy = false; tick++ }
                            }.start()
                        }
                    }
                },
                onBatch = {
                    val picked = db.listJobs(true)
                    val quotaLeft = (PanelState.dailyLimit - db.countDeliveredToday()).coerceAtLeast(0)
                    val list = picked.take(quotaLeft)
                    when {
                        picked.isEmpty() ->
                            Toast.makeText(ctx, "先在清单里勾选岗位", Toast.LENGTH_SHORT).show()
                        list.isEmpty() ->
                            Toast.makeText(ctx, "今日额度已用完", Toast.LENGTH_SHORT).show()
                        else -> startBatch(ctx, db, list) { tick++ }
                    }
                }
            )
            }
        }
    }

    LaunchedEffect(aiMenu) {
        if (!aiMenu) return@LaunchedEffect
        AlertDialog.Builder(ctx)
            .setTitle("AI 精算")
            .setItems(
                arrayOf(
                    "自动筛 · 规则评分并勾选推荐",
                    "单条 AI · 读当前 BOSS 详情页打分",
                    "批量 AI · 读 JD 精算前 10 个"
                )
            ) { _, which ->
                aiMenu = false
                when (which) {
                    0 -> runScreen(ctx, db) { tick++ }
                    1 -> oneShot(ctx, db) { tick++ }
                    else -> refine(ctx, db) { tick++ }
                }
            }
            .setNegativeButton("取消") { _, _ -> aiMenu = false }
            .setOnDismissListener { aiMenu = false }
            .show()
    }
}

// ------------------------------------------------------------------ 自动筛选

private fun runScreen(ctx: android.content.Context, db: Db, onDone: () -> Unit) {
    val resume = ResumeProfile.load(ctx)
    if (!resume.loaded) {
        Toast.makeText(ctx, "没有找到简历（assets/resume.txt）", Toast.LENGTH_LONG).show()
        return
    }
    val all = db.listJobs(false)
    var rec = 0
    var skip = 0
    val bucket = LinkedHashMap<String, Int>()
    for (j in all) {
        val sc = Scorer.score(j, resume)
        db.saveScore(j.id, sc.total, sc.hit.joinToString(","), sc.miss.joinToString(","), sc.strengths, sc.gaps, sc.advice)
        when {
            sc.excluded -> skip++
            sc.recommended -> { rec++; db.setSelected(j.id, true) }
        }
        val k = when {
            sc.excluded -> "排除（销售/中介等）"
            sc.total >= 80 -> "80–100 很匹配"
            sc.total >= 65 -> "65–79 可以投"
            sc.total >= 45 -> "45–64 再看"
            else -> "45 以下 跳过"
        }
        bucket[k] = (bucket[k] ?: 0) + 1
    }
    AlertDialog.Builder(ctx)
        .setTitle("自动筛选完成")
        .setMessage(
            """
            简历技能：${resume.skills.size} 个
            岗位总数：${all.size}

            推荐并已勾选：$rec 个
            硬排除：$skip 个

            分布：
            ${bucket.entries.joinToString("\n") { "  ${it.key}：${it.value} 个" }}
            """.trimIndent()
        )
        .setPositiveButton("知道了") { _, _ -> onDone() }
        .show()
}

// ------------------------------------------------------------------ 单条 AI 打分

private fun oneShot(ctx: android.content.Context, db: Db, onDone: () -> Unit) {
    val target = db.listJobs(true).firstOrNull() ?: db.listJobs(false).firstOrNull()
    if (target == null) {
        Toast.makeText(ctx, "清单里还没有岗位", Toast.LENGTH_SHORT).show()
        return
    }
    AlertDialog.Builder(ctx)
        .setTitle("单条 AI 打分")
        .setMessage(
            """
            目标：${target.company} · ${target.title}

            点开始后，请在 30 秒内：
              1. 切到 BOSS
              2. 打开【上面这个岗位】的详情页
              3. 停在那里别动

            会读那一页的 JD，调 DeepSeek 打分，然后弹出完整结果。
            """.trimIndent()
        )
        .setPositiveButton("开始") { _, _ ->
            val dlg = AlertDialog.Builder(ctx)
                .setTitle("单条打分中")
                .setMessage("等待 BOSS 前台…")
                .setCancelable(false)
                .create()
            dlg.show()
            Thread {
                val r = OneShot.run(ctx, db, target)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    runCatching { dlg.dismiss() }
                    // 把 App 拉回前台，否则结果弹窗会被 BOSS 盖住
                    runCatching {
                        ctx.startActivity(
                            android.content.Intent(ctx, CockpitActivity::class.java)
                                .addFlags(
                                    android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                                        android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                )
                        )
                    }
                    val msg = buildString {
                        if (!r.ok) {
                            append("卡在：${r.stage}\n")
                            append("原因：${r.error}\n\n")
                            if (r.jd.isNotBlank()) append("JD 读到 ${r.jd.length} 字：\n${r.jd.take(160)}…\n")
                            if (r.pageCompany.isNotBlank()) append("页面上看到的公司：${r.pageCompany}\n")
                        } else {
                            val v = r.verdict!!
                            append("公司核对：${if (r.companyOnPage) "页面上有「${target.company}」✓" else "页面上没找到「${target.company}」⚠ 可能打错了"}\n")
                            append("JD：${r.jd.length} 字\n\n")
                            append("匹配度 ${v.score} 分（${v.level}）\n\n")
                            if (v.pros.isNotEmpty()) append("优势：\n" + v.pros.joinToString("\n") { "· $it" } + "\n\n")
                            if (v.gaps.isNotEmpty()) append("缺口：\n" + v.gaps.joinToString("\n") { "· $it" } + "\n\n")
                            if (v.verify.isNotEmpty()) append("待核实：\n" + v.verify.joinToString("\n") { "· $it" } + "\n\n")
                            if (v.advice.isNotBlank()) append("建议：${v.advice}")
                        }
                    }
                    AlertDialog.Builder(ctx)
                        .setTitle(if (r.ok) "AI 打分 · ${r.verdict!!.score} 分" else "单条打分失败")
                        .setMessage(msg.take(2500))
                        .setPositiveButton("知道了") { _, _ -> onDone() }
                        .setOnDismissListener { onDone() }
                        .show()
                }
            }.start()
        }
        .setNegativeButton("取消", null)
        .show()
}

// ------------------------------------------------------------------ 读 JD 精算

private fun refine(ctx: android.content.Context, db: Db, onDone: () -> Unit) {
    val resume = ResumeProfile.load(ctx)
    if (!resume.loaded) {
        Toast.makeText(ctx, "没有找到简历", Toast.LENGTH_LONG).show()
        return
    }
    val jobs = db.listJobs(true).ifEmpty { db.listJobs(false) }.take(10)
    if (jobs.isEmpty()) {
        Toast.makeText(ctx, "没有岗位可算", Toast.LENGTH_SHORT).show()
        return
    }
    AlertDialog.Builder(ctx)
        .setTitle("读 JD 精算 ${jobs.size} 个岗位")
        .setMessage(
            """
            会逐个打开岗位详情，读真正的职位描述（tv_description），
            再按「简历 ∩ JD」重新打分。每个约 2–3 秒。

            开始前请让 BOSS 停在【职位列表】页。
            """.trimIndent()
        )
        .setPositiveButton("开始") { _, _ ->
            val dlg = AlertDialog.Builder(ctx)
                .setTitle("读 JD 中")
                .setMessage("准备…")
                .setNegativeButton("停止") { _, _ -> RefineRunner.stop() }
                .setCancelable(false)
                .create()
            dlg.show()
            val main = Handler(Looper.getMainLooper())
            Thread {
                val n = RefineRunner.run(ctx, jobs, db, resume) { p ->
                    main.post {
                        runCatching {
                            dlg.setMessage("${p.index + 1}/${p.total}  ${p.company}\n正在：${p.phase}\n已算 ${p.done} 个")
                        }
                    }
                }
                main.post {
                    runCatching { dlg.dismiss() }
                    AlertDialog.Builder(ctx)
                        .setTitle("精算完成 · $n / ${jobs.size}")
                        .setMessage("已按真实 JD 重新打分，清单上的分数已经更新。")
                        .setPositiveButton("知道了") { _, _ -> onDone() }
                        .setOnDismissListener { onDone() }
                        .show()
                }
            }.start()
        }
        .setNegativeButton("取消", null)
        .show()
}

// ------------------------------------------------------------------ 批量

private fun startBatch(
    ctx: android.content.Context,
    db: Db,
    list: List<Job>,
    onDone: () -> Unit
) {
    val estMin = ((list.size - 1) * 82) / 60
    AlertDialog.Builder(ctx)
        .setTitle("批量投递 ${list.size} 个岗位")
        .setMessage(
            """
            会逐个执行：找到岗位 → 进详情 → 发送话术 → 退回列表。
            每条之间等 45–120 秒，预计 $estMin 分钟。

            开始前请让 BOSS 停在【职位列表】页。
            中途可以点「停止」，已投的会记账。
            """.trimIndent()
        )
        .setPositiveButton("开始") { _, _ ->
            val dlg = AlertDialog.Builder(ctx)
                .setTitle("批量投递中")
                .setMessage("准备…")
                .setNegativeButton("停止") { _, _ -> BatchRunner.stop() }
                .setCancelable(false)
                .create()
            dlg.show()

            val main = Handler(Looper.getMainLooper())
            Thread {
                val outcomes = BatchRunner.run(list, db, { greetingFor(ctx, it) }) { p ->
                    val text = buildString {
                        append("${p.index + 1}/${p.total}")
                        if (p.company.isNotBlank()) append("  ${p.company}")
                        append("\n正在：${p.phase}")
                        if (p.waitSec > 0) append("（${p.waitSec}s）")
                        append("\n成功 ${p.ok} · 跳过/失败 ${p.fail}")
                    }
                    main.post { runCatching { dlg.setMessage(text) } }
                }
                main.post {
                    runCatching { dlg.dismiss() }
                    val ok = outcomes.count { it.level == "delivered" }
                    val msg = outcomes.joinToString("\n") { o ->
                        val mark = if (o.level == "delivered") "✓" else "·"
                        "$mark ${o.job.company}：${levelText(o.level)}" +
                            (if (o.note.isNotBlank()) "｜${o.note.take(40)}" else "")
                    }
                    AlertDialog.Builder(ctx)
                        .setTitle("批量完成 · 送达 $ok / ${outcomes.size}")
                        .setMessage(msg.ifBlank { "没有执行任何投递" })
                        .setPositiveButton("知道了") { _, _ -> onDone() }
                        .setOnDismissListener { onDone() }
                        .show()
                }
            }.start()
        }
        .setNegativeButton("取消", null)
        .show()
}

private fun levelText(level: String): String = when (level) {
    "delivered" -> "L3 已送达"
    "bound" -> "L2 进会话未送达"
    "clicked" -> "L1 只点到按钮"
    "skipped" -> "被安全阀跳过"
    "not_found" -> "列表里没找到"
    else -> "失败"
}

// ------------------------------------------------------------------ 单条

private fun toastSwitch(ctx: android.content.Context, target: Job) {
    Toast.makeText(
        ctx, "30 秒内切到 BOSS 并打开「${target.company}」的岗位详情页…", Toast.LENGTH_LONG
    ).show()
}

private fun confirmSend(
    ctx: android.content.Context,
    db: Db,
    target: Job,
    onResult: (Boolean) -> Unit
) {
    val gate = Safety.check(db, target)
    if (!gate.ok) {
        AlertDialog.Builder(ctx)
            .setTitle("先等一下")
            .setMessage(gate.reason)
            .setPositiveButton("知道了") { _, _ -> onResult(false) }
            .show()
        return
    }
    val text = greetingFor(ctx, target)
    val g = Gate.check(text, target)
    AlertDialog.Builder(ctx)
        .setTitle("确认发送给「${target.company}」　闸门 ${g.summary}")
        .setMessage(
            """
            岗位：${target.title}

            话术（${text.length} 字）：
            $text

            今日额度：${db.countDeliveredToday()} / ${PanelState.dailyLimit}
            投递间隔：${PanelState.gapMin}–${PanelState.gapMax} 秒随机（防机器特征）
            保护：发送前会核对会话里的公司名，对不上就放弃
            """.trimIndent() + if (g.pass) "" else "\n\n⚠ 没过闸：\n" + g.fails.joinToString("\n") { "· $it" }
        )
        .setPositiveButton(if (g.pass) "确认发送" else "仍然发送") { _, _ -> onResult(true) }
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
        AlertDialog.Builder(ctx)
            .setTitle(
                if (r.ok) "$title · ${levelText(r.level)}"
                else "$title · 中断（${levelText(r.level)}）"
            )
            .setMessage(msg)
            .setPositiveButton("知道了", null)
            .show()
    }
}

// ------------------------------------------------------------------ 原型 s3 组件

@Composable
private fun HeaderRow(
    total: Int,
    selected: Int,
    sub: String,
    onlySelected: Boolean,
    onToggleSelected: () -> Unit,
    onRecollect: () -> Unit,
    onAi: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("待投清单", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = INK)
            Text(
                "$total 个候选 · 已选 $selected 家 · $sub",
                fontSize = 11.5.sp, color = SUB, modifier = Modifier.padding(top = 3.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("匹配分", fontSize = 10.5.sp, color = SUB)
            Box(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF1F4F9))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text("降序", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = INK)
            }
            FilterChip("只看已选", onlySelected, onToggleSelected)
            GhostButton("AI 精算", onAi)
            GhostButton("重新采集", onRecollect)
        }
    }
}

@Composable
private fun GhostButton(text: String, onClick: () -> Unit) {
    Box(
        Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(CARD)
            .border(1.dp, LINE, RoundedCornerShape(9.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 11.sp, color = SUB)
    }
}

@Composable
private fun FilterBar(
    query: String,
    onQuery: (String) -> Unit,
    compact: Boolean,
    todayCount: Int,
    count27: Int,
    todayOnly: Boolean,
    onToday: () -> Unit,
    only27: Boolean,
    onOnly27: () -> Unit,
    hideApplied: Boolean,
    onHideApplied: () -> Unit,
    onlySelected: Boolean,
    onOnlySelected: () -> Unit,
    appliedCount: Int,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SearchBox(query, onQuery, if (compact) 190.dp else 290.dp)
        FilterChip("今日活跃 $todayCount", todayOnly, onToday)
        FilterChip("27 届 $count27", only27, onOnly27)
        FilterChip("排除已投", hideApplied, onHideApplied)
        FilterChip("只看已选", onlySelected, onOnlySelected)
        Spacer(Modifier.weight(1f))
        if (!compact) {
            Text(
                "已投 $appliedCount 家 · 今日新增 $todayCount 家",
                fontSize = 10.5.sp, color = FAINT, maxLines = 1
            )
        }
    }
}

@Composable
private fun SearchBox(value: String, onChange: (String) -> Unit, boxWidth: androidx.compose.ui.unit.Dp) {
    Row(
        Modifier
            .width(boxWidth)
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CARD)
            .border(1.dp, LINE, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(Modifier.size(14.dp)) {
            val r = size.width * 0.34f
            val c = Offset(size.width * 0.42f, size.height * 0.42f)
            drawCircle(
                color = Color(0xFF98A2B3), radius = r, center = c,
                style = Stroke(width = 1.4.dp.toPx())
            )
            drawLine(
                color = Color(0xFF98A2B3),
                start = Offset(c.x + r * 0.72f, c.y + r * 0.72f),
                end = Offset(size.width * 0.94f, size.height * 0.94f),
                strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text("搜索岗位 / 公司 / 关键词", fontSize = 11.sp, color = Color(0xFF98A2B3))
            }
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 12.sp, color = INK),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FilterChip(text: String, on: Boolean = false, onClick: () -> Unit) {
    Box(
        Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (on) GREEN_BG else CARD)
            .border(1.dp, if (on) Color(0x3316A34A) else LINE, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontSize = 11.5.sp,
            color = if (on) GREEN else SUB,
            fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun JobTable(
    modifier: Modifier,
    jobs: List<Job>,
    scores: Map<String, Int>,
    applied: Set<String>,
    emptyHint: String,
    onToggle: (Job, Boolean) -> Unit,
    onToggleAll: (Boolean) -> Unit,
    onOpen: (Job) -> Unit,
) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(CARD)) {
        TableHeader(
            allOn = jobs.isNotEmpty() && jobs.all { it.selected },
            onToggleAll = onToggleAll
        )
        Box(Modifier.fillMaxWidth().height(1.dp).background(LINE))

        if (jobs.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("没有符合条件的岗位", fontSize = 14.sp, color = SUB)
                    Spacer(Modifier.height(6.dp))
                    Text(emptyHint, fontSize = 11.5.sp, color = FAINT)
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                items(jobs, key = { it.id }) { job ->
                    JobTableRow(
                        job = job,
                        score = scores[job.id],
                        applied = job.id in applied,
                        onToggle = { onToggle(job, it) },
                        onOpen = { onOpen(job) }
                    )
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F4F9)))
                }
            }
        }
    }
}

@Composable
private fun TableHeader(allOn: Boolean, onToggleAll: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color(0xFFFBFCFE))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(30.dp).clickable { onToggleAll(!allOn) }) { CheckSquare(allOn) }
        Text("岗位 / 公司", Modifier.weight(1.6f), fontSize = 10.5.sp, color = FAINT)
        Text("薪资", Modifier.width(96.dp), fontSize = 10.5.sp, color = FAINT)
        Text("标签", Modifier.weight(1f), fontSize = 10.5.sp, color = FAINT)
        Text("采集时间", Modifier.width(92.dp), fontSize = 10.5.sp, color = FAINT)
        Text(
            "匹配", Modifier.width(50.dp),
            fontSize = 10.5.sp, color = FAINT, textAlign = TextAlign.End
        )
    }
}

@Composable
private fun JobTableRow(
    job: Job,
    score: Int?,
    applied: Boolean,
    onToggle: (Boolean) -> Unit,
    onOpen: () -> Unit,
) {
    val (atText, atToday) = collectLabel(job.collectedAt)
    val place = listOf(job.company, job.area).filter { it.isNotBlank() }.joinToString(" · ")
    val tags = job.tags.split("/").filter { it.isNotBlank() }.take(2)

    Row(
        Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable { onOpen() }
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.width(30.dp).clickable { onToggle(!job.selected) },
            contentAlignment = Alignment.CenterStart
        ) { CheckSquare(job.selected) }

        Column(Modifier.weight(1.6f).padding(end = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    job.title,
                    fontSize = 12.8.sp, fontWeight = FontWeight.SemiBold, color = INK,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (applied) {
                    Spacer(Modifier.width(5.dp))
                    TagPill("已投")
                }
            }
            Text(
                place.ifBlank { job.keyword },
                fontSize = 10.8.sp, color = FAINT, maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Text(
            job.salary.ifBlank { "面议" },
            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = INK,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(96.dp)
        )

        Row(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tags.forEach { TagPill(it) }
        }

        Text(
            atText,
            fontSize = 10.5.sp,
            color = if (atToday) GREEN else FAINT,
            modifier = Modifier.width(92.dp)
        )

        Box(Modifier.width(50.dp), contentAlignment = Alignment.CenterEnd) {
            ScoreTag(score)
        }
    }
}

@Composable
private fun CheckSquare(on: Boolean) {
    Box(
        Modifier
            .size(16.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (on) GREEN else CARD)
            .border(1.dp, if (on) GREEN else Color(0xFFCBD5E1), RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (on) {
            Text("✓", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TagPill(text: String) {
    Text(
        text,
        fontSize = 9.5.sp, color = SUB, maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFFF5F7FB))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    )
}

@Composable
private fun ScoreTag(score: Int?) {
    if (score == null) {
        Box(Modifier.size(width = 30.dp, height = 22.dp))
        return
    }
    val fg = when {
        score >= 80 -> GREEN
        score >= 65 -> BLUE
        else -> FAINT
    }
    val bg = when {
        score >= 80 -> GREEN_BG
        score >= 65 -> Color(0xFFEFF4FF)
        else -> Color(0xFFF1F4F9)
    }
    Box(
        Modifier
            .size(width = 30.dp, height = 22.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text("$score", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

@Composable
private fun BatchSummary(
    total: Int,
    hit: Int,
    selected: Int,
    todayApplied: Int,
    at: Long,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CARD)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("本批摘要", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = INK)
            Spacer(Modifier.width(6.dp))
            Text("今日", fontSize = 10.5.sp, color = FAINT)
            Spacer(Modifier.weight(1f))
            Text(
                if (at > 0) SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(Date(at)) else "—",
                fontSize = 10.sp, color = FAINT
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            SummaryTile("采集", total, INK, Modifier.weight(1f))
            SummaryTile("命中", hit, BLUE, Modifier.weight(1f))
        }
        Spacer(Modifier.height(9.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            SummaryTile("已投", selected, GREEN, Modifier.weight(1f))
            SummaryTile("今日已投", todayApplied, AMBER, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(LINE))
        Spacer(Modifier.height(10.dp))
        Text(
            "命中口径：规则评分 ≥ 65 分\n去重依据：company|title 哈希（同一岗位只留一条）",
            fontSize = 9.5.sp, color = FAINT, lineHeight = 14.sp
        )
    }
}

@Composable
private fun SummaryTile(label: String, value: Int, valueColor: Color, modifier: Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF7F9FC))
            .padding(10.dp)
    ) {
        Text(label, fontSize = 10.sp, color = FAINT)
        Text(
            "$value", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = valueColor,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}

@Composable
private fun QuotaCard(
    modifier: Modifier,
    todayApplied: Int,
    limit: Int,
    selected: Int,
) {
    val after = todayApplied + selected
    val left = (limit - after).coerceAtLeast(0)
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CARD)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("今日配额", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = INK)
            Spacer(Modifier.weight(1f))
            Text(
                "${PanelState.tierNames[PanelState.tier]}档",
                fontSize = 10.sp, color = GREEN, fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(GREEN_BG)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Ring(
                fraction = if (limit > 0) after.toFloat() / limit else 0f,
                top = "$todayApplied",
                bottom = "/ $limit"
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("本批 +$selected 后", fontSize = 10.sp, color = FAINT)
                Text(
                    "$after / $limit",
                    fontSize = 15.sp, fontWeight = FontWeight.Bold, color = INK,
                    modifier = Modifier.padding(top = 3.dp)
                )
                Text(
                    when {
                        after > limit -> "超出 ${after - limit} 家，会按额度截断"
                        left <= 1 -> "仅剩 $left 家余额"
                        else -> "还有 $left 个额度"
                    },
                    fontSize = 10.5.sp, color = AMBER,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Box(Modifier.fillMaxWidth().height(1.dp).background(LINE))
        Spacer(Modifier.height(10.dp))
        Text(
            "超配额会自动截断，并在批次确认页显性提示；不会静默多投。",
            fontSize = 9.5.sp, color = FAINT, lineHeight = 14.sp
        )
    }
}

@Composable
private fun Ring(fraction: Float, top: String, bottom: String) {
    Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(72.dp)) {
            val stroke = 7.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = LINE, startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = GREEN, startAngle = -90f,
                sweepAngle = 360f * fraction.coerceIn(0f, 1f), useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(top, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = INK)
            Text(bottom, fontSize = 9.sp, color = FAINT)
        }
    }
}

@Composable
private fun NoteCard(selected: Int) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFFFDF7))
            .border(1.dp, Color(0xFFF3E3BE), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(AMBER))
            Text(
                "为什么只能人勾选",
                fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF8A6A22),
                modifier = Modifier.padding(start = 7.dp)
            )
        }
        Text(
            "自动海投会把「投递」变成噪声；这里的 $selected 家决定今晚的全部动作。",
            fontSize = 10.5.sp, color = Color(0xFF8A6A22), lineHeight = 16.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

// ------------------------------------------------------------------ 原型 s4 岗位详情

@Composable
private fun JobDetail(
    modifier: Modifier,
    job: Job,
    jd: String,
    score: ScoreRow?,
    index: Int,
    wide: Boolean,
    onBack: () -> Unit,
    onIgnore: () -> Unit,
    onBlacklist: () -> Unit,
    onSelect: () -> Unit,
) {
    val (atText, _) = collectLabel(job.collectedAt)
    val total = score?.total ?: 0

    Column(modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${job.company} · ${job.title}",
                    fontSize = 21.sp, fontWeight = FontWeight.Bold, color = INK,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    "待投清单 / 第 $index 家 · " + listOf(
                        job.area, job.salary.ifBlank { "面议" }, "采集 $atText"
                    ).filter { it.isNotBlank() }.joinToString(" · "),
                    fontSize = 11.sp, color = SUB, modifier = Modifier.padding(top = 3.dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GhostButton("← 清单", onBack)
                GhostButton("忽略", onIgnore)
                GhostButton("黑名单", onBlacklist)
                DarkPrimary(if (job.selected) "已在待投 · 写话术" else "加入待投并写话术", true, onSelect)
            }
        }

        Spacer(Modifier.height(12.dp))

        if (wide) {
            Row(
                Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(
                    Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())
                ) { JobInfoCard(job, jd, atText) }
                Column(
                    Modifier.width(446.dp).fillMaxHeight().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    MatchScoreCard(score, total)
                    TripleCard(score)
                }
            }
        } else {
            Column(
                Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                JobInfoCard(job, jd, atText)
                MatchScoreCard(score, total)
                TripleCard(score)
            }
        }
    }
}

@Composable
private fun JobInfoCard(job: Job, jd: String, atText: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CARD)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GREEN_BG),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    job.company.take(1).ifBlank { "岗" },
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GREEN
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        job.company.ifBlank { "未知公司" },
                        fontSize = 15.sp, fontWeight = FontWeight.Bold, color = INK,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    TagPill(if (job.source == "app") "BOSS App" else "网页降级")
                    if (job.keyword.isNotBlank()) TagPill(job.keyword)
                }
                Text(
                    listOf(job.area, "HR 招聘", "采集 $atText")
                        .filter { it.isNotBlank() }.joinToString(" · "),
                    fontSize = 10.5.sp, color = FAINT, modifier = Modifier.padding(top = 4.dp)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    job.salary.ifBlank { "面议" },
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = INK
                )
                Text(
                    "BOSS 直聘", fontSize = 10.sp, color = FAINT,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(LINE))
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            job.tags.split("/").filter { it.isNotBlank() }.take(6).forEach { TagPill(it) }
        }

        Spacer(Modifier.height(16.dp))
        Text("JD 原文", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = INK)
        Spacer(Modifier.height(8.dp))
        if (jd.isBlank()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFBFCFE))
                    .border(1.dp, LINE, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    "还没读过这个岗位的 JD",
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = INK
                )
                Text(
                    "点「AI 精算 → 单条 AI」，把 BOSS 停在这个岗位详情页，" +
                        "就能把职责与要求抓下来，并按 JD 重新打分。",
                    fontSize = 10.5.sp, color = SUB, lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        } else {
            Text(
                jd.take(4000),
                fontSize = 12.sp, color = Color(0xFF344054), lineHeight = 20.sp
            )
        }

        Spacer(Modifier.height(16.dp))
        Text("你需要知道的", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = INK)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniTile("地区", job.area.ifBlank { "未写" }, Modifier.weight(1f))
            MiniTile("薪资", job.salary.ifBlank { "面议" }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniTile("采集来源", if (job.source == "app") "BOSS App" else "网页降级", Modifier.weight(1f))
            MiniTile("采集时间", atText, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MiniTile(label: String, value: String, modifier: Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF7F9FC))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(label, fontSize = 10.sp, color = FAINT)
        Text(
            value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = INK,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}

@Composable
private fun MatchScoreCard(score: ScoreRow?, total: Int) {
    val ok = total >= 65
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CARD)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("匹配打分", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = INK)
            Spacer(Modifier.width(6.dp))
            Text("规则 + 简历技能", fontSize = 10.5.sp, color = FAINT)
            Spacer(Modifier.weight(1f))
            TagPill("可解释")
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Ring(fraction = total / 100f, top = "$total", bottom = "/ 100")
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    when {
                        score == null -> "还没打分"
                        total >= 80 -> "建议直接投"
                        total >= 65 -> "可以投"
                        total >= 45 -> "优先级放后"
                        else -> "建议跳过"
                    },
                    fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = if (ok) GREEN else AMBER
                )
                Text(
                    "口径：技能命中 + 城市 + 薪资 − 方向/年限扣分；读过 JD 后可精算。",
                    fontSize = 10.sp, color = FAINT, lineHeight = 15.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(LINE))
        Spacer(Modifier.height(10.dp))
        KeywordRow("JD 命中", score?.hit, GREEN, GREEN_BG)
        Spacer(Modifier.height(6.dp))
        KeywordRow("JD 缺失", score?.miss, RED, RED_BG)
    }
}

@Composable
private fun KeywordRow(label: String, csv: String?, fg: Color, bg: Color) {
    val items = csv.orEmpty().split(",").map { it.trim() }.filter { it.isNotBlank() }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 10.sp, color = FAINT, modifier = Modifier.width(56.dp))
        if (items.isEmpty()) {
            Text("—", fontSize = 10.sp, color = FAINT)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                items.take(4).forEach {
                    Text(
                        it, fontSize = 9.5.sp, color = fg,
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(bg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                if (items.size > 4) {
                    Text("+${items.size - 4}", fontSize = 9.5.sp, color = FAINT)
                }
            }
        }
    }
}

@Composable
private fun TripleCard(score: ScoreRow?) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CARD)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("三件套", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = INK)
            Spacer(Modifier.width(6.dp))
            Text("优势 / 不足 / 建议", fontSize = 10.5.sp, color = FAINT)
        }
        Spacer(Modifier.height(10.dp))
        TripleBlock("优势", score?.strengths, GREEN, GREEN_BG)
        Spacer(Modifier.height(8.dp))
        TripleBlock("不足", score?.gaps, RED, RED_BG)
        Spacer(Modifier.height(8.dp))
        TripleBlock("建议", score?.advice, BLUE, Color(0xFFEFF4FF))
    }
}

@Composable
private fun TripleBlock(label: String, text: String?, fg: Color, bg: Color) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 13.dp, vertical = 11.dp)
    ) {
        Text(
            label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = fg
        )
        Text(
            text?.takeIf { it.isNotBlank() } ?: "还没有打分结果",
            fontSize = 11.5.sp, color = if (text.isNullOrBlank()) FAINT else INK,
            lineHeight = 17.sp,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
}

@Composable
private fun BatchBar(
    selected: Int,
    estSec: Int,
    deliveredToday: Int,
    limit: Int,
    busy: Boolean,
    onDryRun: () -> Unit,
    onSend: () -> Unit,
    onBatch: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(NAV)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("已选", fontSize = 12.sp, color = Color(0xFFA9BACD))
        Text(" $selected", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(" 家", fontSize = 12.sp, color = Color(0xFFA9BACD))
        Dot()
        Text("预计执行 ${fmtDur(estSec)}", fontSize = 11.sp, color = Color(0xFFA9BACD))
        Dot()
        Text("配额 $deliveredToday / $limit", fontSize = 11.sp, color = Color(0xFFA9BACD))
        Spacer(Modifier.weight(1f))
        Text(
            "下一步：AI 起稿 + 9 条闸门",
            fontSize = 10.5.sp, color = Color(0xFF7E93AB),
            modifier = Modifier.padding(end = 12.dp)
        )
        DarkGhost(if (busy) "…" else "试跑", !busy && selected > 0, onDryRun)
        Spacer(Modifier.width(8.dp))
        DarkGhost("发送", !busy && selected > 0, onSend)
        Spacer(Modifier.width(8.dp))
        DarkPrimary("批量投递 · $selected", !busy && selected > 0, onBatch)
    }
}

@Composable
private fun Dot() {
    Text(
        "·", fontSize = 12.sp, color = Color(0xFF3E4C60),
        modifier = Modifier.padding(horizontal = 10.dp)
    )
}

@Composable
private fun DarkGhost(text: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xFF1B2434))
            .border(1.dp, Color(0xFF2C3A4F), RoundedCornerShape(9.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text, fontSize = 12.sp,
            color = if (enabled) Color(0xFFCBD5E1) else Color(0xFF5A6B80)
        )
    }
}

@Composable
private fun DarkPrimary(text: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) GREEN else Color(0xFF1B2434))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold,
            color = if (enabled) Color.White else Color(0xFF5A6B80)
        )
    }
}

/** 采集时间：今天显示时分并高亮，往期显示日期 */
private fun collectLabel(ts: Long): Pair<String, Boolean> {
    if (ts <= 0L) return "—" to false
    val day = SimpleDateFormat("yyyyMMdd", Locale.CHINA)
    val d = Date(ts)
    return if (day.format(Date()) == day.format(d)) {
        "今天 " + SimpleDateFormat("HH:mm", Locale.CHINA).format(d) to true
    } else {
        SimpleDateFormat("MM-dd", Locale.CHINA).format(d) to false
    }
}

private fun fmtDur(sec: Int): String {
    val m = sec / 60
    val s = sec % 60
    return when {
        m <= 0 -> "${s} 秒"
        s == 0 -> "$m 分钟"
        else -> "$m 分 $s 秒"
    }
}

private val BG = Color(0xFFF4F6FA)
private val CARD = Color.White
private val INK = Color(0xFF0F172A)
private val SUB = Color(0xFF64748B)
private val FAINT = Color(0xFF94A3B8)
private val GREEN = Color(0xFF16A34A)
private val GREEN_BG = Color(0xFFE8F7EE)
private val BLUE = Color(0xFF2563EB)
private val AMBER = Color(0xFFD97706)
private val RED = Color(0xFFDC2626)
private val RED_BG = Color(0xFFFEF2F2)
private val LINE = Color(0xFFE5E9F0)
private val NAV = Color(0xFF0F172A)
