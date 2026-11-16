document.getElementById('board').insertAdjacentHTML('beforeend', `
<section class="shot" data-id="s1" data-module="m1" data-title="今日驾驶舱" data-desc="今日流水线、配额、待跟进与风险，一屏决策">
  <div class="cap"><i>01</i><b>今日驾驶舱</b><span>今天走到哪一步 · 下一步点哪里</span></div>
  <div class="screen">
    <div class="rail-slot" data-active="s1"></div>
    <div class="main">
      <div class="appbar">
        <div class="tt"><h2>早上好，张三</h2><div class="sub">2026-11-16 周一 · 广州 · 距今晚批次 3 小时 12 分</div></div>
        <div class="right">
          <div class="status"><span class="dot"></span>BOSS 前台</div>
          <div class="status"><span class="dot"></span>无障碍 已开启</div>
          <div class="status">今日配额 <b style="margin:0 2px">6</b>/15</div>
          <div class="status">标准档 · 45–90s</div>
        </div>
      </div>
      <div class="content">
        <div class="cmd">
          <div class="ico"><svg viewBox="0 0 24 24"><path d="M12 3l1.7 5.3L19 10l-5.3 1.7L12 17l-1.7-5.3L5 10l5.3-1.7z"/><path d="M18.5 15.5l.8 2.2 2.2.8-2.2.8-.8 2.2-.8-2.2-2.2-.8 2.2-.8z"/></svg></div>
          <div class="ph">试试说：<b>帮我找 27 届 Java 实习，广州或深圳，本周活跃，排除外包</b></div>
          <span class="pill">自然语言任务</span>
          <button class="btn primary sm" data-goto="s2">解析并采集</button>
        </div>

        <div style="display:grid;grid-template-columns:1fr 396px;gap:14px;height:246px">
          <div class="card" style="padding:20px 22px;display:flex;gap:26px;position:relative;overflow:hidden">
            <div style="position:absolute;right:-40px;top:-70px;width:220px;height:220px;border-radius:50%;background:radial-gradient(circle,#D9F5F2 0%,rgba(217,245,242,0) 70%)"></div>
            <div style="flex:1;position:relative">
              <div class="row">
                <span class="pill t">今晚 21:00</span>
                <span class="pill">第 6 批 · 标准档</span>
                <span class="pill g">闸门全过</span>
              </div>
              <div style="display:flex;align-items:baseline;gap:10px;margin-top:12px">
                <div style="font-size:46px;font-weight:760;letter-spacing:-.04em;line-height:1">6</div>
                <div style="font-size:22px;color:#C3CAD5;font-weight:500">/ 8</div>
                <div style="font-size:13px;color:var(--sub);margin-left:4px">已 L3 送达</div>
              </div>
              <div style="font-size:12.5px;color:var(--sub);margin-top:8px">剩余 2 家 · 预计 6 分 20 秒 · 上次中断于 21:04</div>
              <div style="display:flex;gap:10px;margin-top:18px">
                <button class="btn primary" data-goto="s8"><svg viewBox="0 0 24 24"><path d="M8 5l11 7-11 7z"/></svg>继续执行 2 家</button>
                <button class="btn ghost" data-goto="s9">查看核验报告</button>
                <button class="btn ghost" data-goto="s7">调整批次</button>
              </div>
              <div style="display:flex;align-items:center;gap:0;margin-top:20px">
                <div style="display:flex;flex-direction:column;gap:3px"><span class="faint tiny">07:30 采集</span><b style="font-size:13px">30 条</b></div>
                <div style="width:34px;height:1px;background:var(--line);margin:0 10px"></div>
                <div style="display:flex;flex-direction:column;gap:3px"><span class="faint tiny">12:30 审阅</span><b style="font-size:13px">勾选 8 家</b></div>
                <div style="width:34px;height:1px;background:var(--line);margin:0 10px"></div>
                <div style="display:flex;flex-direction:column;gap:3px"><span class="faint tiny">21:00 执行</span><b style="font-size:13px;color:var(--brand-ink)">进行中 6/8</b></div>
                <div style="width:34px;height:1px;background:var(--line);margin:0 10px"></div>
                <div style="display:flex;flex-direction:column;gap:3px"><span class="faint tiny">21:20 核验</span><b style="font-size:13px">待生成</b></div>
              </div>
            </div>
            <div style="display:flex;flex-direction:column;align-items:center;justify-content:center;gap:10px">
              <div class="ring" style="--v:75"><div class="in"><b>75%</b><span>批次完成度</span></div></div>
              <div class="tiny faint">按「家」计，非按步数</div>
            </div>
          </div>

          <div class="card">
            <div class="card-h"><h3>今日节奏</h3><span class="sub">后台只读，不打扰</span><span class="r pill g">1 项待办</span></div>
            <div style="display:flex;flex-direction:column">
              <div style="display:flex;gap:10px;padding:10px 0;border-bottom:1px dashed var(--line2)">
                <span class="mono tiny faint" style="width:38px">07:30</span><span class="dot" style="margin-top:5px"></span>
                <div style="flex:1"><div style="font-size:12.5px;font-weight:600">后台采集完成</div><div class="tiny faint" style="margin-top:2px">新增 30 · 去重 5 · 字段完整率 96%</div></div>
              </div>
              <div style="display:flex;gap:10px;padding:10px 0;border-bottom:1px dashed var(--line2)">
                <span class="mono tiny faint" style="width:38px">12:30</span><span class="dot" style="margin-top:5px"></span>
                <div style="flex:1"><div style="font-size:12.5px;font-weight:600">通勤审阅 8 家</div><div class="tiny faint" style="margin-top:2px">平均每家 22 秒 · 全部过闸</div></div>
              </div>
              <div style="display:flex;gap:10px;padding:10px 0;border-bottom:1px dashed var(--line2)">
                <span class="mono tiny faint" style="width:38px">21:00</span><span class="dot t" style="margin-top:5px"></span>
                <div style="flex:1"><div style="font-size:12.5px;font-weight:600;color:var(--brand-ink)">批量执行中</div><div class="tiny faint" style="margin-top:2px">已送达 6 · 失败 0 · 自动暂停等你继续</div></div>
              </div>
              <div style="display:flex;gap:10px;padding:10px 0">
                <span class="mono tiny faint" style="width:38px">21:20</span><span class="dot a" style="margin-top:5px"></span>
                <div style="flex:1"><div style="font-size:12.5px;font-weight:600">会话刷新</div><div class="tiny faint" style="margin-top:2px">1 条未读：文云文化 HR 要简历</div></div>
              </div>
            </div>
          </div>
        </div>

        <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:14px;height:104px">
          <div class="kpi"><div class="lab">候选岗位</div><div class="val">32<small>条</small></div><div class="delta" style="color:var(--green)">↑ 今日新增 12 条</div></div>
          <div class="kpi"><div class="lab">待投清单</div><div class="val">8<small>家</small></div><div class="delta">全部过闸 · 0 冲突</div></div>
          <div class="kpi"><div class="lab">今日送达 <span class="pill g sm">L3</span></div><div class="val">6<small>家</small></div><div class="delta">送达率 75% · 目标 ≥95%</div></div>
          <div class="kpi"><div class="lab">真人回复</div><div class="val">1<small>条</small></div><div class="delta" style="color:var(--brand-ink)">本周累计 3 · 北极星指标</div></div>
        </div>

        <div style="display:grid;grid-template-columns:1fr 1fr 380px;gap:14px;flex:1;min-height:0">
          <div class="card" style="display:flex;flex-direction:column">
            <div class="card-h"><h3>今日漏斗</h3><span class="sub">候选 → 约面</span><span class="r"><span class="pill">近 7 天</span></span></div>
            <div style="display:flex;flex-direction:column;gap:9px;flex:1;justify-content:center">
              <div style="display:flex;align-items:center;gap:10px"><span style="width:38px;text-align:right;font-size:11.5px;color:var(--sub)">候选</span><div class="bar" style="flex:1"><i style="width:100%"></i></div><b style="width:30px;text-align:right;font-size:12.5px">32</b></div>
              <div style="display:flex;align-items:center;gap:10px"><span style="width:38px;text-align:right;font-size:11.5px;color:var(--sub)">审批</span><div class="bar" style="flex:1"><i style="width:25%"></i></div><b style="width:30px;text-align:right;font-size:12.5px">8</b></div>
              <div style="display:flex;align-items:center;gap:10px"><span style="width:38px;text-align:right;font-size:11.5px;color:var(--sub)">送达</span><div class="bar" style="flex:1"><i class="g" style="width:19%"></i></div><b style="width:30px;text-align:right;font-size:12.5px">6</b></div>
              <div style="display:flex;align-items:center;gap:10px"><span style="width:38px;text-align:right;font-size:11.5px;color:var(--sub)">已读</span><div class="bar" style="flex:1"><i class="g" style="width:9%"></i></div><b style="width:30px;text-align:right;font-size:12.5px">3</b></div>
              <div style="display:flex;align-items:center;gap:10px"><span style="width:38px;text-align:right;font-size:11.5px;color:var(--sub)">回复</span><div class="bar" style="flex:1"><i class="a" style="width:3%"></i></div><b style="width:30px;text-align:right;font-size:12.5px">1</b></div>
            </div>
            <div class="tiny faint" style="margin-top:8px">送达率 18.8%（6/32）· 回复率 16.7%（1/6）· 目标回复率 ≥15%</div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;margin-top:10px">
              <div style="background:#F7F9FC;border-radius:10px;padding:9px 11px"><div class="tiny faint">本批送达率</div><div style="font-size:15px;font-weight:720;margin-top:3px">75%<span class="tiny faint" style="font-weight:400;margin-left:5px">历史 26%</span></div></div>
              <div style="background:#F7F9FC;border-radius:10px;padding:9px 11px"><div class="tiny faint">平均视觉宽度</div><div style="font-size:15px;font-weight:720;margin-top:3px">86<span class="tiny faint" style="font-weight:400;margin-left:5px">10-21 批 137</span></div></div>
            </div>
          </div>

          <div class="card" style="display:flex;flex-direction:column;gap:10px">
            <div class="card-h"><h3>待跟进</h3><span class="r pill a">2 条</span></div>
            <div style="border:1px solid var(--line);border-radius:12px;padding:11px 12px">
              <div class="row"><span class="pill b">要简历</span><span class="tiny faint" style="margin-left:auto">14:20</span></div>
              <div style="font-size:12.5px;font-weight:600;margin-top:7px">文云文化 · HR 陈女士</div>
              <div class="tiny muted" style="margin-top:3px">「方便发一份简历吗？」· AI 草稿已就绪</div>
              <div class="row" style="margin-top:9px"><button class="btn soft sm" data-goto="s11">去回复</button><span class="tiny faint">已等 42 分钟</span></div>
            </div>
            <div style="border:1px solid var(--line);border-radius:12px;padding:11px 12px">
              <div class="row"><span class="pill g">邀面</span><span class="tiny faint" style="margin-left:auto">明天 15:00</span></div>
              <div style="font-size:12.5px;font-weight:600;margin-top:7px">白码科技 · 一面（技术）</div>
              <div class="tiny muted" style="margin-top:3px">准备清单 3 项 · 腾讯会议链接已存</div>
            </div>
            <div class="row" style="gap:8px;padding:9px 2px;border-top:1px dashed var(--line2)">
              <span class="pill">问薪</span>
              <div style="flex:1;min-width:0"><div style="font-size:12px;font-weight:600">穹矩科技 · 期望薪资多少？</div><div class="tiny faint" style="margin-top:2px">草稿未生成 · 需要你先定报价</div></div>
              <span class="tiny faint">10-28</span>
            </div>
            <div class="row" style="margin-top:2px"><span class="tiny faint">共 14 个会话 · 3 条待跟进</span><span class="spacer"></span><span class="tiny" style="color:var(--brand-ink)">查看全部 →</span></div>
          </div>

          <div class="card" style="display:flex;flex-direction:column;gap:10px">
            <div class="card-h"><h3>风险与自检</h3><span class="r"><span class="pill g">整体正常</span></span></div>
            <div style="display:flex;gap:9px;align-items:flex-start;padding:9px 10px;background:var(--amber-soft);border-radius:10px">
              <span class="dot a" style="margin-top:5px"></span>
              <div><div style="font-size:12px;font-weight:650;color:#8A5600">2 条话术触发 R7 相似度</div><div class="tiny" style="color:#9A6A20;margin-top:2px">0.72–0.90 区间，仅提醒不拦截</div></div>
            </div>
            <div style="display:flex;gap:9px;align-items:flex-start;padding:9px 10px;background:#F7F9FC;border-radius:10px">
              <span class="dot" style="margin-top:5px"></span>
              <div><div style="font-size:12px;font-weight:650">无障碍服务运行中</div><div class="tiny faint" style="margin-top:2px">控件命中率 96% · 版本 v3</div></div>
            </div>
            <div style="display:flex;gap:9px;align-items:flex-start;padding:9px 10px;background:#F7F9FC;border-radius:10px">
              <span class="dot" style="margin-top:5px"></span>
              <div><div style="font-size:12px;font-weight:650">本周熔断 0 次</div><div class="tiny faint" style="margin-top:2px">连续 3 次 wrong_target 才停</div></div>
            </div>
            <div style="display:flex;gap:9px;align-items:flex-start;padding:9px 10px;background:#F7F9FC;border-radius:10px">
              <span class="dot" style="margin-top:5px"></span>
              <div><div style="font-size:12px;font-weight:650">今日闸门拦截 2 条</div><div class="tiny faint" style="margin-top:2px">R7 相似度 0.83 · 已改写后通过</div></div>
            </div>
            <div class="divider" style="margin:2px 0"></div>
            <div class="row"><span class="tiny faint">上次自检 08:02 · 6/6 通过</span><span class="spacer"></span><span class="pill g">正常</span></div>
            <button class="btn ghost sm block" style="margin-top:auto" data-goto="s14">打开环境自检</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</section>
`);
