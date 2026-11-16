package com.jobpilot.pad.exec

import android.content.Context
import android.view.accessibility.AccessibilityNodeInfo
import com.jobpilot.pad.A11yBridge
import com.jobpilot.pad.data.Db
import com.jobpilot.pad.data.Job
import com.jobpilot.pad.data.Keys

/**
 * 单条 AI 打分。
 *
 * 故意不做「从列表自动找卡片」——那一步最容易失败，而且失败了看不见。
 * 前置：用户在 BOSS 里打开某个岗位详情页，然后从清单点「单条AI」。
 * 这样每一步都能看见：读到几字 JD、公司名对不对、模型回了什么。
 */
object OneShot {

    private const val PKG = "com.hpbr.bosszhipin"

    data class R(
        val ok: Boolean,
        val jd: String = "",
        val companyOnPage: Boolean = false,
        val pageCompany: String = "",
        val verdict: LlmScorer.V? = null,
        val error: String = "",
        val stage: String = "",
    )

    fun run(ctx: Context, db: Db, job: Job, waitBossMs: Long = 20_000, waitJdBossMs: Long = 45_000): R {
        val t0 = System.currentTimeMillis()
        Log.i(ctx, "oneshot", "开始：目标=${job.company} · ${job.title}")
        val svc = A11yBridge.service
            ?: return R(false, error = "无障碍没开", stage = "前置").also {
                Log.i(ctx, "oneshot", "失败：无障碍没开")
            }

        // 1. 等 BOSS 到前台
        var waited = 0L
        while (waited < waitBossMs) {
            val pkg = runCatching { svc.rootInActiveWindow?.packageName }.getOrNull()
            if (pkg == PKG) break
            Thread.sleep(500); waited += 500
        }
        Log.i(ctx, "oneshot", "前台确认：等了 ${waited}ms")
        val root = runCatching { svc.rootInActiveWindow }.getOrNull()
            ?: return R(false, error = "读不到窗口", stage = "前置").also {
                Log.i(ctx, "oneshot", "失败：读不到窗口")
            }
        if (root.packageName != PKG) {
            Log.i(ctx, "oneshot", "失败：前台是 ${root.packageName}")
            return R(false, error = "当前不是 BOSS（${root.packageName}）", stage = "前置")
        }

        // 2. 读 JD
        // 2. 等 JD 出现：用户可能还在列表页，必须轮询到详情页渲染出来
        var jd = ""
        var jdWaited = 0L
        while (jdWaited < waitJdBossMs) {
            val r0 = runCatching { svc.rootInActiveWindow }.getOrNull()
            if (r0?.packageName == PKG) {
                jd = JdReader.read(ctx)
                if (jd.length >= 80) break
            }
            Thread.sleep(700); jdWaited += 700
        }
        Log.i(ctx, "oneshot", "读 JD：${jd.length} 字（等了 ${jdWaited}ms）" +
            (if (jd.isBlank()) "" else "｜开头：${jd.take(40).replace("\n", " ")}"))
        if (jd.length < 80) {
            Log.i(ctx, "oneshot", "失败：JD 太短")
            return R(false, jd = jd, error = "等了 ${jdWaited / 1000} 秒，JD 只有 ${jd.length} 字——请确认停在岗位详情页", stage = "读 JD")
        }
        db.saveJd(job.id, jd)

        // 3. 核对页面上的公司名（防打错分）
        val pageText = StringBuilder()
        walk(root, 0) { n -> n.text?.toString()?.let { pageText.append(it).append('|') } }
        val page = pageText.toString()
        val want = Keys.normCompany(job.company)
        val hit = want.isNotBlank() && page.contains(want)
        Log.i(ctx, "oneshot", "公司核对：目标「$want」页面命中=$hit")
        // 页面上的公司名（用于显示）
        val pageCompany = runCatching {
            val m = Regex("([\\u4e00-\\u9fa5A-Za-z0-9]{2,12})(有限公司|股份有限公司|科技|集团)")
            m.find(page)?.groupValues?.get(0).orEmpty()
        }.getOrDefault("")

        // 4. 调模型
        val resume = ResumeStore.current(ctx)
        if (resume.isBlank()) {
            return R(false, jd = jd, companyOnPage = hit, pageCompany = pageCompany,
                error = "没有简历，先去设置上传", stage = "简历")
        }
        Log.i(ctx, "oneshot", "调模型：skill + ${jd.length} 字 JD + ${resume.length} 字简历")
        val mt0 = System.currentTimeMillis()
        val v = LlmScorer.score(ctx, job, resume, jd)
        Log.i(ctx, "oneshot", "模型返回：ok=${v.ok} score=${v.score} err=${v.err} 耗时=${System.currentTimeMillis()-mt0}ms")
        if (!v.ok) {
            return R(false, jd = jd, companyOnPage = hit, pageCompany = pageCompany,
                verdict = v, error = v.err, stage = "调模型")
        }

        // 5. 落库
        val need = if (v.verify.isEmpty()) "" else "【待核实】" + v.verify.joinToString("；")
        db.saveScore(
            job.id, v.score, "",
            "",
            v.pros.joinToString("；"),
            (v.gaps + listOfNotNull(need.ifBlank { null })).joinToString("；"),
            v.advice
        )

        Log.i(ctx, "oneshot", "已写入 score：${job.id} = ${v.score} 分｜总耗时 ${System.currentTimeMillis()-t0}ms")
        return R(true, jd = jd, companyOnPage = hit, pageCompany = pageCompany, verdict = v)
    }

    private fun walk(n: AccessibilityNodeInfo?, depth: Int, fn: (AccessibilityNodeInfo) -> Unit) {
        if (n == null || depth > 40) return
        fn(n)
        for (i in 0 until runCatching { n.childCount }.getOrDefault(0)) {
            walk(runCatching { n.getChild(i) }.getOrNull(), depth + 1, fn)
        }
    }
}
