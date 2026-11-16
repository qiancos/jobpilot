# M0-APP 结论：能不能接管 BOSS App？

> 验证时间：2026-11-16 · 设备：vivo PA2573 / Android 16 · BOSS App `com.hpbr.bosszhipin`

---

## ✅ 2026-11-16 15:56 更新：已解决

**结论翻盘：无障碍能完整读到岗位卡片。** 之前读不到是我自己的 bug。

真实探针输出：

```
>> PROBE byId(cl_card_container)=3  byId(view_job_card)=3
   rootPkg=com.hpbr.bosszhipin  rootChildren=1
>> CARD TEXT: 留学咨询顾问（澳洲/新西兰）-双休  15-25K  新东方  已上市  10000人以上
   本科  在校/应届  五险一金  海外留学背景  销售背景
   方女士 · 招聘经理  今日回复10+次  南京 江宁区
```

**两个根因**：

| # | 问题 | 修法 |
|---|---|---|
| 1 | 我的 `walk()` 限制了 `depth > 16` 就返回，而卡片在更深的层级 | 深度放到 40，或直接用 `findAccessibilityNodeInfosByViewId` 定位 |
| 2 | 用 `windows` 遍历拿到的是底部导航那个窗口，不是内容窗口 | 改用 `rootInActiveWindow` + 按 view id 反查 |

**字段全部可读**：岗位名 / 薪资 / 公司 / 规模 / 学历 / 经验 / 福利标签 / HR 姓名职位 / 活跃度 / 地区。

**注意**：vivo 禁止用 `settings put secure` 强开无障碍服务（写入返回成功但读回来是 null）。
必须在「设置 → 无障碍 → 已下载的服务 → JobPilot」手动打开一次（现在已开）。

**Shizuku 那条路放弃**：`uiautomator dump` 在 Shizuku 的 UserService 进程里返回
`null root node returned by UiTestAutomationBridge`，退出码 134。同为 uid 2000 / `u:r:shell:s0`，
但从 App 派生出来的进程跑不了。Shizuku 留着做 `input` 兜底点击仍可用。

---

## 原结论（保留作记录）

**能做，但第一条通道（无障碍）读不到岗位卡片。** 需要换读取方式。

---

## 实测证据

### 通了的

| 项 | 结果 |
|---|---|
| 无障碍服务能启用 | ✅ `settings put secure enabled_accessibility_services` 生效 |
| 能收到 BOSS 的事件 | ✅ `pkg=com.hpbr.bosszhipin event=WIN_CONTENT` |
| 能读到界面外壳 | ✅ 底部导航「职位/发现/消息/我的」、`ivSearch`、`img_more` |
| 能拿到控件 id | ✅ `com.hpbr.bosszhipin:id/tv_tab_1` 这种 |

### 没通的（关键）

**岗位卡片一个字都读不到。** 我们的服务只拿到这些：

```
ViewGroup | id=- | click=1 | t= | d=
ViewGroup | id=- | click=1 | t= | d=
TextView  | id=com.hpbr.bosszhipin:id/tv_tab_1 | click=0 | t=职位
```

而同一时刻，`uiautomator dump`（shell 通道）拿到的是：

```
总节点 277 个，88 个带文字
TextView text='实施运维实习生-双休'
TextView text='110-120元/天'
TextView text='光锥智能'
TextView text='吴女士 · HR'
TextView text='广州  海珠区  赤岗'
...
```

**所以内容确实在控件树里，只是我们的无障碍服务拿不到。**

### 顺手搞清了 BOSS App 的卡片结构（来自 uiautomator）

```
ViewGroup  id=cl_card_container          ← 卡片容器
└ FrameLayout id=boss_job_card_view
  └ ViewGroup id=view_job_card           ← 真正的一张卡
      ├ TextView  岗位名
      ├ TextView  薪资
      ├ TextView  公司
      ├ TextView  标签
      └ TextView  HR + 活跃度 + 地区
```

**是原生 View（RecyclerView + TextView），不是 WebView、不是 Flutter**。这很重要——意味着只要拿到树，就能精确定位，不用 OCR。

---

## 为什么读不到：两种可能

| 可能 | 说明 | 能不能解 |
|---|---|---|
| **① 我的服务配置不对** | XML 里的 `canRetrieveWindowContent` / flags 没完全生效，或事件时机太早（在窗口合成前 dump） | 大概率能，需要再花半天试「定时 dump + 事件 source」 |
| **② BOSS 对第三方无障碍做了降级** | 有些 App 检测到非系统无障碍服务时，返回裁剪过的树 | 难，只能换通道 |

**现在无法区分。** 但 `uiautomator` 能读全，说明系统层面数据是完整的。

---

## 三条可选通道

| 通道 | 原理 | 能否读全 | 门槛 | 风险 |
|---|---|---|---|---|
| **A. 无障碍服务**（现在） | AccessibilityService 读 window root | ❓ 待定 | 用户手动开一次 | 可能被 App 降级 |
| **B. Shizuku**（推荐） | 拿到 shell 权限，跑 `uiautomator dump` + `input tap` | ✅ **已验证能读全** | 装 Shizuku + 无线调试配对一次，**重启后可能要重连** | 低，但多一个 App 依赖 |
| **C. 截图 + OCR** | 截屏后识别文字/找图标 | ✅ 一定行 | 无 | 慢（1-3 秒/屏）、耗电、点击靠坐标 |

**我的建议：B 为主，A 为辅。**

理由：B 已经用 `uiautomator dump` 实测验证过（277 节点全在），而 A 还是未知数。用 B 的话，今天验证成功的读取方式可以直接变成产品代码，不用赌。

C 作为最后兜底（Shizuku 掉了也能降级跑，只是慢）。

---

## 对产品的影响（如果走 B）

| 变化 | 说明 |
|---|---|
| **不再需要 WebView 采** | 采集直接读 BOSS App 的列表，岗位信息更全（薪资/HR/活跃度都有） |
| **不需要登录** | 你平板上 BOSS App 已经登录，天然复用 |
| **执行动作变了** | 从「DOM 点击」变成「按控件 id 找节点 → performAction(CLICK)」或坐标点击 |
| **悬浮面板直接可用** | 已经建好，浮在 BOSS App 上就能当控制台 |
| **网页版降级保留** | 万一 App 通道被封，退回网页版（已跑通） |
| **短板** | 依赖 Shizuku；App 改版会改变控件 id；需要屏幕常亮 |

---

## 下一步需要你确认

1. **愿不愿意装 Shizuku？** 需要：装 App → 开开发者选项 → 无线调试配对（一次性，5 分钟）。平板重启后可能要重新点一下。
2. 还是先让我再花半天，把无障碍服务调到能读全（不装额外 App，但可能白费）？

**两条路我都能走。选 B 见效快、确定性强；选 A 更干净、但结果未知。**
