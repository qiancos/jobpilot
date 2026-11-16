document.getElementById('board').insertAdjacentHTML('beforeend', `
<section class="shot" data-id="s5" data-module="m3" data-title="话术工作台" data-desc="双版本起稿，9 条闸门实时校验，逐条可改">
  <div class="cap"><i>05</i><b>话术工作台</b><span>AI 起稿，人改；闸门不是生成器，是质检</span></div>
  <div class="screen">
    <div class="rail-slot" data-active="s5"></div>
    <div class="main">
      <div class="appbar">
        <div class="tt"><h2>话术工作台</h2><div class="sub">8 家待生成 · 第 3 / 8 · 已过闸 5 家 · 事实库 lexicon v1.4</div></div>
        <div class="right">
          <div class="status"><span class="dot"></span>过闸率 100%</div>
          <button class="btn ghost sm">批量重新生成</button>
          <button class="btn primary sm" data-goto="s7">全部过闸 → 去确认</button>
        </div>
      </div>
      <div class="content" style="display:grid;grid-template-columns:220px 1fr 360px;gap:14px">
        <div class="card pad0" style="display:flex;flex-direction:column;overflow:hidden">
          <div style="padding:14px 14px 10px;border-bottom:1px solid var(--line)">
            <div class="row"><b style="font-size:13px">本批 8 家</b><span class="pill g" style="margin-left:auto">5 已过闸</span></div>
            <div class="tiny faint" style="margin-top:4px">按匹配分排序</div>
          </div>
          <div style="flex:1;display:flex;flex-direction:column">
            <div style="display:flex;gap:9px;padding:11px 12px;background:var(--brand-soft);border-left:3px solid var(--brand)">
              <span class="mono tiny faint" style="width:14px">1</span>
              <div style="flex:1;min-width:0"><div style="font-size:12.5px;font-weight:650;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">艾特数字</div><div class="tiny faint" style="margin-top:2px">java开发实习生</div></div>
              <span class="dot" style="margin-top:4px"></span>
            </div>
            <div style="display:flex;gap:9px;padding:11px 12px;border-bottom:1px solid var(--line2)">
              <span class="mono tiny faint" style="width:14px">2</span>
              <div style="flex:1;min-width:0"><div style="font-size:12.5px;font-weight:600;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">青蓝智慧</div><div class="tiny faint" style="margin-top:2px">全栈实习生</div></div>
              <span class="dot" style="margin-top:4px"></span>
            </div>
            <div style="display:flex;gap:9px;padding:11px 12px;border-bottom:1px solid var(--line2)">
              <span class="mono tiny faint" style="width:14px">3</span>
              <div style="flex:1;min-width:0"><div style="font-size:12.5px;font-weight:600;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">广州矣润技术</div><div class="tiny faint" style="margin-top:2px">开发实习工程师</div></div>
              <span class="dot" style="margin-top:4px"></span>
            </div>
            <div style="display:flex;gap:9px;padding:11px 12px;border-bottom:1px solid var(--line2)">
              <span class="mono tiny faint" style="width:14px">4</span>
              <div style="flex:1;min-width:0"><div style="font-size:12.5px;font-weight:600;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">穹矩科技</div><div class="tiny faint" style="margin-top:2px">后端开发实习生</div></div>
              <span class="dot" style="margin-top:4px"></span>
            </div>
            <div style="display:flex;gap:9px;padding:11px 12px;border-bottom:1px solid var(--line2)">
              <span class="mono tiny faint" style="width:14px">5</span>
              <div style="flex:1;min-width:0"><div style="font-size:12.5px;font-weight:600;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">库莱朋</div><div class="tiny faint" style="margin-top:2px">java实习生</div></div>
              <span class="dot a" style="margin-top:4px"></span>
            </div>
            <div style="display:flex;gap:9px;padding:11px 12px;border-bottom:1px solid var(--line2)">
              <span class="mono tiny faint" style="width:14px">6</span>
              <div style="flex:1;min-width:0"><div style="font-size:12.5px;font-weight:600;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">景游游戏</div><div class="tiny faint" style="margin-top:2px">java服务端</div></div>
              <span class="dot a" style="margin-top:4px"></span>
            </div>
            <div style="display:flex;gap:9px;padding:11px 12px;border-bottom:1px solid var(--line2)">
              <span class="mono tiny faint" style="width:14px">7</span>
              <div style="flex:1;min-width:0"><div style="font-size:12.5px;font-weight:600;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">嘉为科技</div><div class="tiny faint" style="margin-top:2px">Java 校招</div></div>
              <span class="dot" style="margin-top:4px"></span>
            </div>
            <div style="display:flex;gap:9px;padding:11px 12px">
              <span class="mono tiny faint" style="width:14px">8</span>
              <div style="flex:1;min-width:0"><div style="font-size:12.5px;font-weight:600;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">文云文化</div><div class="tiny faint" style="margin-top:2px">AI 应用开发</div></div>
              <span class="dot gray" style="margin-top:4px"></span>
            </div>
          </div>
          <div style="padding:11px 12px;border-top:1px solid var(--line);display:flex;align-items:center;gap:8px">
            <span class="dot"></span><span class="tiny faint">已过闸</span><span class="dot a"></span><span class="tiny faint">待改</span><span class="dot gray"></span><span class="tiny faint">未生成</span>
          </div>
        </div>

        <div class="card tabbox" style="display:flex;flex-direction:column;padding:18px 22px">
          <div class="row">
            <div>
              <div class="row"><span style="font-size:16px;font-weight:700">艾特数字 · java开发实习生</span><span class="pill">3-4K</span><span class="pill g">刚刚活跃</span></div>
              <div class="tiny faint" style="margin-top:4px">广州·花都·花都广场 · 20-99 人 · 匹配 78</div>
            </div>
            <div class="spacer"></div>
            <div class="tiny faint">第 1 / 8</div>
          </div>

          <div style="display:flex;gap:6px;margin-top:14px;background:#F4F6FA;padding:4px;border-radius:10px;width:fit-content">
            <button data-tab="A" class="btn sm" style="background:#fff;box-shadow:0 1px 3px rgba(16,24,40,.1);border:0">版本 A · 技术证据型</button>
            <button data-tab="B" class="btn sm" style="background:transparent;border:0;color:var(--sub)">版本 B · 业务理解型</button>
          </div>

          <div style="margin-top:14px;background:#F7F9FC;border-radius:12px;padding:12px 14px">
            <div class="row"><span class="kicker">引用了 JD 这一句</span><span class="pill t" style="margin-left:auto">R3 通过</span></div>
            <div style="font-size:13px;line-height:1.75;margin-top:6px;color:#344054">「根据需求做<span class="kw hit">解决方案和技术实现</span>，能独立推进小模块交付。」</div>
          </div>

          <div data-variant="A" style="margin-top:14px;border:1px solid var(--line);border-radius:14px;padding:16px 18px;flex:1;display:flex;flex-direction:column">
            <div style="font-size:14.5px;line-height:2;letter-spacing:.01em;color:#1D2939">
              看到岗位要按需求做<span class="kw hit">解决方案和技术实现</span>，我实习时独立推完一个摄影预约小程序：uni-app + SpringBoot + 微信支付 + OSS，从需求到上线 3 周，支付回调幂等和档期冲突都自己处理。27 届，可每周 5 天到岗，方便发您一份简历吗？
            </div>
            <div style="margin-top:16px;padding-top:12px;border-top:1px solid var(--line2);display:flex;flex-direction:column;gap:8px">
              <div class="row" style="align-items:flex-start"><span class="kicker" style="width:58px;flex:none">AI 依据</span><span class="tiny muted" style="line-height:1.7">引用 JD「解决方案和技术实现」；事实库「摄影预约小程序全流程」</span></div>
              <div class="row" style="align-items:flex-start"><span class="kicker" style="width:58px;flex:none">避开</span><span class="tiny muted" style="line-height:1.7">Kafka / 微服务治理（jd_miss，一个字都不出现）</span></div>
              <div class="row" style="align-items:flex-start"><span class="kicker" style="width:58px;flex:none">版本差异</span><span class="tiny muted" style="line-height:1.7">A 偏交付细节；B 偏对方业务与提问，过闸分略低</span></div>
            </div>
            <div style="display:flex;gap:12px;margin-top:16px;align-items:flex-start">
              <span class="kicker" style="width:58px;flex:none;padding-top:6px">手机预览</span>
              <div style="width:306px;background:#EDF1F6;border-radius:12px;padding:10px 11px">
                <div style="background:#95EC69;border-radius:9px;padding:9px 11px;font-size:11px;line-height:1.95;color:#12301A">
                  看到岗位要按需求做解决方案和技术实现，我实习时独立推完一个摄影预约小程序：uni-app + SpringBoot + 微信支付 + OSS，从需求到上线 3 周，支付回调幂等和档期冲突都自己处理。27 届，可每周 5 天到岗，方便发您一份简历吗？
                </div>
                <div class="tiny faint" style="margin-top:6px">BOSS 聊天气泡 · 视觉宽度 91 · 3 行内读完</div>
              </div>
              <div class="tiny faint" style="flex:1;line-height:1.7;padding-top:4px">预览只为检查「手机上会不会变成一坨」，不改变实际发送内容。</div>
            </div>
            <div class="row" style="margin-top:auto;padding-top:12px">
              <span class="pill">视觉宽度 <b style="margin-left:2px">91</b></span>
              <span class="pill">3 句</span>
              <span class="pill g">质量分 88</span>
              <span class="spacer"></span>
              <span class="tiny faint">末 8 字指纹：方便发您一份简历吗</span>
            </div>
          </div>

          <div data-variant="B" style="display:none;margin-top:14px;border:1px solid var(--line);border-radius:14px;padding:16px 18px;flex:1;display:none;flex-direction:column">
            <div style="font-size:14.5px;line-height:2;letter-spacing:.01em;color:#1D2939">
              你们在做移动互联网的小程序业务，我注意到这个岗要能独立推进交付。我做过一个从 0 到 1 的摄影预约小程序，负责档期冲突、支付回调和 OSS 直传，3 周上线，后来按周迭代过 2 版。可每周 5 天到岗，想了解你们现在最缺哪块？
            </div>
            <div class="row" style="margin-top:auto;padding-top:12px">
              <span class="pill">视觉宽度 <b style="margin-left:2px">89</b></span>
              <span class="pill">3 句</span>
              <span class="pill a">R7 提醒 0.78</span>
              <span class="spacer"></span>
              <span class="tiny faint">与 10-23 批 #7 相似</span>
            </div>
          </div>

          <div class="row" style="margin-top:14px;gap:8px">
            <button class="btn ghost sm">重新生成 A/B</button>
            <button class="btn ghost sm" data-goto="s6">查看闸门详情</button>
            <div class="spacer"></div>
            <button class="btn ghost">丢弃</button>
            <button class="btn primary">收进队列 →</button>
          </div>
        </div>

        <div style="display:flex;flex-direction:column;gap:14px;min-height:0">
          <div class="card" style="flex:1;display:flex;flex-direction:column">
            <div class="card-h"><h3>9 条闸门</h3><span class="sub">硬规则守底线</span><span class="r"><span class="pill g">全部通过</span></span></div>
            <div class="row" style="gap:14px;align-items:center;margin-bottom:12px">
              <div class="ring sm green" style="--v:88"><div class="in"><b>88</b><span>A</span></div></div>
              <div>
                <div style="font-size:13px;font-weight:650">可以原样发</div>
                <div class="tiny faint" style="margin-top:3px">硬拦 0 · 软扣 0 · 提醒 0</div>
                <div class="tiny faint" style="margin-top:2px">阈值：无硬拦且 ≥65</div>
              </div>
            </div>
            <div style="display:flex;flex-direction:column;gap:0;flex:1">
              <div style="display:flex;align-items:center;gap:8px;padding:7px 0;border-bottom:1px solid var(--line2)"><span class="dot"></span><div style="flex:1"><div style="font-size:12px;font-weight:600">R1 视觉宽度 55–95</div><div class="tiny faint">91 · 3 句</div></div><span class="pill g">通过</span></div>
              <div style="display:flex;align-items:center;gap:8px;padding:7px 0;border-bottom:1px solid var(--line2)"><span class="dot"></span><div style="flex:1"><div style="font-size:12px;font-weight:600">R2 非模板开场</div><div class="tiny faint">以「看到岗位…」开头</div></div><span class="pill g">通过</span></div>
              <div style="display:flex;align-items:center;gap:8px;padding:7px 0;border-bottom:1px solid var(--line2)"><span class="dot"></span><div style="flex:1"><div style="font-size:12px;font-weight:600">R3 引用 JD 具体词</div><div class="tiny faint">「解决方案和技术实现」</div></div><span class="pill g">通过</span></div>
              <div style="display:flex;align-items:center;gap:8px;padding:7px 0;border-bottom:1px solid var(--line2)"><span class="dot"></span><div style="flex:1"><div style="font-size:12px;font-weight:600">R7 近 7 天相似度</div><div class="tiny faint">0.41 · 低于 0.72</div></div><span class="pill g">通过</span></div>
              <div style="display:flex;align-items:center;gap:8px;padding:7px 0;border-bottom:1px solid var(--line2)"><span class="dot"></span><div style="flex:1"><div style="font-size:12px;font-weight:600">R8 事实库校验</div><div class="tiny faint">uni-app / SpringBoot / 27 届</div></div><span class="pill g">通过</span></div>
              <div style="display:flex;align-items:center;gap:8px;padding:7px 0;border-bottom:1px solid var(--line2)"><span class="dot"></span><div style="flex:1"><div style="font-size:12px;font-weight:600">R4 带数字的结果</div><div class="tiny faint">3 周上线 · 每周 5 天</div></div><span class="pill g">通过</span></div>
              <div style="display:flex;align-items:center;gap:8px;padding:7px 0;border-bottom:1px solid var(--line2)"><span class="dot"></span><div style="flex:1"><div style="font-size:12px;font-weight:600">R5 到岗时间</div><div class="tiny faint">每周 5 天</div></div><span class="pill g">通过</span></div>
              <div style="display:flex;align-items:center;gap:8px;padding:7px 0;border-bottom:1px solid var(--line2)"><span class="dot"></span><div style="flex:1"><div style="font-size:12px;font-weight:600">R6 技术栈 ≤4 类</div><div class="tiny faint">uni-app / SpringBoot / 微信支付 / OSS</div></div><span class="pill g">通过</span></div>
              <div style="display:flex;align-items:center;gap:8px;padding:7px 0"><span class="dot"></span><div style="flex:1"><div style="font-size:12px;font-weight:600">R9 结尾收在问句</div><div class="tiny faint">「方便发您一份简历吗？」</div></div><span class="pill g">通过</span></div>
            </div>
            <button class="btn soft block" style="margin-top:12px">全部过闸（还剩 3 家）</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</section>

<section class="shot" data-id="s6" data-module="m3" data-title="闸门逐条修复" data-desc="告诉你为什么不过，并给出可编辑的修复建议">
  <div class="cap"><i>06</i><b>闸门逐条修复</b><span>不做静默替换；每条修复都回填到正文</span></div>
  <div class="screen">
    <div class="rail-slot" data-active="s5"></div>
    <div class="main">
      <div class="appbar">
        <div class="tt"><h2>闸门 · 逐条修复</h2><div class="sub">库莱朋 · java实习生（任务制） · 原稿 61 分 · 3 条不通过</div></div>
        <div class="right">
          <div class="status">事实库 v1.4</div>
          <div class="status">近 7 天话术 42 条</div>
          <button class="btn ghost sm">重新生成</button>
        </div>
      </div>
      <div class="content" style="display:grid;grid-template-columns:1fr 434px;gap:14px">
        <div style="display:flex;flex-direction:column;gap:14px;min-height:0">
          <div class="card" style="flex:1;display:flex;flex-direction:column">
            <div class="card-h"><h3>原稿</h3><span class="pill r">61 分 · 不过闸</span><span class="r tiny faint">视觉宽度 137 · 4 句</span></div>
            <div style="font-size:14px;line-height:2;color:#344054;background:#FCFCFD;border:1px solid var(--line);border-radius:12px;padding:14px 16px">
              您好，我是27届本科（湖南师大 计算机科学与技术），有2段Java实习，做过电商商城和后台权限，SpringBoot、MyBatis-Plus、MySQL、Redis、Vue3、uni-app 都写过。<span style="background:var(--red-soft);border-bottom:2px solid var(--red);padding:0 2px">看到岗位做 Java 后端，我觉得很匹配。可随时到岗。</span>
            </div>
            <div style="display:flex;flex-direction:column;gap:8px;margin-top:12px">
              <div class="row" style="gap:9px"><span class="pill r">R2</span><span class="tiny" style="color:#475467">「您好，我是 27 届本科（…）」是模板开场，20 条里会一模一样</span></div>
              <div class="row" style="gap:9px"><span class="pill r">R6</span><span class="tiny" style="color:#475467">堆了 6 类技术名词，超过 4 类上限</span></div>
              <div class="row" style="gap:9px"><span class="pill r">R4 / R9</span><span class="tiny" style="color:#475467">没有带数字的结果；结尾是陈述句，不是问句</span></div>
              <div class="row" style="gap:9px"><span class="pill a">R7</span><span class="tiny" style="color:#475467">与 10-23 批 #7 相似度 0.83（≥0.90 才硬拦）</span></div>
            </div>
            <div class="divider" style="margin:14px 0"></div>
            <div class="row"><span class="kicker">发送前自检三问</span></div>
            <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:8px;margin-top:8px">
              <div style="background:#F7F9FC;border-radius:10px;padding:9px 11px;font-size:11.5px;color:var(--sub)">① 只有这家能收到吗？<br><b style="color:var(--red)">否，没有公司/JD 专属信息</b></div>
              <div style="background:#F7F9FC;border-radius:10px;padding:9px 11px;font-size:11.5px;color:var(--sub)">② 回应了 JD 具体要求吗？<br><b style="color:var(--red)">否，只说了「很匹配」</b></div>
              <div style="background:#F7F9FC;border-radius:10px;padding:9px 11px;font-size:11.5px;color:var(--sub)">③ 手机 3 行能读完吗？<br><b style="color:var(--red)">否，4 行以上</b></div>
            </div>
          </div>

          <div class="card" style="flex:1;display:flex;flex-direction:column;border-color:#BFE8DF;background:#FCFFFE">
            <div class="card-h"><h3>修复稿</h3><span class="pill g">89 分 · 可发</span><span class="r tiny faint">视觉宽度 84 · 3 句</span></div>
            <div style="font-size:14px;line-height:2;color:#1D2939;background:#fff;border:1px solid #D6EFE9;border-radius:12px;padding:14px 16px">
              看到岗位是任务制的 Java 实习，我实习时按任务独立交付过商城商品/订单模块和后台权限，接口文档、联调、上线都跟过，<span class="kw hit">按时交付率 100%</span>。你们现在最急的是哪块任务？我可以每周 5 天到岗，先接一段试试。
            </div>
            <div class="row" style="margin-top:10px;gap:6px">
              <span class="pill g">R2 通过</span><span class="pill g">R4 通过</span><span class="pill g">R6 3 类</span><span class="pill g">R9 问句</span><span class="pill g">R7 0.52</span>
            </div>
            <div class="tiny faint" style="margin-top:10px">改动：删掉自我介绍开场 → 引用 JD「任务制」→ 补带数字结果 → 结尾改成问句 → 技术栈压到 3 类。原意未变。</div>
          </div>
        </div>

        <div style="display:flex;flex-direction:column;gap:14px;min-height:0">
          <div class="card">
            <div class="row" style="gap:16px;align-items:center">
              <div class="ring amber" style="--v:61;width:96px;height:96px"><div class="in"><b style="font-size:26px">61</b><span>当前</span></div></div>
              <div style="font-size:20px;color:var(--faint)">→</div>
              <div class="ring green" style="--v:89;width:96px;height:96px"><div class="in" id="fixScore" style="font-size:26px"><b style="font-size:26px">89</b><span>修复后</span></div></div>
              <div style="flex:1">
                <div style="font-size:13px;font-weight:650">3 条可自动修</div>
                <div class="tiny faint" style="margin-top:3px">R8 事实库类问题不自动修</div>
                <div class="tiny faint" style="margin-top:2px">所有改动都需你确认</div>
              </div>
            </div>
          </div>

          <div class="card" style="flex:1;display:flex;flex-direction:column" id="gateRules">
            <div class="card-h"><h3>修什么、为什么</h3><span class="sub">按影响排序</span></div>
            <div class="rule bad" style="border:1px solid #F3C9C9;background:#FFFBFB;border-radius:12px;padding:12px 13px;margin-bottom:10px">
              <div class="row"><span class="pill r">R2 硬拦</span><b style="font-size:12.5px">模板开场</b><span class="st" style="margin-left:auto;font-size:11px;color:var(--red);font-weight:650">不通过</span></div>
              <div class="tiny muted" style="margin-top:6px;line-height:1.7">「您好，我是27届本科（…）」在 78 条历史里出现过 20 次。HR 一眼看出群发。</div>
              <div class="row" style="margin-top:9px"><button class="btn sm soft">改成「看到岗位…」</button><button class="btn sm ghost">看 3 个句式</button></div>
            </div>
            <div class="rule bad" style="border:1px solid #F3C9C9;background:#FFFBFB;border-radius:12px;padding:12px 13px;margin-bottom:10px">
              <div class="row"><span class="pill r">R4 软扣</span><b style="font-size:12.5px">没有带数字的结果</b><span class="st" style="margin-left:auto;font-size:11px;color:var(--red);font-weight:650">-8</span></div>
              <div class="tiny muted" style="margin-top:6px;line-height:1.7">事实库里可用数字：商城订单模块 3 个、接口 12 个、按时交付率 100%。</div>
              <div class="row" style="margin-top:9px"><button class="btn sm soft">插入「按时交付率 100%」</button></div>
            </div>
            <div class="rule bad" style="border:1px solid #F3C9C9;background:#FFFBFB;border-radius:12px;padding:12px 13px;margin-bottom:10px">
              <div class="row"><span class="pill r">R9 软扣</span><b style="font-size:12.5px">结尾不是问句</b><span class="st" style="margin-left:auto;font-size:11px;color:var(--red);font-weight:650">-6</span></div>
              <div class="tiny muted" style="margin-top:6px;line-height:1.7">结尾「可随时到岗。」没有给 HR 一个低成本回复的入口。</div>
              <div class="row" style="margin-top:9px"><button class="btn sm soft">改成「方便发您一份简历吗？」</button></div>
            </div>
            <div class="rule" style="border:1px solid var(--line);border-radius:12px;padding:12px 13px;margin-bottom:10px">
              <div class="row"><span class="pill a">R7 动态</span><b style="font-size:12.5px">与近 7 天话术相似 0.83</b><span class="st" style="margin-left:auto;font-size:11px;color:var(--amber);font-weight:650">提醒</span></div>
              <div class="tiny muted" style="margin-top:6px">最相似：10-23 批 #7（0.83）。未到 0.90 硬拦线，但同一批里两条开头不要相同。</div>
            </div>
            <div class="rule" style="border:1px solid var(--line);border-radius:12px;padding:12px 13px">
              <div class="row"><span class="pill">R8 硬拦</span><b style="font-size:12.5px">事实库外说法</b><span class="st" style="margin-left:auto;font-size:11px;color:var(--green);font-weight:650">通过</span></div>
              <div class="tiny muted" style="margin-top:6px">未出现硕士 / 3 年经验 / 大厂等禁止声明。此类问题不提供一键修复，只能删或人工改写。</div>
            </div>

            <div id="fixNote" class="tiny" style="margin-top:auto;padding-top:12px;color:var(--faint)">建议按 R2 → R4 → R9 顺序修，分数会实时重算。</div>
            <button class="btn primary block" id="fixAll" style="margin-top:10px">应用全部修复（3 条）</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</section>
`);
