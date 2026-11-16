document.getElementById('board').insertAdjacentHTML('beforeend', `
<section class="shot" data-id="s12" data-module="m6" data-title="简历库" data-desc="3 个方向版本，按岗位自动推荐；不做逐岗 PDF">
  <div class="cap"><i>12</i><b>简历库</b><span>3 版覆盖 3 个方向；改简历必须同步事实库</span></div>
  <div class="screen">
    <div class="rail-slot" data-active="s12"></div>
    <div class="main">
      <div class="appbar">
        <div class="tt"><h2>简历库</h2><div class="sub">3 个方向 · 按岗位自动选 · 不做逐岗定制 PDF</div></div>
        <div class="right">
          <div class="status">事实库 lexicon v1.4</div>
          <button class="btn ghost sm">导入新版本</button>
          <button class="btn primary sm">新增方向</button>
        </div>
      </div>
      <div class="content">
        <div class="row" style="flex:none;background:#F7F9FC;border:1px solid var(--line);border-radius:12px;padding:10px 14px">
          <span class="dot t"></span>
          <span style="font-size:12px;color:#475467">为什么不做「一岗一份」：投递对象是实习/初级岗，HR 看的是匹配度不是排版；3 个方向已经覆盖 Java / AI / 产品。</span>
          <span class="pill" style="margin-left:auto">UPCV 的 47 份 PDF 不抄</span>
        </div>

        <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:14px;height:318px;flex:none">
          <div class="card" style="border-color:#B9E6E2;box-shadow:0 10px 26px rgba(0,156,151,.12);display:flex;flex-direction:column">
            <div class="row"><span class="pill t">默认版本</span><span class="pill g" style="margin-left:auto">使用中</span></div>
            <div style="font-size:19px;font-weight:730;margin-top:12px">Java 后端 · v2.8</div>
            <div class="tiny faint" style="margin-top:5px">2026-11-12 更新 · 2 段实习 · 6-7K</div>
            <div class="row" style="gap:5px;flex-wrap:wrap;margin-top:12px">
              <span class="chip sm">Java</span><span class="chip sm">Spring Boot</span><span class="chip sm">MyBatis</span><span class="chip sm">MySQL</span><span class="chip sm">小程序</span>
            </div>
            <div style="margin-top:12px;background:#F7F9FC;border-radius:10px;padding:9px 11px">
              <div class="row tiny"><span class="faint">关联投递</span><span class="spacer"></span><b>62</b></div>
              <div class="row tiny" style="margin-top:4px"><span class="faint">回复率</span><span class="spacer"></span><b>11%</b></div>
            </div>
            <div class="spacer"></div>
            <div class="row" style="gap:7px"><button class="btn sm ghost" style="flex:1">预览</button><button class="btn sm soft" style="flex:1">设为默认</button></div>
          </div>

          <div class="card" style="display:flex;flex-direction:column">
            <div class="row"><span class="pill b">备选方向</span><span class="pill" style="margin-left:auto">可用</span></div>
            <div style="font-size:19px;font-weight:730;margin-top:12px">AI 应用 · v1.2</div>
            <div class="tiny faint" style="margin-top:5px">2026-11-08 更新 · RAG / Agent 项目为主</div>
            <div class="row" style="gap:5px;flex-wrap:wrap;margin-top:12px">
              <span class="chip sm">LangGraph</span><span class="chip sm">RAG</span><span class="chip sm">Function Call</span><span class="chip sm">Redis</span>
            </div>
            <div style="margin-top:12px;background:#F7F9FC;border-radius:10px;padding:9px 11px">
              <div class="row tiny"><span class="faint">关联投递</span><span class="spacer"></span><b>9</b></div>
              <div class="row tiny" style="margin-top:4px"><span class="faint">回复率</span><span class="spacer"></span><b>22%</b></div>
            </div>
            <div class="spacer"></div>
            <div class="row" style="gap:7px"><button class="btn sm ghost" style="flex:1">预览</button><button class="btn sm ghost" style="flex:1">设为默认</button></div>
          </div>

          <div class="card" style="display:flex;flex-direction:column">
            <div class="row"><span class="pill">备选方向</span><span class="pill" style="margin-left:auto">可用</span></div>
            <div style="font-size:19px;font-weight:730;margin-top:12px">产品 · v1.0</div>
            <div class="tiny faint" style="margin-top:5px">2026-10-30 更新 · 技术 + 产品双背景</div>
            <div class="row" style="gap:5px;flex-wrap:wrap;margin-top:12px">
              <span class="chip sm">PRD</span><span class="chip sm">Axure</span><span class="chip sm">SQL</span><span class="chip sm">用户调研</span>
            </div>
            <div style="margin-top:12px;background:#F7F9FC;border-radius:10px;padding:9px 11px">
              <div class="row tiny"><span class="faint">关联投递</span><span class="spacer"></span><b>7</b></div>
              <div class="row tiny" style="margin-top:4px"><span class="faint">回复率</span><span class="spacer"></span><b>0%</b></div>
            </div>
            <div class="spacer"></div>
            <div class="row" style="gap:7px"><button class="btn sm ghost" style="flex:1">预览</button><button class="btn sm ghost" style="flex:1">设为默认</button></div>
          </div>
        </div>

        <div style="display:grid;grid-template-columns:1fr 1fr;gap:14px;flex:1;min-height:0">
          <div class="card" style="display:flex;flex-direction:column">
            <div class="card-h"><h3>当前岗位建议</h3><span class="sub">艾特数字 · java开发实习生</span><span class="r pill g">自动匹配</span></div>
            <div class="row" style="gap:14px">
              <div style="width:52px;height:52px;border-radius:12px;background:var(--brand-soft);color:var(--brand-ink);display:flex;align-items:center;justify-content:center;font-weight:750">v2.8</div>
              <div>
                <div style="font-size:14px;font-weight:700">建议使用 Java 后端 v2.8</div>
                <div class="tiny faint" style="margin-top:3px">不是按岗位生成，而是按方向选版本</div>
              </div>
            </div>
            <div class="divider" style="margin:14px 0"></div>
            <div style="display:flex;flex-direction:column;gap:8px;font-size:12.5px;color:#475467">
              <div class="row"><span class="dot"></span>JD 命中 Spring / MyBatis / MySQL / uni-app</div>
              <div class="row"><span class="dot"></span>岗位要求「独立推进小模块」，v2.8 把小程序全流程放在第一段</div>
              <div class="row"><span class="dot"></span>未命中 Kafka / 微服务治理，话术中不出现</div>
            </div>
            <div class="spacer"></div>
            <div class="row" style="gap:8px;margin-top:14px"><button class="btn sm soft">用这版投递</button><button class="btn sm ghost">看完整简历</button></div>
          </div>

          <div class="card" style="display:flex;flex-direction:column">
            <div class="card-h"><h3>版本差异</h3><span class="sub">只调顺序与措辞，不改事实</span></div>
            <div style="display:grid;grid-template-columns:96px 1fr 1fr;gap:8px;font-size:11.5px">
              <div class="faint">模块</div><div class="faint">v2.8 Java</div><div class="faint">v1.2 AI</div>
              <div style="font-weight:650">首段经历</div><div>摄影预约小程序全流程</div><div>RAG 电商客服（85%）</div>
              <div style="font-weight:650">技能排序</div><div>Java / Spring / MySQL</div><div>LangGraph / RAG / Python</div>
              <div style="font-weight:650">项目数</div><div>2 个（商城 / 小程序）</div><div>3 个（RAG / Agent / 剪辑）</div>
            </div>
            <div class="divider" style="margin:12px 0"></div>
            <div style="background:var(--amber-soft);border-radius:10px;padding:10px 12px;font-size:11.5px;line-height:1.75;color:#8A6A22">
              改简历后必须同步 <span class="mono">FACTS</span>，否则闸门 R8 会把你真实的新经历判成「事实不实」。当前事实库版本 v1.4（11-12）。
            </div>
            <div class="spacer"></div>
            <div class="row" style="gap:8px;margin-top:12px"><button class="btn sm ghost">编辑事实库</button><button class="btn sm ghost">查看变更记录</button></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</section>

<section class="shot" data-id="s13" data-module="m6" data-title="网申代填" data-desc="识别 31 个字段，缺失项高亮不猜，人工提交">
  <div class="cap"><i>13</i><b>网申代填</b><span>只代填、不提交；缺失项宁可留空</span></div>
  <div class="screen">
    <div class="rail-slot" data-active="s13"></div>
    <div class="main">
      <div class="appbar">
        <div class="tt"><h2>网申代填 · 北森 iTalent</h2><div class="sub">2026 届校园招聘 · 应聘登记表 · 31 个字段 · 6 个部分</div></div>
        <div class="right">
          <div class="status">WebView 通道</div>
          <div class="status">不自动提交</div>
          <button class="btn ghost sm">切换 Moka</button>
        </div>
      </div>
      <div class="content" style="display:grid;grid-template-columns:1fr 408px;gap:14px">
        <div class="card pad0" style="overflow:hidden;display:flex;flex-direction:column">
          <div style="height:42px;background:#F4F6FA;border-bottom:1px solid var(--line);display:flex;align-items:center;gap:10px;padding:0 14px">
            <div class="row" style="gap:5px"><span style="width:8px;height:8px;border-radius:50%;background:#E5A3A3"></span><span style="width:8px;height:8px;border-radius:50%;background:#E8C98E"></span><span style="width:8px;height:8px;border-radius:50%;background:#A9D6B8"></span></div>
            <div style="flex:1;height:26px;background:#fff;border:1px solid var(--line);border-radius:6px;display:flex;align-items:center;padding:0 10px;font-size:11px;color:var(--faint)">https://xyz.zhiye.com/campus/apply/form</div>
            <span class="pill" style="background:var(--green-soft);color:var(--green)">识别到 31 项</span>
          </div>

          <div style="flex:1;padding:22px 30px;overflow:hidden;background:#fff">
            <div style="font-size:18px;font-weight:730">2026 届校园招聘 · 应聘登记表</div>
            <div class="tiny faint" style="margin-top:5px">共 6 部分 · 带 * 为必填项 · 请如实填写</div>
            <div class="row" style="margin-top:14px;gap:6px">
              <span class="chip sm on">1 基本信息</span><span class="chip sm">2 教育背景</span><span class="chip sm">3 语言技能</span><span class="chip sm">4 实习经历</span><span class="chip sm">5 求职意向</span><span class="chip sm">6 自我评价</span>
            </div>
            <div class="divider" style="margin:16px 0"></div>

            <div class="kicker">一、基本信息</div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px 18px;margin-top:12px">
              <div><div class="tiny" style="margin-bottom:4px">姓名 <span style="color:var(--red)">*</span></div><div style="height:34px;border:1px solid var(--line);border-radius:7px;display:flex;align-items:center;padding:0 10px;font-size:12px;background:#F9FBFD">张三</div></div>
              <div><div class="tiny" style="margin-bottom:4px">性别 <span style="color:var(--red)">*</span></div><div style="height:34px;border:1px solid var(--line);border-radius:7px;display:flex;align-items:center;padding:0 10px;font-size:12px;background:#F9FBFD">男</div></div>
              <div><div class="tiny" style="margin-bottom:4px">手机号码 <span style="color:var(--red)">*</span></div><div style="height:34px;border:1px solid var(--line);border-radius:7px;display:flex;align-items:center;padding:0 10px;font-size:12px;background:#F9FBFD">135****8866</div></div>
              <div><div class="tiny" style="margin-bottom:4px">电子邮箱 <span style="color:var(--red)">*</span></div><div style="height:34px;border:1px solid var(--line);border-radius:7px;display:flex;align-items:center;padding:0 10px;font-size:12px;background:#F9FBFD">gaosheng@example.com</div></div>
              <div><div class="tiny" style="margin-bottom:4px">出生年月 <span style="color:var(--red)">*</span> <span class="pill a" style="height:18px;font-size:9px;padding:0 5px;margin-left:3px">缺失</span></div><div style="height:34px;border:1.5px dashed #E4B45C;border-radius:7px;display:flex;align-items:center;padding:0 10px;font-size:11.5px;background:#FFFBF0;color:#A97516">需你补充，不猜</div></div>
              <div><div class="tiny" style="margin-bottom:4px">籍贯</div><div style="height:34px;border:1.5px dashed #E4B45C;border-radius:7px;display:flex;align-items:center;padding:0 10px;font-size:11.5px;background:#FFFBF0;color:#A97516">事实库无此字段</div></div>
            </div>

            <div class="kicker" style="margin-top:18px">二、教育背景</div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px 18px;margin-top:12px">
              <div><div class="tiny" style="margin-bottom:4px">毕业院校 <span style="color:var(--red)">*</span></div><div style="height:34px;border:1px solid var(--line);border-radius:7px;display:flex;align-items:center;padding:0 10px;font-size:12px;background:#F9FBFD">示例大学</div></div>
              <div><div class="tiny" style="margin-bottom:4px">所学专业 <span style="color:var(--red)">*</span></div><div style="height:34px;border:1px solid var(--line);border-radius:7px;display:flex;align-items:center;padding:0 10px;font-size:12px;background:#F9FBFD">计算机科学与技术</div></div>
              <div><div class="tiny" style="margin-bottom:4px">最高学历 <span style="color:var(--red)">*</span></div><div style="height:34px;border:1px solid var(--line);border-radius:7px;display:flex;align-items:center;padding:0 10px;font-size:12px;background:#F9FBFD">本科</div></div>
              <div><div class="tiny" style="margin-bottom:4px">毕业时间 <span style="color:var(--red)">*</span></div><div style="height:34px;border:1px solid var(--line);border-radius:7px;display:flex;align-items:center;padding:0 10px;font-size:12px;background:#F9FBFD">2027-06</div></div>
              <div><div class="tiny" style="margin-bottom:4px">GPA / 排名</div><div style="height:34px;border:1.5px dashed #E4B45C;border-radius:7px;display:flex;align-items:center;padding:0 10px;font-size:11.5px;background:#FFFBF0;color:#A97516">需你补充，不猜</div></div>
              <div><div class="tiny" style="margin-bottom:4px">院校类型</div><div style="height:34px;border:1px solid var(--line);border-radius:7px;display:flex;align-items:center;padding:0 10px;font-size:12px;background:#F9FBFD">双一流</div></div>
            </div>

            <div style="margin-top:20px;display:flex;align-items:center;gap:10px;background:#F7F9FC;border-radius:10px;padding:10px 12px">
              <span class="dot a"></span><span style="font-size:11.5px;color:#475467">已填字段为灰色只读回填；黄色虚线框是缺失项，需要你手动补充。</span>
              <span class="spacer"></span><span class="pill">24 / 31</span>
            </div>
          </div>
        </div>

        <div style="display:flex;flex-direction:column;gap:14px;min-height:0">
          <div class="card">
            <div class="card-h"><h3>识别结果</h3><span class="r"><span class="pill g">24 已填</span><span class="pill a">7 缺失</span></span></div>
            <div class="row" style="gap:10px;margin-bottom:10px">
              <div class="bar" style="flex:1"><i style="width:77%"></i></div><b style="font-size:13px">77%</b>
            </div>
            <div class="tiny faint" style="line-height:1.7">识别方式：label 文本归一化 → 匹配事实库字段 → 填值。匹配不到就留空，<b style="color:var(--amber)">不猜</b>。</div>
          </div>

          <div class="card" style="flex:1;display:flex;flex-direction:column">
            <div class="card-h"><h3>需要你补充的 7 项</h3><span class="sub">缺失项合集</span></div>
            <div style="display:flex;flex-direction:column;gap:0">
              <div class="row" style="padding:9px 0;border-bottom:1px solid var(--line2)"><span class="pill a">必填</span><span style="font-size:12px;margin-left:8px">出生年月</span><span class="tiny faint" style="margin-left:auto">事实库无</span></div>
              <div class="row" style="padding:9px 0;border-bottom:1px solid var(--line2)"><span class="pill">选填</span><span style="font-size:12px;margin-left:8px">籍贯</span><span class="tiny faint" style="margin-left:auto">事实库无</span></div>
              <div class="row" style="padding:9px 0;border-bottom:1px solid var(--line2)"><span class="pill a">必填</span><span style="font-size:12px;margin-left:8px">GPA / 排名</span><span class="tiny faint" style="margin-left:auto">未填写</span></div>
              <div class="row" style="padding:9px 0;border-bottom:1px solid var(--line2)"><span class="pill a">必填</span><span style="font-size:12px;margin-left:8px">英语水平</span><span class="tiny faint" style="margin-left:auto">无证书信息</span></div>
              <div class="row" style="padding:9px 0;border-bottom:1px solid var(--line2)"><span class="pill">选填</span><span style="font-size:12px;margin-left:8px">政治面貌</span><span class="tiny faint" style="margin-left:auto">事实库无</span></div>
              <div class="row" style="padding:9px 0;border-bottom:1px solid var(--line2)"><span class="pill a">必填</span><span style="font-size:12px;margin-left:8px">身份证号</span><span class="tiny faint" style="margin-left:auto">敏感，不存</span></div>
              <div class="row" style="padding:9px 0"><span class="pill">选填</span><span style="font-size:12px;margin-left:8px">获奖与证书</span><span class="tiny faint" style="margin-left:auto">可手动补充</span></div>
            </div>
            <div class="spacer"></div>
            <button class="btn ghost sm block" style="margin-top:12px">复制缺失清单</button>
          </div>

          <div class="card" style="background:#FFFDF7;border-color:#F1E1BA">
            <div class="row"><span class="dot a"></span><b style="font-size:12px">最后一步永远是你</b></div>
            <div class="tiny" style="color:#8A6A22;line-height:1.75;margin-top:5px">检查所有字段后，在网页里自己点「提交申请」。我们不代提交，也不保存身份证号。</div>
            <button class="btn sm block" style="margin-top:10px;background:#fff;border-color:#EAD8AC;color:#8A6A22" disabled>提交申请（请在网页内操作）</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</section>
`);
