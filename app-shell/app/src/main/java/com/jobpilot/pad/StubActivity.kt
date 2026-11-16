package com.jobpilot.pad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobpilot.pad.data.Db
import com.jobpilot.pad.ui.NavDest
import com.jobpilot.pad.ui.NavRail
import com.jobpilot.pad.ui.go

/**
 * 话术 / 台账 / 设置 三屏占位。
 * 现在只把「有什么数据、下一步做什么」讲清楚，功能等 M3/M4 再接。
 */
class StubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dest = runCatching {
            NavDest.valueOf(intent.getStringExtra("dest") ?: NavDest.SKILL.name)
        }.getOrDefault(NavDest.SKILL)

        setContent {
            MaterialTheme {
                Row(Modifier.fillMaxSize().background(Color(0xFFF4F6FA))) {
                    NavRail(dest)
                    StubScreen(dest)
                }
            }
        }
    }
}

@Composable
private fun StubScreen(dest: NavDest) {
    val ctx = LocalContext.current
    val db = remember { Db.get(ctx) }
    val total = remember { db.count() }
    val selected = remember { db.countSelected() }
    val applied = remember { db.countApplications() }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 22.dp)
    ) {
        Text(dest.label, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        Spacer(Modifier.height(6.dp))

        when (dest) {
            NavDest.SKILL -> {
                Text("话术工作台 · M3 实现", fontSize = 12.sp, color = Color(0xFF64748B))
                Spacer(Modifier.height(18.dp))
                InfoCard("要做什么", "AI 按 JD 起稿 → 9 条闸门校验 → 不过自动重写 ≤2 轮 → 人工改 1–2 字")
                InfoCard("当前数据", "已选 $selected 个岗位等待生成话术")
                InfoCard("依赖", "话术闸门（Python 版已跑通 78 条历史数据，待移植到 App）")
            }
            NavDest.LEDGER -> {
                Text("台账与复盘 · M4 实现", fontSize = 12.sp, color = Color(0xFF64748B))
                Spacer(Modifier.height(18.dp))
                InfoCard("漏斗", "候选 $total → 审批 $selected → 送达 0 → 回复 0 → 约面 0")
                InfoCard("已投", "$applied 次")
                InfoCard("依赖", "application 表已建好，等执行器接入后就会有数据")
            }
            else -> {
                Text("设置 · 部分可用", fontSize = 12.sp, color = Color(0xFF64748B))
                Spacer(Modifier.height(18.dp))
                InfoCard("投递档位", "标准 15 条/日（现在只在面板内存里，重启会丢）")
                InfoCard("无障碍", if (a11yEnabled(ctx)) "已开启 ✓" else "未开启")
                InfoCard("数据", "岗位 $total 条 · 已选 $selected · 已投 $applied")
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { go(ctx, NavDest.HOME) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) { Text("回首页") }
            }
        }
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
        Text(title, fontSize = 12.sp, color = Color(0xFF64748B))
        Spacer(Modifier.height(6.dp))
        Text(body, fontSize = 14.sp, color = Color(0xFF0F172A), lineHeight = 22.sp)
    }
}
