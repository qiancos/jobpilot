package com.jobpilot.pad

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 01 首页 / 今日概览 */
class HomeActivity : ComponentActivity() {

    private val refresh = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Row(Modifier.fillMaxSize().background(BG)) {
                    NavRail(NavDest.HOME)
                    HomeScreen(refresh.intValue, Modifier.weight(1f))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refresh.intValue++
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
private val AMBER_BG = Color(0xFFFEF3E2)
private val LINE = Color(0xFFE5E9F0)

fun a11yEnabled(ctx: Context): Boolean = runCatching {
    val s = Settings.Secure.getString(ctx.contentResolver, "enabled_accessibility_services")
    s != null && s.contains(ctx.packageName)
}.getOrDefault(false)

@Composable
fun HomeScreen(refreshToken: Int, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val db = remember { Db.get(ctx) }

    val total by remember(refreshToken) { mutableIntStateOf(db.count()) }
    val selected by remember(refreshToken) { mutableIntStateOf(db.countSelected()) }
    val today by remember(refreshToken) { mutableIntStateOf(db.countCollectedToday()) }
    val applied by remember(refreshToken) { mutableIntStateOf(db.countApplications()) }
    val a11y by remember(refreshToken) { mutableIntStateOf(if (a11yEnabled(ctx)) 1 else 0) }

    val date = remember { SimpleDateFormat("yyyy-MM-dd · EEEE", Locale.CHINA).format(Date()) }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 18.dp)
    ) {
        Text("今天", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = INK)
        Text(date, fontSize = 12.sp, color = SUB, modifier = Modifier.padding(top = 3.dp))

        Spacer(Modifier.height(18.dp))

        // KPI
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Kpi("今日采集", today.toString(), "个岗位", BLUE, Modifier.weight(1f))
            Kpi("待你审", total.toString(), "个", INK, Modifier.weight(1f))
            Kpi("已选", selected.toString(), "个", GREEN, Modifier.weight(1f))
            Kpi("已投", applied.toString(), "次", INK, Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        // 无障碍状态
        if (a11y == 0) {
            Card(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("无障碍服务未开启", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AMBER)
                        Text(
                            "读 BOSS App 的岗位卡片需要它；不开启只能用网页版降级采集",
                            fontSize = 11.5.sp, color = Color(0xFF7C5B22), lineHeight = 17.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = {
                            runCatching {
                                ctx.startActivity(
                                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AMBER),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) { Text("去开启", fontSize = 12.5.sp) }
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        // 下一步
        Card(Modifier.fillMaxWidth()) {
            Text("接着做", fontSize = 12.sp, color = SUB, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            val (title, desc) = when {
                total == 0 -> "先采集一批岗位" to "打开 BOSS 职位列表，点悬浮面板的「采集」"
                selected == 0 -> "从 $total 个岗位里挑几个" to "30 秒勾出 8 个想投的"
                else -> "写好这 $selected 个岗位的话术" to "AI 起稿 + 9 条闸门，改完再投"
            }
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = INK)
            Text(desc, fontSize = 12.sp, color = SUB, modifier = Modifier.padding(top = 5.dp))
        }

        Spacer(Modifier.height(16.dp))

        // 入口
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    ctx.startActivity(Intent(ctx, MainActivity::class.java))
                },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GREEN),
                shape = RoundedCornerShape(12.dp)
            ) { Text("开始采集", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }

            OutlinedButton(
                onClick = {
                    ctx.startActivity(Intent(ctx, CockpitActivity::class.java))
                },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) { Text("待投清单（$total）", fontSize = 14.sp) }
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = {
                    ctx.startActivity(Intent(ctx, MainActivity::class.java))
                },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp)
            ) { Text("打开 BOSS 网页版", fontSize = 13.sp) }
            OutlinedButton(
                onClick = {
                    ctx.startActivity(
                        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp)
            ) { Text("权限设置", fontSize = 13.sp) }
        }

        Spacer(Modifier.height(22.dp))
        Text(
            "所有数据保存在本机 · 简历不上传 · 日投上限 15 家",
            fontSize = 10.5.sp, color = FAINT
        )
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun Kpi(label: String, value: String, unit: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CARD)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(label, fontSize = 11.5.sp, color = SUB)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = color)
            Text(unit, fontSize = 10.5.sp, color = FAINT, modifier = Modifier.padding(start = 3.dp, bottom = 5.dp))
        }
    }
}

@Composable
private fun Card(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CARD)
            .padding(16.dp)
    ) { Column { content() } }
}
