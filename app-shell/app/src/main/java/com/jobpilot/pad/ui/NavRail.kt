package com.jobpilot.pad.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobpilot.pad.CockpitActivity
import com.jobpilot.pad.HomeActivity
import com.jobpilot.pad.StubActivity

/** 左侧导航的 5 个去处（对齐原型 01–13 屏的侧栏） */
enum class NavDest(val label: String) {
    HOME("首页"), LIST("清单"), SKILL("话术"), LEDGER("台账"), SETTINGS("设置")
}

fun go(ctx: Context, dest: NavDest) {
    val intent = when (dest) {
        NavDest.HOME -> Intent(ctx, HomeActivity::class.java)
        NavDest.LIST -> Intent(ctx, CockpitActivity::class.java)
        else -> Intent(ctx, StubActivity::class.java).putExtra("dest", dest.name)
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
    runCatching { ctx.startActivity(intent) }
}

/**
 * 侧边栏。宽度 84dp（比原型 78 略宽，为了手指点得准）。
 * 只用中文文字，不放图标字符——避免字体缺字变成方框。
 */
@Composable
fun NavRail(current: NavDest, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    Column(
        modifier
            .width(84.dp)
            .fillMaxHeight()
            .background(Color.White)
            .padding(top = 20.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Text("JP", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))

        NavDest.values().forEach { d ->
            val on = d == current
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (on) Color(0xFFE8F7EE) else Color.Transparent)
                    .clickable(enabled = !on) { go(ctx, d) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    d.label,
                    fontSize = 13.sp,
                    fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (on) Color(0xFF16A34A) else Color(0xFF94A3B8)
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // 采集页入口（原型里由首页「开始采集」进入，这里给个常驻入口）
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF1F4F9))
                .clickable {
                    runCatching {
                        ctx.startActivity(Intent(ctx, com.jobpilot.pad.MainActivity::class.java))
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text("采集", fontSize = 13.sp, color = Color(0xFF475569))
        }
    }
}
