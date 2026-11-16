/* JobPilot 原型交互：导航 / 视图切换 / 截图模式 / 小交互 */
(function () {
  const ICONS = {
    cockpit: '<rect x="3" y="3" width="7.5" height="7.5" rx="1.6"/><rect x="13.5" y="3" width="7.5" height="7.5" rx="1.6"/><rect x="3" y="13.5" width="7.5" height="7.5" rx="1.6"/><rect x="13.5" y="13.5" width="7.5" height="7.5" rx="1.6"/>',
    target: '<circle cx="12" cy="12" r="8.5"/><circle cx="12" cy="12" r="4.5"/><circle cx="12" cy="12" r="1"/>',
    chat: '<path d="M20.5 12.2c0 4-3.8 7.2-8.5 7.2-1 0-2-.14-2.9-.4L4.5 20.5l1.2-3.5c-1.3-1.3-2.2-3-2.2-4.8 0-4 3.8-7.2 8.5-7.2s8.5 3.2 8.5 7.2z"/>',
    play: '<circle cx="12" cy="12" r="8.5"/><path d="M10.2 8.6l5.2 3.4-5.2 3.4z"/>',
    book: '<path d="M5 4.8h9.2a2 2 0 012 2v12.4H7a2 2 0 01-2-2z"/><path d="M16.2 18.9h2.8V7.6a2 2 0 00-2-2h-.8"/><path d="M8 8.5h6M8 12h6"/>',
    users: '<circle cx="9.2" cy="8.2" r="3.2"/><path d="M3.5 19.2c.5-3.2 2.8-5 5.7-5s5.2 1.8 5.7 5"/><path d="M16 5.2a3 3 0 010 5.6M17.2 14.4c1.8.6 3 2.2 3.3 4.4"/>',
    file: '<path d="M13.4 3.5H7a2 2 0 00-2 2v13a2 2 0 002 2h10a2 2 0 002-2V9.1z"/><path d="M13.4 3.5v5.6H19"/><path d="M9 13h6M9 16.5h4"/>',
    form: '<rect x="4.5" y="3.5" width="15" height="17" rx="2"/><path d="M8 8h8M8 12h8M8 16h4"/>',
    gear: '<circle cx="12" cy="12" r="3.2"/><path d="M19.4 13.6a7.6 7.6 0 000-3.2l2-1.4-2-3.5-2.3.9a7.4 7.4 0 00-2.7-1.6L14 2.5h-4l-.4 2.3A7.4 7.4 0 006.9 6.4L4.6 5.5l-2 3.5 2 1.4a7.6 7.6 0 000 3.2l-2 1.4 2 3.5 2.3-.9a7.4 7.4 0 002.7 1.6l.4 2.3h4l.4-2.3a7.4 7.4 0 002.7-1.6l2.3.9 2-3.5z"/>',
    spark: '<path d="M12 3l1.7 5.3L19 10l-5.3 1.7L12 17l-1.7-5.3L5 10l5.3-1.7z"/>',
    arrow: '<path d="M5 12h14M13 6l6 6-6 6"/>',
    check: '<path d="M4 12.5l5 5L20 6.5"/>'
  };
  const NAV = [
    ['s1', 'cockpit', '驾驶舱'], ['s3', 'target', '待投'], ['s5', 'chat', '话术'],
    ['s8', 'play', '执行'], ['s10', 'book', '台账'], ['s11', 'users', '会话'],
    ['s12', 'file', '简历'], ['s13', 'form', '网申'], ['s14', 'gear', '设置']
  ];
  const MODULES = {
    m1: { name: 'M1 今日驾驶舱', desc: '把「今天该做什么」压缩成一条流水线：采集 → 审阅 → 执行 → 核验，3 秒内知道下一步点哪。', screens: ['s1'] },
    m2: { name: 'M2 采集与待投', desc: '原生控件树只读采集，规则先淘汰；清单里 30 秒勾出 8 家，详情页用打分卡与三件套解释「值不值得投」。', screens: ['s2', 's3', 's4'] },
    m3: { name: 'M3 话术与闸门', desc: '双版本起稿 + 9 条质量闸门实时校验；不过闸时给逐条解释与可编辑的修复建议，而不是一个红叉。', screens: ['s5', 's6'] },
    m4: { name: 'M4 批次执行', desc: '全流程唯一的人工闸口：确认 8 家 → BOSS 前台执行（悬浮面板是唯一入口）→ L1/L2/L3 三态核验。', screens: ['s7', 's8', 's9'] },
    m5: { name: 'M5 台账与会话', desc: '漏斗回答「哪一步漏了」，会话管家回答「HR 说了什么、我该回什么」，所有数据留在本地。', screens: ['s10', 's11'] },
    m6: { name: 'M6 简历与网申', desc: '3 个方向版本替代逐岗定制；北森 / Moka 表单只代填、不提交，缺失项高亮不猜。', screens: ['s12', 's13'] },
    m7: { name: 'M7 设置与浮层', desc: '档位、配额、无障碍自检、选择器与事实库版本；悬浮面板的展开 / 折叠 / 熔断三种形态。', screens: ['s14'] }
  };

  const $ = (s, r = document) => r.querySelector(s);
  const $$ = (s, r = document) => Array.from(r.querySelectorAll(s));

  // 注入左侧导航
  $$('.rail-slot').forEach(slot => {
    const active = slot.dataset.active;
    slot.outerHTML = '<aside class="rail"><div class="logo">JP</div>' +
      NAV.map(([id, ico, label]) =>
        '<div class="item ' + (id === active ? 'on' : '') + '" data-goto="' + id + '" title="' + label + '">' +
        '<svg viewBox="0 0 24 24">' + ICONS[ico] + '</svg><span>' + label + '</span></div>').join('') +
      '<div class="sp"></div><div class="ava">张</div></aside>';
  });

  // 屏幕元数据
  const shots = $$('.shot');
  const meta = shots.map((el, i) => ({
    id: el.dataset.id || ('s' + (i + 1)),
    module: el.dataset.module,
    title: el.dataset.title || '',
    desc: el.dataset.desc || '',
    el
  }));
  const byId = {};
  meta.forEach(m => byId[m.id] = m);

  const params = new URLSearchParams(location.search);
  const capture = params.get('capture') === '1';
  const mod = params.get('module');
  const view = params.get('view');

  if (capture) {
    document.body.classList.add('capture');
    const active = mod && MODULES[mod] ? mod : 'm1';
    const info = MODULES[active];
    const head = document.createElement('div');
    head.className = 'modhead';
    head.innerHTML = '<div class="k">JOBPILOT · TABLET 1440×960</div><h1>' + info.name + '</h1><p>' + info.desc + '</p>';
    document.body.insertBefore(head, $('#board'));
    shots.forEach(s => {
      s.classList.toggle('on', s.dataset.module === active);
    });
    return;
  }

  // 工具栏
  const tb = document.createElement('div');
  tb.id = 'toolbar';
  tb.innerHTML =
    '<div class="brand"><div class="mark">JP</div>投递驾驶舱 <span>平板端高保真 · v1.0</span></div>' +
    '<div class="mods"></div><div class="sp"></div>' +
    '<div class="hint" id="tbTitle"></div>' +
    '<div class="viewbtns"><button data-view="view">单屏</button><button data-view="overview">总览</button></div>' +
    '<div class="nav"><button id="prev" title="上一屏">‹</button><button id="next" title="下一屏">›</button></div>';
  document.body.insertBefore(tb, $('#board'));

  const mods = $('.mods', tb);
  mods.innerHTML = Object.entries(MODULES).map(([k, v]) =>
    '<button data-mod="' + k + '">' + v.name.replace(/^M\d /, '') + '</button>').join('');

  // 初始化视图
  let mode = view === 'overview' ? 'overview' : 'view';
  let cur = params.get('screen') && byId[params.get('screen')] ? params.get('screen') : 's1';

  function apply() {
    document.body.classList.toggle('view', mode === 'view');
    document.body.classList.toggle('overview', mode === 'overview');
    shots.forEach(s => s.classList.toggle('active', s.dataset.id === cur));
    $$('.shot .cap').forEach(c => c.style.display = '');
    $$('#toolbar .viewbtns button').forEach(b => b.classList.toggle('on', b.dataset.view === mode));
    $$('#toolbar .mods button').forEach(b => {
      b.classList.toggle('on', byId[cur] && byId[cur].module === b.dataset.mod);
    });
    $('#tbTitle').textContent = byId[cur] ? (byId[cur].module.toUpperCase() + ' · ' + byId[cur].title) : '';
    if (mode === 'view') fit();
  }
  function fit() {
    const s = Math.min((innerWidth - 56) / 1440, (innerHeight - 58 - 24) / 960, 1);
    document.body.style.setProperty('--s', s.toFixed(3));
  }
  function go(id) {
    if (!byId[id]) return;
    cur = id; mode = 'view'; apply();
  }
  function step(d) {
    const list = meta.map(m => m.id);
    let i = list.indexOf(cur) + d;
    if (i < 0) i = list.length - 1;
    if (i >= list.length) i = 0;
    go(list[i]);
  }

  window.addEventListener('resize', fit);
  $$('#toolbar .viewbtns button').forEach(b => b.onclick = () => { mode = b.dataset.view; apply(); });
  $$('#toolbar .mods button').forEach(b => b.onclick = () => {
    const first = meta.find(m => m.module === b.dataset.mod);
    if (first) go(first.id);
  });
  $('#prev').onclick = () => step(-1);
  $('#next').onclick = () => step(1);

  document.addEventListener('click', e => {
    const nav = e.target.closest('[data-goto]');
    if (nav) { go(nav.dataset.goto); return; }
    const tab = e.target.closest('[data-tab]');
    if (tab) {
      const box = tab.closest('.tabbox');
      $$('[data-tab]', box).forEach(t => {
        const on = t === tab;
        t.classList.toggle('on', on);
        t.style.background = on ? '#fff' : 'transparent';
        t.style.boxShadow = on ? '0 1px 3px rgba(16,24,40,.1)' : 'none';
        t.style.color = on ? 'var(--ink)' : 'var(--sub)';
        t.style.border = '0';
      });
      const v = tab.dataset.tab;
      $$('[data-variant]', box).forEach(x => x.style.display = x.dataset.variant === v ? '' : 'none');
      return;
    }
    const cb = e.target.closest('.cb');
    if (cb && !cb.dataset.static) { cb.classList.toggle('on'); return; }
  });

  $$('.shot').forEach(s => {
    const cap = s.querySelector('.cap');
    if (cap) {
      const b = cap.querySelector('b');
      const id = s.dataset.id;
      if (b) b.textContent = id + ' · ' + (s.dataset.title || b.textContent);
    }
  });

  // 闸门「一键修复」示意
  document.addEventListener('click', e => {
    if (e.target.closest('#fixAll')) {
      const sc = $('#fixScore b'); if (sc) sc.textContent = '89';
      $$('#gateRules .rule').forEach(r => {
        r.classList.remove('bad');
        const st = r.querySelector('.st'); if (st) { st.textContent = '通过'; st.className = 'st ok'; }
      });
      const n = $('#fixNote'); if (n) n.textContent = '已按建议改写：补数字、改问句、降低与 10-23 批的相似度。请再读一遍再收进队列。';
      const b = $('#fixAll'); if (b) { b.disabled = true; b.textContent = '已应用'; }
    }
    if (e.target.closest('#agree')) {
      const row = e.target.closest('#agree');
      const cb = row.querySelector('.cb');
      const btn = $('#goBtn');
      if (btn && cb) btn.disabled = !cb.classList.contains('on');
    }
  });

  apply();
})();
