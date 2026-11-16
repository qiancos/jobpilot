package com.jobpilot.pad.collect

import android.webkit.WebView
import com.jobpilot.pad.data.Job
import org.json.JSONArray
import org.json.JSONTokener

/**
 * 采集器：在职位列表页滚动几轮，把卡片读出来。
 *
 * 只读，不点任何按钮——采集失败不影响账号安全，所以这里不做熔断，只做重试预算。
 */
object Collector {

    private val SCROLL_JS =
        "(function(){window.scrollBy(0, Math.max(600, document.body.scrollHeight)); return 1;})()"

    private val EXTRACT_JS = """
        (function(){
          var out=[];
          // 卡片单位是 div.job-card-wrap（含 li.job-card-box + .job-card-footer），
          // 公司和地区在 footer 里，对 li 做 querySelector 拿不到。
          var wraps=document.querySelectorAll('div.job-card-wrap');
          for (var i=0;i<wraps.length;i++){
            var w=wraps[i];
            var a=w.querySelector('a.job-name');
            if(!a) continue;
            var sal=w.querySelector('.job-salary');
            // 实测：地区在 span.company-location；列表卡片里没有薪资（只在详情页）
            var area=w.querySelector('.company-location')||w.querySelector('.job-area');
            var comp=w.querySelector('.boss-name')||w.querySelector('.company-name');
            var tagNodes=w.querySelectorAll('.tag-list li');
            var tags=[];
            for (var t=0;t<tagNodes.length && t<4;t++){ tags.push((tagNodes[t].innerText||'').trim()); }
            out.push({
              href: a.getAttribute('href')||'',
              title: (a.innerText||'').trim(),
              salary: sal?(sal.innerText||'').trim():'',
              area: area?(area.innerText||'').trim():'',
              company: comp?(comp.innerText||'').trim():'',
              tags: tags.join('/')
            });
          }
          return JSON.stringify(out);
        })()
    """.trimIndent()

    /**
     * @param rounds 滚动轮数，每轮等 1.5s 让懒加载渲染
     */
    fun collect(web: WebView, keyword: String, rounds: Int = 3, onDone: (List<Job>) -> Unit) {
        val acc = LinkedHashMap<String, Job>()
        var round = 0

        fun step() {
            if (round >= rounds) {
                onDone(acc.values.toList())
                return
            }
            round++
            web.evaluateJavascript(SCROLL_JS, null)
            web.postDelayed({
                web.evaluateJavascript(EXTRACT_JS) { raw ->
                    parse(raw, keyword).forEach { j ->
                        if (j.id.isNotEmpty() && j.title.isNotEmpty()) acc[j.id] = j
                    }
                    web.postDelayed({ step() }, 300)
                }
            }, 1500)
        }

        step()
    }

    private fun parse(raw: String?, keyword: String): List<Job> {
        if (raw.isNullOrBlank() || raw == "null") return emptyList()
        val body = try {
            (JSONTokener(raw).nextValue() as? String) ?: raw
        } catch (e: Exception) {
            raw
        }
        return try {
            val arr = JSONArray(body)
            val now = System.currentTimeMillis()
            val out = ArrayList<Job>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val href = o.optString("href")
                val id = jobIdOf(href) ?: continue
                out.add(
                    Job(
                        id = id,
                        title = o.optString("title"),
                        company = o.optString("company"),
                        salary = o.optString("salary"),
                        area = o.optString("area"),
                        tags = o.optString("tags"),
                        href = if (href.startsWith("http")) href else "https://www.zhipin.com$href",
                        keyword = keyword,
                        collectedAt = now,
                        source = "web",
                    )
                )
            }
            out
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun jobIdOf(href: String): String? {
        val i = href.indexOf("job_detail/")
        if (i < 0) return null
        val rest = href.substring(i + "job_detail/".length)
        val end = rest.indexOf(".html")
        return if (end > 0) rest.substring(0, end) else rest.take(40).ifBlank { null }
    }
}
