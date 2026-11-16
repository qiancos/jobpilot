document.getElementById('board').insertAdjacentHTML('beforeend', `
<section class="shot" data-id="s10" data-module="m5" data-title="台账与漏斗" data-desc="78 条历史 + 本批；任一投递可追溯">
  <div class="cap"><i>10</i><b>台账与漏斗</b><span>不只看送达，也看哪一步漏了</span></div>
  <div class="screen">
    <div class="rail-slot" data-active="s10"></div>
    <div class="main">
      <div class="appbar">
        <div class="tt"><h2>台账与漏斗</h2><div class="sub">78 条历史 + 本批 8 条 · 数据全本地 · 上次同步 21:07</div></div>
        <div class="right">
          <div class="status">近 30 天</div>
          <div class="status">全部方向</div>
          <button class="btn ghost sm">导出 xlsx</button>
        </div>
      </div>
      <div class="content" style="display:grid;grid-template-columns:442px 1fr;gap:14px">
        <div style="display:flex;flex-direction:column;gap:14px;min-height:0">
          <div class="card" style="flex:1;display:flex;flex-direction:column">
            <div class="card-h"><h3>投递漏斗</h3><span class="sub">候选 → 约面</span><span class="r pill t">近 30 天</span></div>
            <div style="display:flex;flex-direction:column;gap:7px;flex:1;justify-content:center">
              <div><div class="row tiny"><span style="width:40px;color:var(--sub)">候选</span><span class="spacer"></span><b>136</b></div><div class="bar" style="margin-top:4px"><i style="width:100%"></i></div></div>
              <div><div class="row tiny"><span style="width:40px;color:var(--sub)">审批</span><span class="spacer"></span><b>86</b><span class="faint" style="margin-left:6px">63%</span></div><div class="bar" style="margin-top:4px"><i style="width:63%"></i></div></div>
              <div><div class="row tiny"><span style="width:40px;color:var(--sub)">送达</span><span class="spacer"></span><b>78</b><span class="faint" style="margin-left:6px">91%</span></div><div class="bar" style="margin-top:4px"><i class="g" style="width:57%"></i></div></div>
              <div><div class="row tiny"><span style="width:40px;color:var(--sub)">已读</span><span class="spacer"></span><b>41</b><span class="faint" style="margin-left:6px">53%</span></div><div class="bar" style="margin-top:4px"><i class="g" style="width:30%"></i></div></div>
              <div><div class="row tiny"><span style="width:40px;color:var(--sub)">回复</span><span class="spacer"></span><b>9</b><span class="faint" style="margin-left:6px">22%</span></div><div class="bar" style="margin-top:4px"><i class="a" style="width:7%"></i></div></div>
              <div><div class="row tiny"><span style="width:40px;color:var(--sub)">约面</span><span class="spacer"></span><b>2</b><span class="faint" style="margin-left:6px">22%</span></div><div class="bar" style="margin-top:4px"><i class="a" style="width:2%"></i></div></div>
            </div>
            <div style="margin-top:12px;padding-top:10px;border-top:1px solid var(--line2)"><div class="tiny faint">北极星：每周真人 HR 回复数 · 本周 3 / 目标 5</div></div>
          </div>

          <div class="card" style="height:292px">
            <div class="card-h"><h3>话术 A/B</h3><span class="sub">按版本看送达与回复</span></div>
            <div style="display:flex;flex-direction:column;gap:12px;margin-top:4px">
              <div>
                <div class="row"><span class="pill t">A 技术证据型</span><span class="tiny faint" style="margin-left:auto">42 投</span></div>
                <div style="display:flex;align-items:center;gap:8px;margin-top:7px"><span class="tiny faint" style="width:34px">送达</span><div class="bar" style="flex:1"><i style="width:96%"></i></div><b style="font-size:12px">96%</b></div>
                <div style="display:flex;align-items:center;gap:8px;margin-top:5px"><span class="tiny faint" style="width:34px">回复</span><div class="bar" style="flex:1"><i class="a" style="width:12%"></i></div><b style="font-size:12px">6%</b></div>
              </div>
              <div>
                <div class="row"><span class="pill b">B 业务理解型</span><span class="tiny faint" style="margin-left:auto">36 投</span></div>
                <div style="display:flex;align-items:center;gap:8px;margin-top:7px"><span class="tiny faint" style="width:34px">送达</span><div class="bar" style="flex:1"><i style="width:92%"></i></div><b style="font-size:12px">92%</b></div>
                <div style="display:flex;align-items:center;gap:8px;margin-top:5px"><span class="tiny faint" style="width:34px">回复</span><div class="bar" style="flex:1"><i class="a" style="width:8%"></i></div><b style="font-size:12px">4%</b></div>
              </div>
            </div>
            <div class="divider" style="margin:12px 0"></div>
            <div class="tiny muted" style="line-height:1.75">A 版回复率略高，但样本仍小；R7 相似度高的批次集中在 10-21，已单列。</div>
          </div>
        </div>

        <div style="display:flex;flex-direction:column;gap:14px;min-height:0">
          <div class="row" style="flex:none">
            <div style="display:flex;align-items:center;gap:8px;height:36px;padding:0 12px;border:1px solid var(--line);border-radius:10px;background:#fff;width:280px">
              <svg viewBox="0 0 24 24" style="width:15px;height:15px;stroke:#98A2B3;fill:none;stroke-width:1.8"><circle cx="11" cy="11" r="7"/><path d="M16.5 16.5L21 21"/></svg>
              <span class="tiny faint">搜索公司 / 岗位 / 关键词</span>
            </div>
            <span class="chip on">全部状态</span><span class="chip">已送达</span><span class="chip">失败</span><span class="chip">已回复</span>
            <div class="spacer"></div>
            <span class="tiny faint">点任意行展开关键词 / 话术 / 时间</span>
          </div>

          <div class="card pad0" style="flex:1;display:flex;flex-direction:column;overflow:hidden">
            <div class="tbl" style="flex:1">
              <div class="th" style="grid-template-columns:64px 1.1fr 1.3fr 54px 84px 1fr">
                <span>日期</span><span>公司</span><span>岗位</span><span>闸门</span><span>状态</span><span>核验 / 失败原因</span>
              </div>
              <div class="tr" style="grid-template-columns:64px 1.1fr 1.3fr 54px 84px 1fr;height:52px">
                <span class="mono tiny faint">11-16</span><div class="ttl">艾特数字</div><div class="tiny muted" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis">java开发实习生</div><span class="scoretag" style="width:34px;height:26px;font-size:12px">88</span><span class="pill g">已送达</span><span class="tiny">L3 · 21:01:12 · 指纹匹配</span>
              </div>
              <div class="tr" style="grid-template-columns:64px 1.1fr 1.3fr 54px 84px 1fr;height:52px">
                <span class="mono tiny faint">11-16</span><div class="ttl">青蓝智慧</div><div class="tiny muted" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis">全栈实习生</div><span class="scoretag" style="width:34px;height:26px;font-size:12px">86</span><span class="pill g">已送达</span><span class="tiny">L3 · 21:02:04 · 指纹匹配</span>
              </div>
              <div class="tr" style="grid-template-columns:64px 1.1fr 1.3fr 54px 84px 1fr;height:52px">
                <span class="mono tiny faint">11-16</span><div class="ttl">广州矣润技术</div><div class="tiny muted" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis">开发实习工程师</div><span class="scoretag" style="width:34px;height:26px;font-size:12px">85</span><span class="pill g">已送达</span><span class="tiny">L3 · 21:03:06 · 指纹匹配</span>
              </div>
              <div class="tr" style="grid-template-columns:64px 1.1fr 1.3fr 54px 84px 1fr;height:52px">
                <span class="mono tiny faint">11-16</span><div class="ttl">嘉为科技</div><div class="tiny muted" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis">Java 后端研发</div><span class="scoretag" style="width:34px;height:26px;font-size:12px">80</span><span class="pill r">失败</span><span class="tiny" style="color:var(--red)">L2 · session_not_found</span>
              </div>
              <div class="tr" style="grid-template-columns:64px 1.1fr 1.3fr 54px 84px 1fr;height:52px">
                <span class="mono tiny faint">11-16</span><div class="ttl">文云文化</div><div class="tiny muted" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis">AI 应用开发</div><span class="scoretag" style="width:34px;height:26px;font-size:12px">83</span><span class="pill a">已跳</span><span class="tiny">already_sent · 5 分钟内</span>
              </div>
              <div class="tr" style="grid-template-columns:64px 1.1fr 1.3fr 54px 84px 1fr;height:52px">
                <span class="mono tiny faint">10-28</span><div class="ttl">景游游戏</div><div class="tiny muted" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis">java服务端</div><span class="scoretag mid" style="width:34px;height:26px;font-size:12px">72</span><span class="pill">默认语</span><span class="tiny">仅默认语 · 未补专属语</span>
              </div>
              <div class="tr" style="grid-template-columns:64px 1.1fr 1.3fr 54px 84px 1fr;height:52px">
                <span class="mono tiny faint">10-23</span><div class="ttl">芯海智控</div><div class="tiny muted" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis">Python 后端</div><span class="scoretag mid" style="width:34px;height:26px;font-size:12px">74</span><span class="pill g">已送达</span><span class="tiny">L3 · 20:42:18 · 指纹匹配</span>
              </div>
              <div class="tr" style="grid-template-columns:64px 1.1fr 1.3fr 54px 84px 1fr;height:52px">
                <span class="mono tiny faint">10-21</span><div class="ttl">白码</div><div class="tiny muted" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis">Java 后端</div><span class="scoretag low" style="width:34px;height:26px;font-size:12px">58</span><span class="pill b">已回复</span><span class="tiny">先过简历，合适邀约</span>
              </div>
            </div>
          </div>

          <div class="card" style="height:112px;background:#FBFCFE;border-style:dashed">
            <div class="row"><span class="dot a"></span><b style="font-size:12.5px">历史洞察</b><span class="pill r" style="margin-left:auto">必须同时看两个指标</span></div>
            <div style="font-size:12px;line-height:1.8;color:#475467;margin-top:6px">
              10-21 批：<b>送达 20/20，过闸 0/20</b>（平均视觉宽度 137）｜10-28 批：<b>过闸 14/15，送达 0/15</b>。<br>
              送得最到位的质量最差，质量最好的没送到——所以本产品把「闸门」和「L3 核验」放在同一条链上。
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</section>

<section class="shot" data-id="s11" data-module="m5" data-title="会话管家" data-desc="意图分类 + 草稿；只填入，不自动发送">
  <div class="cap"><i>11</i><b>会话管家</b><span>HR 原话 + 事实库 → 草稿；发送仍然是人</span></div>
  <div class="screen">
    <div class="rail-slot" data-active="s11"></div>
    <div class="main">
      <div class="appbar">
        <div class="tt"><h2>会话管家</h2><div class="sub">未读 2 · 上次刷新 21:16 · 前台每 10 分钟刷新一次</div></div>
        <div class="right">
          <div class="status"><span class="dot"></span>BOSS 消息已同步</div>
          <button class="btn ghost sm">刷新</button>
        </div>
      </div>
      <div class="content" style="display:grid;grid-template-columns:300px 1fr 344px;gap:14px">
        <div class="card pad0" style="display:flex;flex-direction:column;overflow:hidden">
          <div style="padding:12px 14px;border-bottom:1px solid var(--line)">
            <div style="display:flex;align-items:center;gap:8px;height:32px;background:#F4F6FA;border-radius:8px;padding:0 10px">
              <svg viewBox="0 0 24 24" style="width:13px;height:13px;stroke:#98A2B3;fill:none;stroke-width:1.8"><circle cx="11" cy="11" r="7"/><path d="M16.5 16.5L21 21"/></svg>
              <span class="tiny faint">搜索公司 / HR</span>
            </div>
            <div class="row" style="margin-top:10px;gap:14px"><span class="tiny" style="font-weight:700">未读 2</span><span class="tiny faint">全部 14</span><span class="tiny faint">待跟进 3</span></div>
          </div>
          <div style="flex:1;display:flex;flex-direction:column">
            <div style="padding:12px 14px;background:var(--brand-soft);border-left:3px solid var(--brand)">
              <div class="row"><b style="font-size:12.5px">文云文化</b><span class="pill b sm" style="margin-left:auto">要简历</span></div>
              <div class="tiny muted" style="margin-top:5px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">方便发一份简历吗？我们主要做 AI 应用</div>
              <div class="row" style="margin-top:6px"><span class="tiny faint">HR 陈女士</span><span class="tiny faint" style="margin-left:auto">14:20</span></div>
            </div>
            <div style="padding:12px 14px;border-bottom:1px solid var(--line2)">
              <div class="row"><b style="font-size:12.5px">白码</b><span class="pill g sm" style="margin-left:auto">邀面</span></div>
              <div class="tiny muted" style="margin-top:5px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">周三下午方便过来聊一下吗？</div>
              <div class="row" style="margin-top:6px"><span class="tiny faint">HR 李先生</span><span class="tiny faint" style="margin-left:auto">昨天</span></div>
            </div>
            <div style="padding:12px 14px;border-bottom:1px solid var(--line2)">
              <div class="row"><b style="font-size:12.5px">艾特数字</b><span class="pill sm" style="margin-left:auto">无回复</span></div>
              <div class="tiny muted" style="margin-top:5px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">你已发送专属问候</div>
              <div class="row" style="margin-top:6px"><span class="tiny faint">HR 招聘专员</span><span class="tiny faint" style="margin-left:auto">21:01</span></div>
            </div>
            <div style="padding:12px 14px;border-bottom:1px solid var(--line2)">
              <div class="row"><b style="font-size:12.5px">欢聚集团</b><span class="pill r sm" style="margin-left:auto">已拒绝</span></div>
              <div class="tiny muted" style="margin-top:5px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">我们找的是营收产品方向</div>
              <div class="row" style="margin-top:6px"><span class="tiny faint">HR 王女士</span><span class="tiny faint" style="margin-left:auto">10-21</span></div>
            </div>
            <div style="padding:12px 14px">
              <div class="row"><b style="font-size:12.5px">穹矩科技</b><span class="pill a sm" style="margin-left:auto">问薪资</span></div>
              <div class="tiny muted" style="margin-top:5px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">期望薪资大概多少？</div>
              <div class="row" style="margin-top:6px"><span class="tiny faint">HR 张女士</span><span class="tiny faint" style="margin-left:auto">10-28</span></div>
            </div>
          </div>
        </div>

        <div class="card" style="display:flex;flex-direction:column;padding:0;overflow:hidden">
          <div style="padding:14px 18px;border-bottom:1px solid var(--line);display:flex;align-items:center;gap:10px">
            <div style="width:34px;height:34px;border-radius:50%;background:#EAF0F7;display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:650;color:#52627A">文</div>
            <div><div style="font-size:13.5px;font-weight:650">文云文化 · HR 陈女士</div><div class="tiny faint" style="margin-top:2px">AI 应用开发实习生 · 150-300元/天 · 广州·天河</div></div>
            <div class="spacer"></div>
            <span class="pill g">L3 已送达</span>
          </div>

          <div style="flex:1;padding:18px;display:flex;flex-direction:column;gap:12px;background:#FBFCFE">
            <div style="text-align:center"><span class="tiny faint">今天 21:01 · 你发送了专属问候</span></div>
            <div style="align-self:flex-end;max-width:78%;background:var(--brand-soft);border-radius:12px 12px 3px 12px;padding:11px 13px;font-size:12.5px;line-height:1.8;color:#1D3B3A">
              看到岗位做多 AI 协同的应用研发，我用 LangGraph 搭过多阶段工作流、用 RAG 做过电商客服（自动应答解决率 85%），大模型 API 接入和 Function Call 都写过。27 届，可每周 5 天到岗，方便发您一份简历吗？
            </div>
            <div style="align-self:flex-end"><span class="tiny" style="color:var(--green)">已送达 · L3 指纹校验通过</span></div>

            <div style="align-self:flex-start;max-width:70%;background:#fff;border:1px solid var(--line);border-radius:12px 12px 12px 3px;padding:11px 13px;font-size:12.5px;line-height:1.8">
              方便发一份简历吗？我们这边主要做 AI 应用，实习可以转正。
            </div>
            <div style="align-self:flex-start"><span class="tiny faint">14:20 · 已读</span></div>

            <div style="margin-top:auto;background:#fff;border:1px solid #CFE9E6;border-radius:14px;padding:13px 14px">
              <div class="row"><span class="pill t">AI 草稿</span><span class="tiny faint">基于 HR 原话 + 事实库</span><span class="pill g sm" style="margin-left:auto">未发送</span></div>
              <div style="font-size:12.5px;line-height:1.8;margin-top:8px;color:#1D2939">可以，简历 PDF 已附上：<b>张三-Java全栈-27届.pdf</b>。周三下午我都有空，您看 15:00 还是 16:30 方便？</div>
              <div class="row" style="margin-top:10px;gap:7px"><button class="btn sm ghost">重新生成</button><button class="btn sm soft">填入输入框</button><div class="spacer"></div><button class="btn sm primary">发送</button></div>
            </div>
          </div>

          <div style="border-top:1px solid var(--line);padding:12px 16px;display:flex;align-items:center;gap:10px">
            <div style="flex:1;height:38px;border:1px solid var(--line);border-radius:10px;display:flex;align-items:center;padding:0 12px;font-size:12px;color:var(--faint)">输入回复…（草稿不会自动发送）</div>
            <button class="btn sm ghost">附件</button>
          </div>
        </div>

        <div style="display:flex;flex-direction:column;gap:14px;min-height:0">
          <div class="card">
            <div class="card-h"><h3>意图识别</h3><span class="r"><span class="pill b">要简历</span></span></div>
            <div class="row" style="gap:12px;align-items:center">
              <div class="ring sm" style="--v:92;width:70px;height:70px"><div class="in"><b style="font-size:19px">92</b><span>置信度</span></div></div>
              <div style="flex:1"><div class="tiny muted" style="line-height:1.8">依据：HR 原话「方便发一份简历吗」+ 岗位仍在招 + 未提薪资。</div><div class="tiny faint" style="margin-top:4px">分类：邀面 / 要简历 / 问薪资 / 拒绝 / 无回复</div></div>
            </div>
          </div>

          <div class="card">
            <div class="card-h"><h3>事实库命中</h3><span class="sub">只说这里有的</span></div>
            <div style="display:flex;flex-direction:column;gap:7px">
              <div class="row" style="gap:8px"><span class="pill g">简历</span><span class="tiny">简历-v2.8-Java.pdf</span></div>
              <div class="row" style="gap:8px"><span class="pill g">到岗</span><span class="tiny">立即到岗 · 每周 5 天</span></div>
              <div class="row" style="gap:8px"><span class="pill g">到岗</span><span class="tiny">周三下午有空</span></div>
              <div class="row" style="gap:8px"><span class="pill r">禁用</span><span class="tiny">不提薪资数字，不说「随时入职」</span></div>
            </div>
          </div>

          <div class="card">
            <div class="card-h"><h3>待办</h3><span class="r tiny faint">2 项</span></div>
            <div style="display:flex;flex-direction:column;gap:9px">
              <div class="row" style="gap:8px"><span class="cb on"></span><span style="font-size:12px">发送简历 PDF + 约面试时间</span></div>
              <div class="row" style="gap:8px"><span class="cb"></span><span style="font-size:12px">准备 AI 应用项目 2 分钟讲解</span></div>
            </div>
            <div class="divider" style="margin:11px 0"></div>
            <div class="row"><span class="tiny faint">面试日历</span><span class="tiny" style="margin-left:auto;color:var(--brand-ink)">11-18 周三</span></div>
            <div style="background:var(--brand-soft);border-radius:10px;padding:9px 11px;margin-top:7px">
              <div style="font-size:12px;font-weight:650">白码科技 · 一面（技术）</div>
              <div class="tiny" style="color:var(--brand-ink);margin-top:3px">15:00 · 腾讯会议 · 准备清单 3 项</div>
            </div>
          </div>

          <div class="card" style="background:#FFFDF7;border-color:#F1E1BA">
            <div class="row"><span class="dot a"></span><span style="font-size:12px;font-weight:650">不代聊</span></div>
            <div class="tiny" style="color:#8A6A22;line-height:1.75;margin-top:5px">AI 只起稿，不替你谈薪、不自动回复。发送前请自己读一遍。</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</section>
`);
