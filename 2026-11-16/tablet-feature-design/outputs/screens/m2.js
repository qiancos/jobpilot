document.getElementById('board').insertAdjacentHTML('beforeend', `
<section class="shot" data-id="s2" data-module="m2" data-title="采集台" data-desc="设定条件，只读采集，实时看命中与去重">
  <div class="cap"><i>02</i><b>采集台</b><span>原生控件树只读采集 · 规则先淘汰</span></div>
  <div class="screen">
    <div class="rail-slot" data-active="s3"></div>
    <div class="main">
      <div class="appbar">
        <div class="tt"><h2>采集台</h2><div class="sub">BOSS 原生 App · 只读不投 · 第 7 批 · 07:30 定时</div></div>
        <div class="right">
          <div class="status"><span class="dot"></span>BOSS 在前台</div>
          <div class="status"><span class="dot"></span>控件命中率 96%</div>
          <div class="status">网页版降级 <b style="margin-left:2px">备用</b></div>
        </div>
      </div>
      <div class="content" style="display:grid;grid-template-columns:424px 1fr;gap:14px">
        <div class="card" style="display:flex;flex-direction:column;padding:18px 20px">
          <div class="card-h"><h3>采集条件</h3><span class="sub">预设：Java 实习</span><span class="r"><span class="pill">可保存</span></span></div>

          <div style="margin-top:2px">
            <div class="kicker" style="margin-bottom:8px">关键词</div>
            <div style="display:flex;flex-wrap:wrap;gap:7px">
              <span class="chip on">Java</span><span class="chip on">后端开发</span><span class="chip on">全栈</span>
              <span class="chip on">Java实习</span><span class="chip ghost">+ 添加</span>
            </div>
          </div>

          <div style="margin-top:16px">
            <div class="kicker" style="margin-bottom:8px">城市</div>
            <div style="display:flex;gap:7px"><span class="chip on">广州</span><span class="chip on">深圳</span><span class="chip">居家/远程</span></div>
          </div>

          <div style="margin-top:16px">
            <div class="kicker" style="margin-bottom:8px">薪资范围</div>
            <div style="display:flex;align-items:center;justify-content:space-between;font-size:12.5px;margin-bottom:7px"><b>6K</b><span class="faint">实习按天折算</span><b>15K</b></div>
            <div class="bar" style="height:6px;background:#EDF0F5"><i style="margin-left:12%;width:62%"></i></div>
            <div style="display:flex;justify-content:space-between;margin-top:6px" class="tiny faint"><span>低于 6K 自动淘汰</span><span>面议保留</span></div>
          </div>

          <div style="margin-top:16px">
            <div class="kicker" style="margin-bottom:8px">经验 / 学历</div>
            <div style="display:flex;gap:7px;flex-wrap:wrap"><span class="chip on">经验不限</span><span class="chip on">在校/应届</span><span class="chip on">1 年以内</span><span class="chip">本科</span></div>
          </div>

          <div style="margin-top:16px">
            <div class="kicker" style="margin-bottom:8px">排除词（命中即跳过）</div>
            <div style="display:flex;gap:6px;flex-wrap:wrap">
              <span class="chip sm" style="background:var(--red-soft);color:var(--red)">外包</span>
              <span class="chip sm" style="background:var(--red-soft);color:var(--red)">驻场</span>
              <span class="chip sm" style="background:var(--red-soft);color:var(--red)">硕士</span>
              <span class="chip sm" style="background:var(--red-soft);color:var(--red)">3-5年</span>
              <span class="chip sm" style="background:var(--red-soft);color:var(--red)">销售</span>
            </div>
          </div>

          <div style="margin-top:16px;padding:11px 12px;border:1px solid var(--line);border-radius:12px;background:#FBFCFE">
            <div class="row"><span style="font-size:12px;font-weight:650">数据源</span><span class="pill g" style="margin-left:auto">主通道</span></div>
            <div class="tiny muted" style="margin-top:5px;line-height:1.65">无障碍读 BOSS 控件树<br>字段最全：岗位 / 薪资 / 公司 / HR / 活跃度</div>
          </div>

          <div style="margin-top:14px">
            <div class="row"><span class="kicker">采集深度</span><span class="tiny faint" style="margin-left:auto">约 40 条 / 2 分钟</span></div>
            <div style="display:flex;gap:6px;margin-top:8px"><span class="chip sm">3 屏</span><span class="chip sm on">5 屏</span><span class="chip sm">8 屏</span><span class="chip sm">到底</span></div>
          </div>

          <div style="margin-top:auto;padding-top:16px">
            <button class="btn primary block lg" data-goto="s3"><svg viewBox="0 0 24 24"><path d="M8 5l11 7-11 7z"/></svg>开始采集</button>
            <div class="tiny faint" style="text-align:center;margin-top:8px">开始前会校验 BOSS 是否在前台</div>
          </div>
        </div>

        <div style="display:flex;flex-direction:column;gap:14px;min-height:0">
          <div class="card" style="flex:1;display:flex;flex-direction:column">
            <div class="card-h">
              <h3>正在采集</h3><span class="pill t">第 3 屏 / 5</span>
              <span class="r"><span class="tiny faint">预计剩余 48 秒</span><button class="btn sm ghost">暂停</button></span>
            </div>
            <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:10px">
              <div style="background:#F7F9FC;border-radius:12px;padding:11px 12px"><div class="tiny faint">已读卡片</div><div style="font-size:24px;font-weight:740;margin-top:4px">23</div></div>
              <div style="background:#F7F9FC;border-radius:12px;padding:11px 12px"><div class="tiny faint">命中候选</div><div style="font-size:24px;font-weight:740;margin-top:4px;color:var(--brand-ink)">12</div></div>
              <div style="background:#F7F9FC;border-radius:12px;padding:11px 12px"><div class="tiny faint">去重跳过</div><div style="font-size:24px;font-weight:740;margin-top:4px">5</div></div>
              <div style="background:#F7F9FC;border-radius:12px;padding:11px 12px"><div class="tiny faint">规则淘汰</div><div style="font-size:24px;font-weight:740;margin-top:4px">6</div></div>
            </div>
            <div style="margin-top:14px"><div class="bar"><i style="width:58%"></i></div><div class="row tiny faint" style="margin-top:6px"><span>23 / 40 条</span><span class="spacer"></span><span>重复率 21% · 距 80% 停止还很远</span></div></div>

            <div style="margin-top:14px;display:flex;flex-direction:column;gap:8px;flex:1;min-height:0">
              <div style="display:flex;align-items:center;gap:12px;border:1px solid var(--line);border-radius:12px;padding:9px 12px">
                <div style="flex:1"><div class="ttl">java开发实习生</div><div class="sub2">艾特数字 · 广州·花都 · 3-4K</div></div>
                <span class="pill">刚刚活跃</span><span class="scoretag">88</span>
              </div>
              <div style="display:flex;align-items:center;gap:12px;border:1px solid var(--line);border-radius:12px;padding:9px 12px">
                <div style="flex:1"><div class="ttl">java开发工程师（全栈）实习生</div><div class="sub2">青蓝智慧 · 广州·番禺 · 130-200元/天</div></div>
                <span class="pill">刚刚活跃</span><span class="scoretag">86</span>
              </div>
              <div style="display:flex;align-items:center;gap:12px;border:1px solid var(--line);border-radius:12px;padding:9px 12px">
                <div style="flex:1"><div class="ttl">开发实习工程师</div><div class="sub2">广州矣润技术 · 广州·天河 · 100-130元/天</div></div>
                <span class="pill">刚刚活跃</span><span class="scoretag">84</span>
              </div>
              <div style="display:flex;align-items:center;gap:12px;border:1px solid var(--line);border-radius:12px;padding:9px 12px">
                <div style="flex:1"><div class="ttl">【27应届】后端开发实习生</div><div class="sub2">穹矩科技 · 广州·番禺 · 6-7K</div></div>
                <span class="pill">刚刚活跃</span><span class="scoretag">83</span>
              </div>
              <div style="display:flex;align-items:center;gap:12px;border:1px solid var(--line);border-radius:12px;padding:9px 12px;opacity:.55">
                <div style="flex:1"><div class="ttl">Java 开发工程师（3-5年）</div><div class="sub2">已按排除词跳过 · 经验不符</div></div>
                <span class="pill r">淘汰</span>
              </div>
            </div>
          </div>

          <div class="card" style="height:172px;padding:14px 18px">
            <div class="card-h" style="margin-bottom:8px"><h3>run_log</h3><span class="sub">每次采集都留痕</span><span class="r tiny faint">实时</span></div>
            <div class="mono" style="font-size:11px;line-height:1.95;color:#475467">
              <div><span class="faint">21:02:14</span> rootInActiveWindow → com.hpbr.bosszhipin <span style="color:var(--green)">✓</span></div>
              <div><span class="faint">21:02:15</span> byId(view_job_card)=3 · 字段完整 11/11</div>
              <div><span class="faint">21:02:17</span> ACTION_SCROLL_FORWARD → 新卡 3 · 重复 0</div>
              <div><span class="faint">21:02:18</span> INSERT OR IGNORE 3 条 · 累计 23</div>
              <div><span class="faint">21:02:18</span> 规则淘汰 2 条（3-5年 / 外包）</div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div style="position:absolute;right:26px;bottom:26px;background:var(--nav);color:#fff;border-radius:22px;padding:9px 14px;display:flex;align-items:center;gap:8px;box-shadow:0 12px 30px rgba(0,0,0,.28)">
      <span class="dot" style="background:#22E2D8"></span><span style="font-size:12px">采集中 23</span><span style="font-size:10px;color:#7E93AB">悬浮球</span>
    </div>
  </div>
</section>

<section class="shot" data-id="s3" data-module="m2" data-title="待投清单" data-desc="30 秒勾出 8 家，去重与配额同时可见">
  <div class="cap"><i>03</i><b>待投清单</b><span>人的决策点：勾选，而不是被自动投递</span></div>
  <div class="screen">
    <div class="rail-slot" data-active="s3"></div>
    <div class="main">
      <div class="appbar">
        <div class="tt"><h2>待投清单</h2><div class="sub">32 个候选 · 已选 8 家 · 最近采集 今天 07:30</div></div>
        <div class="right">
          <div class="status">匹配分 <b style="margin-left:2px">降序</b></div>
          <div class="status">只看已选</div>
          <button class="btn ghost sm" data-goto="s2">重新采集</button>
        </div>
      </div>
      <div class="content">
        <div class="row" style="flex:none">
          <div style="display:flex;align-items:center;gap:8px;height:38px;padding:0 12px;border:1px solid var(--line);border-radius:10px;background:#fff;width:290px">
            <svg viewBox="0 0 24 24" style="width:15px;height:15px;stroke:#98A2B3;fill:none;stroke-width:1.8"><circle cx="11" cy="11" r="7"/><path d="M16.5 16.5L21 21"/></svg>
            <span class="tiny faint">搜索岗位 / 公司 / 关键词</span>
          </div>
          <span class="chip on">今日活跃 6</span><span class="chip">27 届 12</span><span class="chip">排除已投</span><span class="chip">只看已选</span>
          <div class="spacer"></div>
          <span class="tiny faint">规则已淘汰 7 家 · 去重 5 家</span>
        </div>

        <div style="display:grid;grid-template-columns:1fr 330px;gap:14px;flex:1;min-height:0">
          <div class="card pad0" style="display:flex;flex-direction:column;overflow:hidden">
            <div class="tbl" style="flex:1">
              <div class="th" style="grid-template-columns:30px 1.6fr 96px 1fr 92px 50px">
                <span></span><span>岗位 / 公司</span><span>薪资</span><span>标签</span><span>HR 活跃</span><span style="text-align:right">匹配</span>
              </div>
              <div class="tr" style="grid-template-columns:30px 1.6fr 96px 1fr 92px 50px">
                <span class="cb on"></span>
                <div style="min-width:0"><div class="ttl">java开发实习生</div><div class="sub2">艾特数字 · 广州·花都·花都广场</div></div>
                <div style="font-weight:650">3-4K</div>
                <div class="row" style="gap:5px"><span class="pill sm">27届</span><span class="pill sm">实习</span></div>
                <span class="tiny" style="color:var(--green)">刚刚活跃</span><span class="scoretag" style="margin-left:auto">88</span>
              </div>
              <div class="tr" style="grid-template-columns:30px 1.6fr 96px 1fr 92px 50px">
                <span class="cb on"></span>
                <div style="min-width:0"><div class="ttl">java开发工程师（全栈）实习生</div><div class="sub2">青蓝智慧 · 广州·番禺·华南</div></div>
                <div style="font-weight:650">130-200元/天</div>
                <div class="row" style="gap:5px"><span class="pill sm">全栈</span><span class="pill sm">27届</span></div>
                <span class="tiny" style="color:var(--green)">刚刚活跃</span><span class="scoretag" style="margin-left:auto">86</span>
              </div>
              <div class="tr" style="grid-template-columns:30px 1.6fr 96px 1fr 92px 50px">
                <span class="cb on"></span>
                <div style="min-width:0"><div class="ttl">开发实习工程师</div><div class="sub2">广州矣润技术 · 广州·天河·天河公园</div></div>
                <div style="font-weight:650">100-130元/天</div>
                <div class="row" style="gap:5px"><span class="pill sm">Java</span><span class="pill sm">可转正</span></div>
                <span class="tiny" style="color:var(--green)">刚刚活跃</span><span class="scoretag" style="margin-left:auto">84</span>
              </div>
              <div class="tr" style="grid-template-columns:30px 1.6fr 96px 1fr 92px 50px">
                <span class="cb on"></span>
                <div style="min-width:0"><div class="ttl">【27应届】后端开发实习生</div><div class="sub2">穹矩科技 · 广州·番禺·市桥</div></div>
                <div style="font-weight:650">6-7K</div>
                <div class="row" style="gap:5px"><span class="pill sm">后端</span><span class="pill sm">27届</span></div>
                <span class="tiny" style="color:var(--green)">刚刚活跃</span><span class="scoretag" style="margin-left:auto">83</span>
              </div>
              <div class="tr" style="grid-template-columns:30px 1.6fr 96px 1fr 92px 50px">
                <span class="cb on"></span>
                <div style="min-width:0"><div class="ttl">java实习生（任务制）</div><div class="sub2">库莱朋 · 广州·黄埔·科学城</div></div>
                <div style="font-weight:650">150-200元/天</div>
                <div class="row" style="gap:5px"><span class="pill sm">任务制</span></div>
                <span class="tiny" style="color:var(--green)">本周活跃</span><span class="scoretag" style="margin-left:auto">79</span>
              </div>
              <div class="tr" style="grid-template-columns:30px 1.6fr 96px 1fr 92px 50px">
                <span class="cb on"></span>
                <div style="min-width:0"><div class="ttl">java服务端（实习岗）</div><div class="sub2">景游游戏 · 广州·天河</div></div>
                <div style="font-weight:650">4-5K</div>
                <div class="row" style="gap:5px"><span class="pill sm">游戏</span><span class="pill sm">实习</span></div>
                <span class="tiny" style="color:var(--green)">在线</span><span class="scoretag mid" style="margin-left:auto">77</span>
              </div>
              <div class="tr" style="grid-template-columns:30px 1.6fr 96px 1fr 92px 50px">
                <span class="cb on"></span>
                <div style="min-width:0"><div class="ttl">Java后端研发工程师（27校招）</div><div class="sub2">嘉为科技 · 广州</div></div>
                <div style="font-weight:650">100-150元/天</div>
                <div class="row" style="gap:5px"><span class="pill sm">校招</span><span class="pill sm">转正</span></div>
                <span class="tiny" style="color:var(--green)">刚刚活跃</span><span class="scoretag mid" style="margin-left:auto">75</span>
              </div>
              <div class="tr" style="grid-template-columns:30px 1.6fr 96px 1fr 92px 50px">
                <span class="cb on"></span>
                <div style="min-width:0"><div class="ttl">AI 应用开发实习生</div><div class="sub2">文云文化 · 广州·天河·棠下</div></div>
                <div style="font-weight:650">150-300元/天</div>
                <div class="row" style="gap:5px"><span class="pill sm">RAG</span><span class="pill sm">Agent</span></div>
                <span class="tiny" style="color:var(--green)">刚刚活跃</span><span class="scoretag mid" style="margin-left:auto">73</span>
              </div>
              <div class="tr" style="grid-template-columns:30px 1.6fr 96px 1fr 92px 50px">
                <span class="cb"></span>
                <div style="min-width:0"><div class="ttl">Python后端开发工程师（实习）</div><div class="sub2">芯海智控 · 广州·番禺·大学城</div></div>
                <div style="font-weight:650">200-250元/天</div>
                <div class="row" style="gap:5px"><span class="pill sm">Python</span></div>
                <span class="tiny faint">月内活跃</span><span class="scoretag mid" style="margin-left:auto">68</span>
              </div>
              <div class="tr" style="grid-template-columns:30px 1.6fr 96px 1fr 92px 50px">
                <span class="cb"></span>
                <div style="min-width:0"><div class="ttl">产品实习生（潮玩盲盒）</div><div class="sub2">奥飞娱乐 · 广州·天河·天河北</div></div>
                <div style="font-weight:650">140-160元/天</div>
                <div class="row" style="gap:5px"><span class="pill sm">备选方向</span></div>
                <span class="tiny faint">在线</span><span class="scoretag low" style="margin-left:auto">61</span>
              </div>
            </div>
          </div>

          <div style="display:flex;flex-direction:column;gap:14px;min-height:0">
            <div class="card">
              <div class="card-h"><h3>本批摘要</h3><span class="sub">11-16 07:30</span></div>
              <div style="display:grid;grid-template-columns:1fr 1fr;gap:9px">
                <div style="background:#F7F9FC;border-radius:10px;padding:10px"><div class="tiny faint">采集</div><div style="font-size:19px;font-weight:730;margin-top:3px">30</div></div>
                <div style="background:#F7F9FC;border-radius:10px;padding:10px"><div class="tiny faint">命中</div><div style="font-size:19px;font-weight:730;margin-top:3px">18</div></div>
                <div style="background:#F7F9FC;border-radius:10px;padding:10px"><div class="tiny faint">去重</div><div style="font-size:19px;font-weight:730;margin-top:3px">5</div></div>
                <div style="background:var(--red-soft);border-radius:10px;padding:10px"><div class="tiny faint">规则淘汰</div><div style="font-size:19px;font-weight:730;margin-top:3px;color:var(--red)">7</div></div>
              </div>
              <div class="divider" style="margin:13px 0"></div>
              <div class="tiny muted" style="line-height:1.8">
                淘汰构成：3-5 年经验 ×3 · 外包驻场 ×2 · 要求硕士 ×1 · 销售岗 ×1<br>
                去重依据：<span class="mono" style="color:#344054">company|title</span> 哈希 + 公司名相似度 0.75
              </div>
            </div>

            <div class="card" style="flex:1">
              <div class="card-h"><h3>今日配额</h3><span class="pill g">标准档</span></div>
              <div class="row" style="gap:16px">
                <div class="ring sm" style="--v:40"><div class="in"><b>6</b><span>/ 15</span></div></div>
                <div style="flex:1">
                  <div class="tiny faint">本批 +8 后</div>
                  <div style="font-size:15px;font-weight:700;margin-top:3px">14 / 15</div>
                  <div class="tiny" style="color:var(--amber);margin-top:4px">仅剩 1 家余额</div>
                </div>
              </div>
              <div class="divider" style="margin:13px 0"></div>
              <div class="tiny faint" style="line-height:1.8">超配额会自动截断，并在批次确认页显性提示；不会静默多投。</div>
            </div>

            <div class="card" style="background:#FFFDF7;border-color:#F3E3BE">
              <div class="row"><span class="dot a"></span><span style="font-size:12px;font-weight:650">为什么只能人勾选</span></div>
              <div class="tiny" style="color:#8A6A22;line-height:1.75;margin-top:6px">自动海投会把「投递」变成噪声；这里的 8 家决定今晚的全部动作。</div>
            </div>
          </div>
        </div>

        <div style="height:64px;background:var(--nav);border-radius:14px;display:flex;align-items:center;gap:16px;padding:0 20px;flex:none">
          <span style="color:#fff;font-size:13px">已选 <b style="font-size:17px">8</b> 家</span>
          <span style="color:#6E829B;font-size:12px">·</span>
          <span style="color:#A9BACD;font-size:12px">预计执行 7 分 30 秒</span>
          <span style="color:#6E829B;font-size:12px">·</span>
          <span style="color:#A9BACD;font-size:12px">配额 14 / 15</span>
          <div class="spacer"></div>
          <span style="color:#7E93AB;font-size:11.5px">下一步：AI 起稿 + 9 条闸门</span>
          <button class="btn primary" data-goto="s5">生成话术<svg viewBox="0 0 24 24"><path d="M5 12h14M13 6l6 6-6 6"/></svg></button>
        </div>
      </div>
    </div>
  </div>
</section>

<section class="shot" data-id="s4" data-module="m2" data-title="岗位详情 · 打分卡" data-desc="值不值得投：分数可拆解，优势引用 JD 原文">
  <div class="cap"><i>04</i><b>岗位详情 · 打分卡</b><span>优势 / 不足 / 建议，三件套缺一不可</span></div>
  <div class="screen">
    <div class="rail-slot" data-active="s3"></div>
    <div class="main">
      <div class="appbar">
        <div class="tt"><h2>艾特数字 · java开发实习生</h2><div class="sub">待投清单 / 第 1 家 · 广州·花都·花都广场 · 3-4K · 刚刚活跃</div></div>
        <div class="right">
          <button class="btn ghost sm">忽略</button>
          <button class="btn ghost sm">加入黑名单</button>
          <button class="btn primary sm" data-goto="s5">加入待投并写话术</button>
        </div>
      </div>
      <div class="content" style="display:grid;grid-template-columns:1fr 446px;gap:14px">
        <div class="card" style="display:flex;flex-direction:column;overflow:hidden">
          <div class="row" style="gap:10px">
            <div style="width:46px;height:46px;border-radius:12px;background:var(--brand-soft);color:var(--brand-ink);display:flex;align-items:center;justify-content:center;font-weight:750;font-size:15px">艾</div>
            <div style="flex:1">
              <div class="row"><span style="font-size:15px;font-weight:700">艾特数字</span><span class="pill">20-99 人</span><span class="pill">未融资</span><span class="pill g">刚刚活跃</span></div>
              <div class="tiny faint" style="margin-top:4px">移动互联网 · 广州·花都 · HR 招聘专员 · 今日回复</div>
            </div>
            <div style="text-align:right"><div style="font-size:20px;font-weight:740">3-4K</div><div class="tiny faint">13 薪 · 实习</div></div>
          </div>
          <div class="divider" style="margin:14px 0"></div>
          <div class="row" style="gap:6px;flex-wrap:wrap">
            <span class="chip sm">Java</span><span class="chip sm">Spring Boot</span><span class="chip sm">MySQL</span><span class="chip sm">Vue</span><span class="chip sm">27 届</span><span class="chip sm">每周 5 天</span>
          </div>

          <div style="margin-top:16px;overflow:hidden">
            <div class="kicker">岗位职责</div>
            <div style="font-size:12.5px;line-height:1.9;color:#344054;margin-top:8px">
              <div style="display:flex;gap:8px"><span class="faint">·</span><span>参与公司 <span class="kw hit">Java 后端</span> 业务系统的开发与维护，按需求完成模块设计与编码；</span></div>
              <div style="display:flex;gap:8px;margin-top:2px"><span class="faint">·</span><span>配合产品与前端完成接口联调，输出接口文档，参与上线与问题排查；</span></div>
              <div style="display:flex;gap:8px;margin-top:2px"><span class="faint">·</span><span>根据需求做<span class="kw hit">解决方案和技术实现</span>，能独立推进小模块交付。</span></div>
            </div>
            <div class="kicker" style="margin-top:16px">任职要求</div>
            <div style="font-size:12.5px;line-height:1.9;color:#344054;margin-top:8px">
              <div style="display:flex;gap:8px"><span class="faint">·</span><span>本科及以上，计算机相关专业，<span class="kw hit">27 届</span>优先，可实习 3 个月以上；</span></div>
              <div style="display:flex;gap:8px;margin-top:2px"><span class="faint">·</span><span>熟悉 <span class="kw hit">Spring Boot</span>、<span class="kw hit">MyBatis</span>、<span class="kw hit">MySQL</span>，了解 Redis 加分；</span></div>
              <div style="display:flex;gap:8px;margin-top:2px"><span class="faint">·</span><span>有 <span class="kw hit">uni-app</span> / 微信小程序经验优先；</span></div>
              <div style="display:flex;gap:8px;margin-top:2px"><span class="faint">·</span><span>了解 <span class="kw miss">Kafka</span>、<span class="kw miss">微服务治理</span> 者优先（非硬性）。</span></div>
            </div>

            <div class="kicker" style="margin-top:16px">你需要知道的</div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;margin-top:8px">
              <div style="background:#F7F9FC;border-radius:10px;padding:10px 12px"><div class="tiny faint">团队规模</div><div style="font-size:12.5px;font-weight:600;margin-top:3px">后端 6 人 · 直属技术负责人</div></div>
              <div style="background:#F7F9FC;border-radius:10px;padding:10px 12px"><div class="tiny faint">转正</div><div style="font-size:12.5px;font-weight:600;margin-top:3px">实习 3 个月可评估转正</div></div>
              <div style="background:#F7F9FC;border-radius:10px;padding:10px 12px"><div class="tiny faint">面试</div><div style="font-size:12.5px;font-weight:600;margin-top:3px">线下一面 · 花都广场</div></div>
              <div style="background:#F7F9FC;border-radius:10px;padding:10px 12px"><div class="tiny faint">风险</div><div style="font-size:12.5px;font-weight:600;margin-top:3px;color:var(--amber)">薪资偏低，需确认实习补贴</div></div>
            </div>
          </div>
        </div>

        <div style="display:flex;flex-direction:column;gap:14px;min-height:0">
          <div class="card">
            <div class="card-h"><h3>匹配打分</h3><span class="sub">规则 + 事实库，可拆解</span><span class="r pill t">可解释</span></div>
            <div class="row" style="gap:18px;align-items:center">
              <div class="ring" style="--v:78"><div class="in"><b>78</b><span>/ 100 · 建议投</span></div></div>
              <div style="flex:1;display:flex;flex-direction:column;gap:7px">
                <div><div class="row tiny"><span>硬性条件</span><span class="spacer"></span><b>40 / 40</b></div><div class="bar thin" style="margin-top:3px"><i class="g" style="width:100%"></i></div></div>
                <div><div class="row tiny"><span>技能命中</span><span class="spacer"></span><b>22 / 25</b></div><div class="bar thin" style="margin-top:3px"><i style="width:88%"></i></div></div>
                <div><div class="row tiny"><span>项目可对证</span><span class="spacer"></span><b>14 / 20</b></div><div class="bar thin" style="margin-top:3px"><i class="a" style="width:70%"></i></div></div>
                <div><div class="row tiny"><span>公司质量</span><span class="spacer"></span><b>8 / 10</b></div><div class="bar thin" style="margin-top:3px"><i style="width:80%"></i></div></div>
                <div><div class="row tiny"><span>时效</span><span class="spacer"></span><b>5 / 5</b></div><div class="bar thin" style="margin-top:3px"><i class="g" style="width:100%"></i></div></div>
              </div>
            </div>
            <div class="divider" style="margin:14px 0"></div>
            <div class="row" style="flex-wrap:wrap;gap:6px"><span class="tiny faint" style="width:52px">JD 命中</span><span class="pill g">Spring</span><span class="pill g">MyBatis</span><span class="pill g">MySQL</span><span class="pill g">uni-app</span><span class="pill g">27 届</span></div>
            <div class="row" style="flex-wrap:wrap;gap:6px;margin-top:7px"><span class="tiny faint" style="width:52px">JD 缺失</span><span class="pill r">Kafka</span><span class="pill r">微服务治理</span><span class="tiny faint">→ 话术里一个字都不许出现</span></div>
          </div>

          <div class="card" style="flex:1;display:flex;flex-direction:column;gap:9px">
            <div class="card-h" style="margin-bottom:2px"><h3>三件套</h3><span class="sub">优势必须引用 JD 原文</span></div>
            <div style="background:var(--green-soft);border-radius:12px;padding:11px 13px">
              <div style="font-size:11px;font-weight:700;color:var(--green);letter-spacing:.06em">优势</div>
              <div style="font-size:12.5px;line-height:1.7;margin-top:5px">JD 要求「按需求做解决方案和技术实现」，你独立做过摄影预约小程序全流程（uni-app + SpringBoot + 微信支付 + OSS），从需求到上线一个人推完。</div>
            </div>
            <div style="background:var(--red-soft);border-radius:12px;padding:11px 13px">
              <div style="font-size:11px;font-weight:700;color:var(--red);letter-spacing:.06em">不足</div>
              <div style="font-size:12.5px;line-height:1.7;margin-top:5px">JD 提到 Kafka 与微服务治理，你的项目里没有对应经历；薪资 3-4K 也低于你的期望区间。</div>
            </div>
            <div style="background:var(--blue-soft);border-radius:12px;padding:11px 13px">
              <div style="font-size:11px;font-weight:700;color:var(--blue);letter-spacing:.06em">建议</div>
              <div style="font-size:12.5px;line-height:1.7;margin-top:5px">话术只打「独立交付 + 小程序全流程」，不要提 Kafka；结尾问到岗时间与转正评估节点。</div>
            </div>
            <div class="tiny faint" style="margin-top:auto">打分卡 v1 · 事实库 lexicon v1.4 · 抽查要求：20 条里优势全部引用 JD 原文</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</section>
`);
