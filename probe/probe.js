/* BOSS 直聘网页版 · DOM 自检脚本
 * ---------------------------------------------------------------
 * 用途：验证你 WORKFLOW 里那套选择器还有没有效（防改版）。
 * 用法：在已登录的 BOSS 网页版对应页面上，打开 DevTools Console，整段粘贴回车。
 * 输出：一份可直接复制的 JSON 摘要。
 *
 * 注意：只读，不点任何按钮、不发任何消息。
 */
(() => {
  const P = location.pathname;
  const pageType = P.includes('/web/geek/jobs') ? 'jobs'
    : P.includes('job_detail') ? 'detail'
    : P.includes('/chat') ? 'chat'
    : 'unknown';

  const test = (name, sels) => {
    const out = [];
    for (const sel of sels) {
      let nodes = [];
      try { nodes = [...document.querySelectorAll(sel)]; } catch (e) { continue; }
      out.push({
        selector: sel,
        count: nodes.length,
        sample: nodes.length ? (nodes[0].innerText || '').replace(/\s+/g, ' ').slice(0, 40) : '',
      });
    }
    const best = out.filter(x => x.count > 0).sort((a, b) => b.count - a.count)[0] || null;
    return { name, ok: !!best, best: best ? best.selector : null, candidates: out };
  };

  const report = {
    url: location.href,
    pageType,
    webdriver: navigator.webdriver,
    ua: navigator.userAgent,
    time: new Date().toISOString(),
    checks: [],
  };

  if (pageType === 'jobs') {
    report.checks = [
      test('岗位卡片', ['li.job-card-box', '.job-card-wrapper', 'li.job-card-wrapper']),
      test('职位名', ['a.job-name', '.job-name', '.job-card-left .job-name']),
      test('薪资', ['.job-salary', '.salary', '.red']),
      test('技术标签', ['.tag-list', '.job-card-footer .tag-list']),
      test('详情链接', ['a.job-name[href*="job_detail"]', 'a[href*="job_detail"]']),
      test('右侧详情面板', ['div.job-card-wrap.active', '.job-card-wrap']),
      test('搜索框', ['.search-input input', 'input[placeholder*="搜索"]', '.search-input-box input']),
    ];
  } else if (pageType === 'detail') {
    report.checks = [
      test('立即沟通按钮', ['a.btn-startchat', '.btn-startchat', 'a[href*="chat"]', '.op-btn-chat']),
      test('职位名', ['.name h1', '.job-banner .name', '.info-primary .name']),
      test('公司名', ['.company-info .name', '.sider-company .name', '.company-name']),
      test('JD 正文', ['.job-sec-text', '.job-detail-section .text', '.job-detail-section']),
      test('HR 活跃度', ['.boss-active-time', '.job-boss-info .name', '.boss-info-attr']),
    ];
  } else if (pageType === 'chat') {
    const items = [...document.querySelectorAll('.user-list li')];
    const selfs = [...document.querySelectorAll('.item-myself')];
    const input = document.querySelector('[contenteditable="true"]');
    const sendBtn = document.querySelector('.send-message, .btn-send, [class*="send"]');
    report.checks = [
      test('会话列表项', ['.user-list li', '.user-list .friend-content']),
      test('顶部会话头', ['.chat-header', '.chat-title', '.user-info', '.chat-top']),
      test('自己发的消息', ['.item-myself', '.message-myself', '.item.item-myself']),
      test('对方消息', ['.item-friend', '.message-friend']),
      test('输入框', ['[contenteditable="true"]', '.chat-input [contenteditable]', 'textarea']),
      test('发送按钮', ['.send-message', '.btn-send', '.chat-op .send']),
    ];
    report.chatDebug = {
      conversationCount: items.length,
      selfMessageCount: selfs.length,
      inputPresent: !!input,
      sendButtonDisabled: sendBtn ? (sendBtn.disabled === true || sendBtn.classList.contains('disabled')) : null,
      lastSelfMessage: selfs.length ? selfs[selfs.length - 1].innerText.replace(/\s+/g, ' ').slice(0, 60) : '',
    };
    // 模拟一次 Enter，只派发事件不真的发（用来验证事件通道是否可用）
    report.enterDispatchOK = false;
    if (input) {
      try {
        const ev = () => new KeyboardEvent('keydown', {
          key: 'Enter', code: 'Enter', keyCode: 13, which: 13, bubbles: true, cancelable: true,
        });
        input.dispatchEvent(ev());
        report.enterDispatchOK = true;
      } catch (e) { report.enterDispatchOK = false; }
    }
  }

  const okCount = report.checks.filter(c => c.ok).length;
  report.summary = { total: report.checks.length, ok: okCount, failed: report.checks.filter(c => !c.ok).map(c => c.name) };

  console.log('%cBOSS 自检结果', 'color:#fff;background:#1677ff;padding:2px 6px;border-radius:3px');
  console.table(report.checks.map(c => ({ 检查项: c.name, 命中: c.ok, 选择器: c.best })));
  console.log(JSON.stringify(report, null, 2));
  console.log('↑ 上面这段 JSON 复制给我，我据此改 selectors.json');
  return report.summary;
})();
