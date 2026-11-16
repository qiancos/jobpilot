document.getElementById('board').insertAdjacentHTML('beforeend', `
<section class="shot" data-id="s7" data-module="m4" data-title="批次确认" data-desc="全流程唯一的人工闸口：配额、去重、风险一次看清">
  <div class="cap"><i>07</i><b>批次确认</b><span>AI 不会替你点发送；这一步必须你按</span></div>
  <div class="screen">
    <div class="rail-slot" data-active="s8"></div>
    <div class="main">
      <div class="appbar">
        <div class="tt"><h2>确认第 6 批 · 标准档</h2><div class="sub">8 家 · 预计 7 分 30 秒 · 计划 21:00 开始 · 批次号 B-1116-06</div></div>
        <div class="right">
          <div class="status"><span class="dot"></span>BOSS 前台</div>
          <div class="status"><span class="dot"></span>无障碍 已开启</div>
          <div class="status">配额 6 / 15</div>
        </div>
      </div>
      <div class="content" style="display:grid;grid-template-columns:1fr 424px;gap:14px">
        <div class="card" style="display:flex;flex-direction:column;padding:0;overflow:hidden">
          <div style="padding:16px 18px 12px;display:flex;align-items:center;gap:10px;border-bottom:1px solid var(--line)">
            <b style="font-size:14px">待投 8 家</b>
            <span class="pill g">全部过闸</span>
            <span class="pill">去重 0 冲突</span>
            <div class="spacer"></div>
            <span class="tiny faint">指纹 = 话术末 8 个汉字</span>
          </div>
          <div class="tbl" style="flex:1">
            <div class="th" style="grid-template-columns:30px 1.5fr 1fr 56px 150px 80px">
              <span></span><span>公司 / 岗位</span><span>话术首句</span><span>闸门</span><span>指纹（末 8 字）</span><span>状态</span>
            </div>
            <div class="tr" style="grid-template-columns:30px 1.5fr 1fr 56px 150px 80px">
              <span class="cb on"></span>
              <div><div class="ttl">艾特数字 · java开发实习生</div><div class="sub2">广州·花都 · 3-4K · 匹配 78</div></div>
              <div class="tiny muted" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis">看到岗位要按需求做解决方案…</div>
              <span class="scoretag">88</span><span class="mono tiny">方便发您一份简历吗</span><span class="pill g">就绪</span>
            </div>
            <div class="tr" style="grid-template-columns:30px 1.5fr 1fr 56px 150px 80px">
              <span class="cb on"></span>
              <div><div class="ttl">青蓝智慧 · 全栈实习生</div><div class="sub2">广州·番禺 · 130-200元/天 · 匹配 86</div></div>
              <div class="tiny muted" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis">你们这个岗要求 Vue3 + Spring…</div>
              <span class="scoretag">86</span><span class="mono tiny">方便发您一份简历吗</span><span class="pill g">就绪</span>
            </div>
            <div class="tr" style="grid-template-columns:30px 1.5fr 1fr 56px 150px 80px">
              <span class="cb on"></span>
              <div><div class="ttl">广州矣润技术 · 开发实习工程师</div><div class="sub2">广州·天河 · 100-130元/天 · 匹配 84</div></div>
              <div class="tiny muted" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis">看到实习要求 Java/Spring/MySQL…</div>
              <span class="scoretag">85</span><span class="mono tiny">可以每周 3-5 天到岗</span><span class="pill g">就绪</span>
            </div>
            <div class="tr" style="grid-template-columns:30px 1.5fr 1fr 56px 150px 80px">
              <span class="cb on"></span>
              <div><div class="ttl">穹矩科技 · 后端开发实习生</div><div class="sub2">广州·番禺 · 6-7K · 匹配 83</div></div>
              <div class="tiny muted" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis">看到 27 届后端实习，我做过…</div>
              <span class="scoretag">84</span><span class="mono tiny">方便发您一份简历吗</span><span class="pill g">就绪</span>
            </div>
            <div class="tr" style="grid-template-columns:30px 1.5fr 1fr 56px 150px 80px">
              <span class="cb on"></span>
              <div><div class="ttl">库莱朋 · java实习生（任务制）</div><div class="sub2">广州·黄埔 · 150-200元/天 · 匹配 79</div></div>
              <div class="tiny muted" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis">看到岗位是任务制的 Java 实习…</div>
              <span class="scoretag">89</span><span class="mono tiny">先接一段试试</span><span class="pill g">就绪</span>
            </div>
            <div class="tr" style="grid-template-columns:30px 1.5fr 1fr 56px 150px 80px">
              <span class="cb on"></span>
              <div><div class="ttl">景游游戏 · java服务端（实习岗）</div><div class="sub2">广州·天河 · 4-5K · 匹配 77</div></div>
              <div class="tiny muted" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis">看到岗位做游戏后端接口开发…</div>
              <span class="scoretag">82</span><span class="mono tiny">方便发您一份简历吗</span><span class="pill g">就绪</span>
            </div>
            <div class="tr" style="grid-template-columns:30px 1.5fr 1fr 56px 150px 80px">
              <span class="cb on"></span>
              <div><div class="ttl">嘉为科技 · Java 后端研发（27校招）</div><div class="sub2">广州 · 100-150元/天 · 匹配 75</div></div>
              <div class="tiny muted" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis">看到 27 校招的 Java 后端岗…</div>
              <span class="scoretag">80</span><span class="mono tiny">方便发您一份简历吗</span><span class="pill g">就绪</span>
            </div>
            <div class="tr" style="grid-template-columns:30px 1.5fr 1fr 56px 150px 80px">
              <span class="cb on"></span>
              <div><div class="ttl">文云文化 · AI 应用开发实习生</div><div class="sub2">广州·天河 · 150-300元/天 · 匹配 73</div></div>
              <div class="tiny muted" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis">看到岗位做多 AI 协同的应用…</div>
              <span class="scoretag">83</span><span class="mono tiny">方便发您一份简历吗</span><span class="pill g">就绪</span>
            </div>
          </div>
          <div style="padding:12px 18px;border-top:1px solid var(--line);display:flex;align-items:center;gap:18px;background:#FBFCFE">
            <span class="tiny muted">已选 <b style="color:var(--ink)">8</b> / 8</span>
            <span class="tiny muted">平均闸门分 <b style="color:var(--ink)">84.6</b></span>
            <span class="tiny muted">预计耗时 <b style="color:var(--ink)">7 分 30 秒</b></span>
            <div class="spacer"></div>
            <span class="tiny faint">清单在确认后锁定，执行中不可改</span>
          </div>
        </div>

        <div style="display:flex;flex-direction:column;gap:14px;min-height:0">
          <div class="card">
            <div class="card-h"><h3>执行前检查</h3><span class="r"><span class="pill g">4 / 4 通过</span></span></div>
            <div style="display:flex;flex-direction:column;gap:9px">
              <div class="row" style="gap:9px"><span class="dot"></span><div style="flex:1;font-size:12.5px">BOSS 在前台（<span class="mono tiny">com.hpbr.bosszhipin</span>）</div><span class="tiny" style="color:var(--green)">通过</span></div>
              <div class="row" style="gap:9px"><span class="dot"></span><div style="flex:1;font-size:12.5px">无障碍服务已授权 · 命中率 96%</div><span class="tiny" style="color:var(--green)">通过</span></div>
              <div class="row" style="gap:9px"><span class="dot"></span><div style="flex:1;font-size:12.5px">8 家与近 30 天记录无重复</div><span class="tiny" style="color:var(--green)">通过</span></div>
              <div class="row" style="gap:9px"><span class="dot"></span><div style="flex:1;font-size:12.5px">8 条话术均无硬拦项</div><span class="tiny" style="color:var(--green)">通过</span></div>
            </div>
          </div>

          <div class="card" style="background:#FFFDF7;border-color:#F1E1BA">
            <div class="card-h"><h3>配额与熔断</h3><span class="r"><span class="pill a">注意余额</span></span></div>
            <div style="display:flex;align-items:center;gap:14px">
              <div class="ring sm amber" style="--v:93"><div class="in"><b>14</b><span>/ 15</span></div></div>
              <div style="flex:1">
                <div class="tiny muted">本批按 8 家计，执行后剩 <b>1</b> 家额度</div>
                <div class="tiny" style="color:#8A6A22;margin-top:4px;line-height:1.7">熔断条件：验证码/滑块 · 连续 3 次 wrong_target · 控件命中率 &lt; 50%</div>
              </div>
            </div>
            <div class="divider" style="margin:12px 0"></div>
            <div class="row" style="gap:8px"><span class="dot t"></span><div class="tiny" style="color:#475467;line-height:1.7">安全阀：每次点击后校验会话 header 公司名 ≥0.75，不通过整条放弃，绝不发送。</div></div>
          </div>

          <div class="card" style="flex:1;display:flex;flex-direction:column">
            <div class="card-h"><h3>确认</h3><span class="sub">人工二次确认</span></div>
            <div style="background:#F7F9FC;border-radius:12px;padding:12px 13px;font-size:12px;line-height:1.8;color:#475467">
              这批会在 BOSS App 里逐家点击「立即沟通」并发送专属话术。<br>
              执行期间请保持 BOSS 在前台；失败不会重试超过 2 次。
            </div>
            <div class="row" style="gap:9px;margin-top:14px;cursor:pointer" id="agree">
              <span class="cb" style="margin-top:1px"></span>
              <span style="font-size:12.5px">我已核对 8 条话术与公司名</span>
            </div>
            <div class="spacer"></div>
            <button class="btn primary block lg" id="goBtn" disabled data-goto="s8">确认投递 8 家</button>
            <div class="tiny faint" style="text-align:center;margin-top:8px">AI 不会替你点发送。点击后进入执行监控。</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</section>

<section class="shot" data-id="s8" data-module="m4" data-title="执行中 · 悬浮面板" data-desc="BOSS 前台时唯一可见的进度与刹车">
  <div class="cap"><i>08</i><b>执行中 · 悬浮面板</b><span>系统级 overlay，不抢焦点；BOSS 仍是前台</span></div>
  <div class="screen" style="display:block;background:#EEF0F3">
    <div style="position:absolute;inset:0;background:#fff">
      <div style="height:64px;background:#00BEBD;display:flex;align-items:center;padding:0 24px;gap:16px;color:#fff">
        <span style="font-size:20px;opacity:.9">‹</span>
        <b style="font-size:19px;letter-spacing:.02em">BOSS直聘</b>
        <div style="margin-left:auto;display:flex;gap:26px;font-size:13px;opacity:.9"><span>职位</span><span>公司</span><span>消息<b style="background:#fff;color:#00A9A8;border-radius:9px;padding:1px 6px;font-size:10px;margin-left:3px">2</b></span><span>我的</span></div>
      </div>
      <div style="padding:18px 26px">
        <div style="height:40px;background:#F3F5F7;border-radius:8px;display:flex;align-items:center;padding:0 14px;gap:8px;color:#93A0B0;font-size:13px">
          <span>Java 开发实习生</span><span style="margin-left:auto;font-size:12px;color:#00A9A8">搜索</span>
        </div>
        <div style="display:flex;gap:26px;margin-top:16px;font-size:14px;color:#5B6675">
          <span style="color:#0D1520;font-weight:700;position:relative">推荐<span style="position:absolute;left:50%;transform:translateX(-50%);bottom:-8px;width:18px;height:3px;background:#00BEBD;border-radius:2px"></span></span>
          <span>最新</span><span>附近</span><span style="margin-left:auto;font-size:12px;color:#00A9A8">筛选</span>
        </div>
        <div style="margin-top:22px;display:flex;flex-direction:column;gap:12px">
          <div style="border:1px solid #E6E9EF;border-radius:12px;padding:14px 16px;background:#fff">
            <div style="display:flex"><b style="font-size:15px">Java开发工程师</b><b style="margin-left:auto;font-size:15px;color:#FF6B00">12-18K</b></div>
            <div style="font-size:12px;color:#5B6675;margin-top:6px">广州·天河 · 3-5年 · 本科</div>
            <div style="display:flex;gap:6px;margin-top:10px"><span style="font-size:11px;background:#F2F4F8;border-radius:4px;padding:3px 7px;color:#5B6675">Spring</span><span style="font-size:11px;background:#F2F4F8;border-radius:4px;padding:3px 7px;color:#5B6675">MySQL</span></div>
          </div>
          <div style="border:1px solid #C9EFED;border-radius:12px;padding:14px 16px;background:#F7FFFE">
            <div style="display:flex"><b style="font-size:15px">开发实习工程师</b><b style="margin-left:auto;font-size:15px;color:#FF6B00">100-130元/天</b></div>
            <div style="font-size:12px;color:#5B6675;margin-top:6px">广州矣润技术 · 广州·天河 · 在校/应届</div>
            <div style="display:flex;gap:6px;margin-top:10px"><span style="font-size:11px;background:#E4F6F4;border-radius:4px;padding:3px 7px;color:#00716C">Java</span><span style="font-size:11px;background:#E4F6F4;border-radius:4px;padding:3px 7px;color:#00716C">Spring</span><span style="font-size:11px;background:#E4F6F4;border-radius:4px;padding:3px 7px;color:#00716C">可转正</span></div>
          </div>
          <div style="border:1px solid #E6E9EF;border-radius:12px;padding:14px 16px;background:#fff">
            <div style="display:flex"><b style="font-size:15px">后端开发实习生</b><b style="margin-left:auto;font-size:15px;color:#FF6B00">6-7K</b></div>
            <div style="font-size:12px;color:#5B6675;margin-top:6px">穹矩科技 · 广州·番禺 · 27 届</div>
          </div>
          <div style="border:1px solid #E6E9EF;border-radius:12px;padding:14px 16px;background:#fff">
            <div style="display:flex"><b style="font-size:15px">全栈开发工程师</b><b style="margin-left:auto;font-size:15px;color:#FF6B00">8-13K</b></div>
            <div style="font-size:12px;color:#5B6675;margin-top:6px">青蓝智慧 · 广州·番禺 · 1-3年 · 本科</div>
            <div style="display:flex;gap:6px;margin-top:10px"><span style="font-size:11px;background:#F2F4F8;border-radius:4px;padding:3px 7px;color:#5B6675">Vue3</span><span style="font-size:11px;background:#F2F4F8;border-radius:4px;padding:3px 7px;color:#5B6675">Spring</span></div>
          </div>
          <div style="border:1px solid #E6E9EF;border-radius:12px;padding:14px 16px;background:#fff">
            <div style="display:flex"><b style="font-size:15px">Java开发（跨境电商）</b><b style="margin-left:auto;font-size:15px;color:#FF6B00">10-15K</b></div>
            <div style="font-size:12px;color:#5B6675;margin-top:6px">库莱朋 · 广州·黄埔 · 1-3年</div>
          </div>
          <div style="border:1px solid #E6E9EF;border-radius:12px;padding:14px 16px;background:#fff">
            <div style="display:flex"><b style="font-size:15px">后端开发实习生</b><b style="margin-left:auto;font-size:15px;color:#FF6B00">120-150元/天</b></div>
            <div style="font-size:12px;color:#5B6675;margin-top:6px">星澜数据 · 广州·海珠 · 在校/应届 · 可转正</div>
          </div>
        </div>
      </div>
      <div style="position:absolute;left:0;right:0;bottom:0;height:56px;border-top:1px solid #E6E9EF;display:flex;align-items:center;justify-content:space-around;font-size:11px;color:#93A0B0;background:#fff">
        <span style="color:#00A9A8;font-weight:650">职位</span><span>公司</span><span>消息</span><span>我的</span>
      </div>
    </div>

    <div style="position:absolute;inset:0;background:linear-gradient(90deg,rgba(6,20,30,.06),rgba(6,20,30,.28))"></div>

    <div style="position:absolute;left:28px;top:22px;display:flex;align-items:center;gap:8px;background:rgba(11,21,34,.88);color:#fff;border-radius:20px;padding:7px 13px">
      <span class="dot" style="background:#22E2D8"></span><span style="font-size:11.5px">JobPilot 无障碍运行中</span>
    </div>

    <div style="position:absolute;right:34px;top:34px;width:432px;background:#0B1522;border-radius:20px;padding:20px 20px 18px;color:#fff;box-shadow:0 30px 70px rgba(0,0,0,.45);border:1px solid #1B2A3D">
      <div style="display:flex;align-items:center;gap:9px">
        <span class="dot" style="background:#22E2D8;box-shadow:0 0 0 4px rgba(34,226,216,.15)"></span>
        <b style="font-size:13.5px">JobPilot 执行中</b>
        <span style="font-size:10.5px;color:#7E93AB;background:#111F31;border:1px solid #22334A;border-radius:6px;padding:2px 6px">批次 B-1116-06</span>
        <div style="margin-left:auto;display:flex;gap:8px;color:#7E93AB;font-size:14px"><span>—</span><span>✕</span></div>
      </div>

      <div style="display:flex;align-items:flex-end;gap:8px;margin-top:16px">
        <div style="font-size:40px;font-weight:760;letter-spacing:-.04em;line-height:1">6</div>
        <div style="font-size:19px;color:#4A617C;font-weight:500;margin-bottom:3px">/ 8</div>
        <div style="margin-left:8px;font-size:11.5px;color:#8FA3BC;margin-bottom:5px">标准档 · 每家间隔 45–90s</div>
        <div style="margin-left:auto;font-size:11.5px;color:#22E2D8;margin-bottom:5px">预计 4 分 12 秒完成</div>
      </div>
      <div class="bar" style="height:5px;background:#18283A;margin-top:10px"><i style="width:75%;background:#22E2D8"></i></div>

      <div style="margin-top:16px;background:#111F31;border:1px solid #22334A;border-radius:13px;padding:12px 13px">
        <div style="display:flex;align-items:center;gap:8px">
          <span style="font-size:12.5px;font-weight:650">广州矣润技术</span>
          <span style="font-size:10.5px;color:#7E93AB">开发实习工程师</span>
          <span style="margin-left:auto;font-size:10.5px;background:rgba(34,226,216,.12);color:#22E2D8;border-radius:5px;padding:2px 6px">第 4 / 11 步</span>
        </div>
        <div style="font-size:11.5px;color:#A9BACD;margin-top:8px">已点击「立即沟通」，等待会话加载…</div>
        <div style="display:flex;align-items:center;gap:8px;margin-top:10px">
          <div style="flex:1;height:3px;background:#1B2A3D;border-radius:2px"><i style="display:block;width:40%;height:100%;background:#22E2D8;border-radius:2px"></i></div>
          <span class="mono" style="font-size:11px;color:#22E2D8">00:37</span>
        </div>
      </div>

      <div style="margin-top:14px;display:flex;flex-direction:column;gap:7px">
        <div style="display:flex;align-items:center;gap:8px;font-size:11.5px;color:#8FA3BC"><span style="color:#22E2D8">✓</span>定位卡片 · view_job_card</div>
        <div style="display:flex;align-items:center;gap:8px;font-size:11.5px;color:#8FA3BC"><span style="color:#22E2D8">✓</span>点击「立即沟通」</div>
        <div style="display:flex;align-items:center;gap:8px;font-size:11.5px;color:#8FA3BC"><span style="color:#22E2D8">✓</span>会话建立 · header 校验 0.92</div>
        <div style="display:flex;align-items:center;gap:8px;font-size:11.5px;color:#fff"><span style="width:9px;height:9px;border-radius:50%;border:2px solid #22E2D8;display:inline-block"></span>输入专属话术</div>
        <div style="display:flex;align-items:center;gap:8px;font-size:11.5px;color:#5B718C"><span style="width:9px;height:9px;border-radius:50%;border:2px solid #2A3D55;display:inline-block"></span>读末条消息 · 指纹核验</div>
      </div>

      <div style="margin-top:14px;border-top:1px solid #1B2A3D;padding-top:11px;font-family:var(--mono);font-size:10px;line-height:1.9;color:#63788F">
        <div><span style="color:#3F5670">21:04:12</span> click(boss_job_card_view) ok</div>
        <div><span style="color:#3F5670">21:04:14</span> chat header ≈ 广州矣润技术 0.92</div>
        <div><span style="color:#3F5670">21:04:15</span> wait 1.2s → input(text) 76 字</div>
      </div>

      <div style="display:flex;gap:8px;margin-top:14px">
        <button class="btn sm" style="flex:1;background:#1B2A3D;border-color:#2A3D55;color:#E6EDF5">暂停</button>
        <button class="btn sm" style="flex:1;background:transparent;border-color:#4A2A2A;color:#FF8A8A">终止批次</button>
      </div>
      <div style="display:flex;align-items:center;gap:12px;margin-top:11px;font-size:10.5px;color:#63788F">
        <span>L3 已核验 <b style="color:#22E2D8">6</b></span><span>失败 0</span><span>重复 0</span>
        <span style="margin-left:auto">熔断阈值 3 次</span>
      </div>
    </div>

    <div style="position:absolute;left:26px;bottom:26px;width:64px;height:64px;border-radius:50%;background:#0B1522;color:#fff;display:flex;flex-direction:column;align-items:center;justify-content:center;box-shadow:0 16px 40px rgba(0,0,0,.4)">
      <b style="font-size:15px">6/8</b><span style="font-size:9px;color:#7E93AB">折叠态</span>
    </div>
    <div style="position:absolute;left:104px;bottom:44px;font-size:10.5px;color:#7E93AB;background:rgba(11,21,34,.8);padding:5px 9px;border-radius:6px">可拖到屏幕边缘吸附；熔断时变红</div>
  </div>
</section>

<section class="shot" data-id="s9" data-module="m4" data-title="核验报告" data-desc="L1/L2/L3 三态；只有 L3 才算真送达">
  <div class="cap"><i>09</i><b>核验报告</b><span>8 家逐条给证据，失败给原因与下一步</span></div>
  <div class="screen">
    <div class="rail-slot" data-active="s8"></div>
    <div class="main">
      <div class="appbar">
        <div class="tt"><h2>第 6 批核验报告</h2><div class="sub">21:07 完成 · 8 家 · 平均 51 秒/家 · 批次 B-1116-06</div></div>
        <div class="right">
          <div class="status">截图 8 张 · 已存档</div>
          <button class="btn ghost sm">导出 xlsx</button>
          <button class="btn primary sm" data-goto="s11">去会话管家</button>
        </div>
      </div>
      <div class="content">
        <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:14px;height:92px;flex:none">
          <div class="kpi"><div class="lab">L3 送达 <span class="pill g sm">只认这个</span></div><div class="val">6<small>/ 8</small></div><div class="delta">75% · 目标 ≥95%</div></div>
          <div class="kpi"><div class="lab">L2 会话绑定</div><div class="val">7<small>家</small></div><div class="delta">header 公司名匹配 ≥0.75</div></div>
          <div class="kpi"><div class="lab">失败 / 跳过</div><div class="val" style="color:var(--red)">1<small>家</small></div><div class="delta">session_not_found · 已跳过</div></div>
          <div class="kpi"><div class="lab">重复投递</div><div class="val" style="color:var(--green)">0</div><div class="delta">job/company/session 三层去重</div></div>
        </div>

        <div style="display:grid;grid-template-columns:1fr 392px;gap:14px;flex:1;min-height:0">
          <div class="card pad0" style="display:flex;flex-direction:column;overflow:hidden">
            <div style="padding:14px 18px;border-bottom:1px solid var(--line);display:flex;align-items:center;gap:10px">
              <b style="font-size:13.5px">逐条结果</b>
              <span class="pill">按执行顺序</span>
              <div class="spacer"></div>
              <span class="tiny faint">指纹 = 末 8 个汉字</span>
            </div>
            <div class="tbl" style="flex:1">
              <div class="th" style="grid-template-columns:1.4fr 64px 64px 64px 1fr 84px">
                <span>公司 / 岗位</span><span>L1 点击</span><span>L2 绑定</span><span>L3 送达</span><span>证据</span><span>结果</span>
              </div>
              <div class="tr" style="grid-template-columns:1.4fr 64px 64px 64px 1fr 84px;height:54px">
                <div><div class="ttl">艾特数字</div><div class="sub2">java开发实习生</div></div>
                <span class="pill g">✓</span><span class="pill g">0.94</span><span class="pill g">✓</span>
                <span class="mono tiny">方便发您一份简历吗</span><span class="pill g">delivered</span>
              </div>
              <div class="tr" style="grid-template-columns:1.4fr 64px 64px 64px 1fr 84px;height:54px">
                <div><div class="ttl">青蓝智慧</div><div class="sub2">全栈实习生</div></div>
                <span class="pill g">✓</span><span class="pill g">0.91</span><span class="pill g">✓</span>
                <span class="mono tiny">方便发您一份简历吗</span><span class="pill g">delivered</span>
              </div>
              <div class="tr" style="grid-template-columns:1.4fr 64px 64px 64px 1fr 84px;height:54px">
                <div><div class="ttl">广州矣润技术</div><div class="sub2">开发实习工程师</div></div>
                <span class="pill g">✓</span><span class="pill g">0.92</span><span class="pill g">✓</span>
                <span class="mono tiny">可以每周 3-5 天到岗</span><span class="pill g">delivered</span>
              </div>
              <div class="tr" style="grid-template-columns:1.4fr 64px 64px 64px 1fr 84px;height:54px">
                <div><div class="ttl">穹矩科技</div><div class="sub2">后端开发实习生</div></div>
                <span class="pill g">✓</span><span class="pill g">0.89</span><span class="pill g">✓</span>
                <span class="mono tiny">方便发您一份简历吗</span><span class="pill g">delivered</span>
              </div>
              <div class="tr" style="grid-template-columns:1.4fr 64px 64px 64px 1fr 84px;height:54px">
                <div><div class="ttl">库莱朋</div><div class="sub2">java实习生（任务制）</div></div>
                <span class="pill g">✓</span><span class="pill g">0.86</span><span class="pill g">✓</span>
                <span class="mono tiny">先接一段试试</span><span class="pill g">delivered</span>
              </div>
              <div class="tr" style="grid-template-columns:1.4fr 64px 64px 64px 1fr 84px;height:54px">
                <div><div class="ttl">景游游戏</div><div class="sub2">java服务端（实习岗）</div></div>
                <span class="pill g">✓</span><span class="pill g">0.90</span><span class="pill g">✓</span>
                <span class="mono tiny">方便发您一份简历吗</span><span class="pill g">delivered</span>
              </div>
              <div class="tr" style="grid-template-columns:1.4fr 64px 64px 64px 1fr 84px;height:54px">
                <div><div class="ttl">嘉为科技</div><div class="sub2">Java 后端研发（27校招）</div></div>
                <span class="pill g">✓</span><span class="pill r">0.61</span><span class="pill">—</span>
                <span class="tiny" style="color:var(--red)">会话未定位到</span><span class="pill r">failed</span>
              </div>
              <div class="tr" style="grid-template-columns:1.4fr 64px 64px 64px 1fr 84px;height:54px;opacity:.6">
                <div><div class="ttl">文云文化</div><div class="sub2">AI 应用开发实习生</div></div>
                <span class="pill">skip</span><span class="pill">—</span><span class="pill">—</span>
                <span class="tiny">5 分钟内已有专属语</span><span class="pill a">already_sent</span>
              </div>
            </div>
          </div>

          <div style="display:flex;flex-direction:column;gap:14px;min-height:0">
            <div class="card">
              <div class="card-h"><h3>三态漏斗</h3><span class="sub">8 → 7 → 6</span></div>
              <div style="display:flex;align-items:center;gap:8px">
                <div style="flex:1;height:56px;background:var(--blue-soft);border-radius:10px;display:flex;flex-direction:column;align-items:center;justify-content:center"><b style="font-size:18px;color:var(--blue)">8</b><span class="tiny" style="color:var(--blue)">L1 clicked</span></div>
                <div style="flex:1;height:56px;background:var(--amber-soft);border-radius:10px;display:flex;flex-direction:column;align-items:center;justify-content:center"><b style="font-size:18px;color:var(--amber)">7</b><span class="tiny" style="color:var(--amber)">L2 bound</span></div>
                <div style="flex:1;height:56px;background:var(--green-soft);border-radius:10px;display:flex;flex-direction:column;align-items:center;justify-content:center"><b style="font-size:18px;color:var(--green)">6</b><span class="tiny" style="color:var(--green)">L3 delivered</span></div>
              </div>
              <div class="tiny faint" style="margin-top:9px;line-height:1.7">只有 L3 计入「送达」。L1 只证明点到了，L2 只证明会话对了。</div>
            </div>

            <div class="card" style="flex:1;display:flex;flex-direction:column;border-color:#F3C9C9">
              <div class="card-h"><h3>失败详情</h3><span class="r"><span class="pill r">1 家</span></span></div>
              <div style="font-size:13px;font-weight:650">嘉为科技 · Java 后端研发（27校招）</div>
              <div class="tiny" style="color:var(--red);margin-top:4px">error_code: session_not_found</div>
              <div style="background:#FFF8F8;border-radius:10px;padding:10px 12px;margin-top:10px;font-size:11.5px;line-height:1.8;color:#7A3B3B">
                点击「立即沟通」成功，但会话列表未出现该公司。已按规则跳过，未发送任何内容，不会发错人。
              </div>
              <div style="display:flex;flex-direction:column;gap:7px;margin-top:12px;font-size:11.5px;color:#475467">
                <div class="row"><span class="dot a"></span>建议：手动在 BOSS 搜索该公司，确认是否限流</div>
                <div class="row"><span class="dot a"></span>重试上限 2 次，超过转人工</div>
              </div>
              <div class="spacer"></div>
              <div class="row" style="gap:8px;margin-top:14px"><button class="btn sm ghost" style="flex:1">查看截图</button><button class="btn sm soft" style="flex:1">重试这 1 家</button></div>
            </div>

            <div class="card" style="background:#F7F9FC">
              <div class="row"><span class="tiny faint">本批洞察</span><span class="pill g" style="margin-left:auto">闸门全过</span></div>
              <div style="font-size:12px;line-height:1.75;margin-top:6px;color:#344054">6 条 L3 平均视觉宽度 86，R7 平均相似度 0.47。比 10-21 批（宽度 137、过闸 0/20）更接近可复用的模板。</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</section>
`);
