document.getElementById('board').insertAdjacentHTML('beforeend', `
<section class="shot" data-id="s14" data-module="m7" data-title="设置与自检" data-desc="档位、无障碍、版本、浮层三态；参数即时生效">
  <div class="cap"><i>14</i><b>设置与自检</b><span>vivo 每次更新会重置无障碍——自检要显眼</span></div>
  <div class="screen">
    <div class="rail-slot" data-active="s14"></div>
    <div class="main">
      <div class="appbar">
        <div class="tt"><h2>设置与自检</h2><div class="sub">vivo PA2573 · Android 16 · JobPilot v0.4.0 · 2064×3096 @400dpi</div></div>
        <div class="right">
          <div class="status"><span class="dot"></span>全部正常</div>
          <button class="btn ghost sm">导出诊断包</button>
        </div>
      </div>
      <div class="content" style="display:grid;grid-template-columns:1fr 1fr 392px;gap:14px">
        <div style="display:flex;flex-direction:column;gap:14px;min-height:0">
          <div class="card">
            <div class="card-h"><h3>执行档位</h3><span class="sub">改动立即写入 PanelState</span></div>
            <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:8px">
              <div style="border:1px solid var(--line);border-radius:11px;padding:10px;text-align:center">
                <div style="font-size:12px;font-weight:650">保守</div><div style="font-size:20px;font-weight:740;margin-top:4px">8</div><div class="tiny faint">条 / 日</div>
              </div>
              <div style="border:1.5px solid var(--brand);background:var(--brand-soft);border-radius:11px;padding:10px;text-align:center">
                <div style="font-size:12px;font-weight:650;color:var(--brand-ink)">标准</div><div style="font-size:20px;font-weight:740;margin-top:4px;color:var(--brand-ink)">15</div><div class="tiny" style="color:var(--brand-ink)">条 / 日 · 默认</div>
              </div>
              <div style="border:1px solid var(--line);border-radius:11px;padding:10px;text-align:center">
                <div style="font-size:12px;font-weight:650">激进</div><div style="font-size:20px;font-weight:740;margin-top:4px">30</div><div class="tiny faint">条 / 日</div>
              </div>
            </div>
            <div style="margin-top:16px">
              <div class="row"><span class="tiny muted">每日上限</span><span class="spacer"></span><b style="font-size:13px">15 条</b></div>
              <div class="bar" style="margin-top:7px"><i style="width:40%"></i></div>
              <div class="row tiny faint" style="margin-top:5px"><span>5</span><span class="spacer"></span><span>40</span></div>
            </div>
            <div class="divider" style="margin:14px 0"></div>
            <div class="row"><span class="tiny muted">条间隔</span><span class="spacer"></span><b style="font-size:12.5px">45 – 90 秒</b></div>
            <div class="row" style="margin-top:9px"><span class="tiny muted">单批上限</span><span class="spacer"></span><b style="font-size:12.5px">8 家</b></div>
            <div class="row" style="margin-top:9px"><span class="tiny muted">夜间静默</span><span class="spacer"></span><b style="font-size:12.5px">23:00 – 08:00</b></div>
            <div class="divider" style="margin:14px 0"></div>
            <div class="row"><span class="tiny muted">今日已用</span><span class="spacer"></span><b style="font-size:12.5px">6 / 15</b></div>
            <div class="bar" style="margin-top:7px"><i class="g" style="width:40%"></i></div>
            <div class="tiny faint" style="margin-top:8px">超限直接拒绝新批次；不静默截断已有批次。</div>
          </div>

          <div class="card" style="flex:1;display:flex;flex-direction:column">
            <div class="card-h"><h3>数据与隐私</h3><span class="r pill g">全本地</span></div>
            <div style="display:flex;flex-direction:column;gap:9px">
              <div class="row" style="gap:9px"><span class="dot t"></span><div style="flex:1;font-size:12px">台账 SQLite 本地存储</div><span class="tiny faint">2.8 MB</span></div>
              <div class="row" style="gap:9px"><span class="dot t"></span><div style="flex:1;font-size:12px">简历与附件不出设备</div><span class="tiny faint">0 上传</span></div>
              <div class="row" style="gap:9px"><span class="dot t"></span><div style="flex:1;font-size:12px">LLM 只传脱敏 JD + 技能摘要</div><span class="tiny faint">已脱敏</span></div>
              <div class="row" style="gap:9px"><span class="dot t"></span><div style="flex:1;font-size:12px">LLM Key</div><span class="mono tiny faint">sk-****-7f2a</span></div>
            </div>
            <div class="divider" style="margin:13px 0"></div>
            <div class="row" style="gap:8px;margin-top:auto"><button class="btn sm ghost" style="flex:1">导出 xlsx</button><button class="btn sm ghost" style="flex:1;color:var(--red);border-color:#F3C9C9">清空台账</button></div>
          </div>
        </div>

        <div style="display:grid;grid-template-rows:1fr auto;gap:14px;min-height:0">
          <div class="card" style="display:flex;flex-direction:column">
            <div class="card-h"><h3>环境自检</h3><span class="sub">启动时 + 每 30 分钟</span><span class="r"><span class="pill g">6 / 6 通过</span></span></div>
            <div style="display:flex;flex-direction:column;gap:0">
              <div class="row" style="padding:9px 0;border-bottom:1px solid var(--line2)"><span class="dot"></span><div style="flex:1;font-size:12.5px;margin-left:8px">无障碍服务</div><span class="tiny" style="color:var(--green)">已开启</span></div>
              <div style="padding:8px 0 10px;border-bottom:1px solid var(--line2)">
                <div class="tiny" style="background:var(--amber-soft);border-radius:8px;padding:8px 10px;color:#8A6A22;line-height:1.7">vivo 每次更新 App 会重置授权。若变灰，请到「设置 → 无障碍 → 已下载的服务 → JobPilot」重新开启。</div>
              </div>
              <div class="row" style="padding:9px 0;border-bottom:1px solid var(--line2)"><span class="dot"></span><div style="flex:1;font-size:12.5px;margin-left:8px">BOSS 前台</div><span class="tiny" style="color:var(--green)">com.hpbr.bosszhipin</span></div>
              <div class="row" style="padding:9px 0;border-bottom:1px solid var(--line2)"><span class="dot"></span><div style="flex:1;font-size:12.5px;margin-left:8px">控件命中率</div><span class="tiny" style="color:var(--green)">96% · 阈值 50%</span></div>
              <div class="row" style="padding:9px 0;border-bottom:1px solid var(--line2)"><span class="dot"></span><div style="flex:1;font-size:12.5px;margin-left:8px">屏幕与方向</div><span class="tiny" style="color:var(--green)">3096×2064 横屏</span></div>
              <div class="row" style="padding:9px 0"><span class="dot"></span><div style="flex:1;font-size:12.5px;margin-left:8px">应用保活</div><span class="tiny" style="color:var(--green)">已加入电池白名单</span></div>
            </div>
            <div class="divider" style="margin:12px 0"></div>
            <div class="row" style="gap:8px"><button class="btn sm ghost" style="flex:1">重新自检</button><button class="btn sm soft" style="flex:1">去系统设置</button></div>
          </div>

          <div class="card">
            <div class="card-h"><h3>版本与选择器</h3><span class="sub">改版只改一处</span></div>
            <div style="display:flex;flex-direction:column;gap:7px;font-size:12px">
              <div class="row"><span class="faint" style="width:104px">App</span><b class="mono">v0.4.0</b><span class="spacer"></span><span class="tiny faint">2026-11-15</span></div>
              <div class="row"><span class="faint" style="width:104px">无障碍控件 id</span><b class="mono">a11y_ids v3</b><span class="spacer"></span><span class="tiny faint">view_job_card</span></div>
              <div class="row"><span class="faint" style="width:104px">网页选择器</span><b class="mono">selectors.json v3</b><span class="spacer"></span><span class="tiny faint">降级用</span></div>
              <div class="row"><span class="faint" style="width:104px">事实库</span><b class="mono">lexicon v1.4</b><span class="spacer"></span><span class="tiny faint">FACTS 12 条</span></div>
              <div class="row"><span class="faint" style="width:104px">闸门</span><b class="mono">gate v1.4 · 9 条</b><span class="spacer"></span><span class="pill g">已移植</span></div>
            </div>
            <div class="divider" style="margin:12px 0"></div>
            <div class="row"><span class="tiny faint">熔断记录</span><span class="spacer"></span><b style="font-size:12.5px;color:var(--green)">本周 0 次</b></div>
          </div>
        </div>

        <div class="card" style="display:flex;flex-direction:column;padding:16px 18px">
          <div class="card-h"><h3>悬浮面板</h3><span class="sub">三种形态</span></div>

          <div style="border:1px solid var(--line);border-radius:14px;padding:12px;background:#F7F9FC">
            <div class="row"><span class="tiny faint">展开态</span><span class="pill t" style="margin-left:auto">执行中</span></div>
            <div style="margin-top:9px;background:#0B1522;border-radius:12px;padding:12px;color:#fff">
              <div class="row"><span class="dot" style="background:#22E2D8"></span><span style="font-size:11px;font-weight:650">JobPilot 执行中</span><span style="margin-left:auto;font-size:10px;color:#7E93AB">— ✕</span></div>
              <div class="row" style="margin-top:8px;align-items:flex-end"><b style="font-size:24px">6</b><span style="font-size:11px;color:#5B718C">/ 8</span><span style="margin-left:8px;font-size:9.5px;color:#8FA3BC">广州矣润技术 · 第 4/11 步</span></div>
              <div class="bar" style="height:4px;background:#18283A;margin-top:7px"><i style="width:75%;background:#22E2D8"></i></div>
              <div class="row" style="margin-top:8px;gap:6px"><span style="flex:1;text-align:center;font-size:9px;background:#1B2A3D;border-radius:5px;padding:4px;color:#C9D6E6">暂停</span><span style="flex:1;text-align:center;font-size:9px;border:1px solid #4A2A2A;border-radius:5px;padding:4px;color:#FF8A8A">终止</span></div>
            </div>
          </div>

          <div style="border:1px solid var(--line);border-radius:14px;padding:12px;background:#F7F9FC;margin-top:12px">
            <div class="row"><span class="tiny faint">折叠态</span><span class="pill" style="margin-left:auto">拖到边缘吸附</span></div>
            <div class="row" style="margin-top:10px;gap:12px">
              <div style="width:56px;height:56px;border-radius:50%;background:#0B1522;color:#fff;display:flex;flex-direction:column;align-items:center;justify-content:center"><b style="font-size:13px">6/8</b><span style="font-size:8px;color:#7E93AB">6 送达</span></div>
              <div class="tiny faint" style="line-height:1.7">点击展开；长按拖动；执行中显示进度，空闲时显示「待命」。</div>
            </div>
          </div>

          <div style="border:1px solid #F3C9C9;border-radius:14px;padding:12px;background:#FFF9F9;margin-top:12px">
            <div class="row"><span class="tiny faint">熔断态</span><span class="pill r" style="margin-left:auto">已停止</span></div>
            <div style="margin-top:9px;background:#2A1114;border:1px solid #5A2A2A;border-radius:10px;padding:10px 11px;color:#FFD5D5">
              <div style="font-size:10.5px;font-weight:650">检测到验证码，已停止</div>
              <div style="font-size:9px;color:#D79A9A;margin-top:4px;line-height:1.6">已截图 · 冷却 24h · 今天不再投递</div>
            </div>
            <div class="tiny faint" style="margin-top:8px;line-height:1.7">异常即停，宁可当天不投；熔断记录进台账。</div>
          </div>

          <div class="spacer"></div>
          <div style="background:#F7F9FC;border-radius:10px;padding:10px 12px;margin-top:12px">
            <div class="row"><span class="tiny" style="font-weight:650">自动弹出</span><span class="spacer"></span><span class="pill g">开</span></div>
            <div class="row" style="margin-top:7px"><span class="tiny" style="font-weight:650">默认折叠</span><span class="spacer"></span><span class="pill">关</span></div>
            <div class="row" style="margin-top:7px"><span class="tiny" style="font-weight:650">浮层透明度</span><span class="spacer"></span><span class="tiny faint">92%</span></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</section>
`);
