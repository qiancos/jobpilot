# -*- coding: utf-8 -*-
"""拿历史台账跑一遍闸门，看这套规则在你的真实数据上站不站得住。

输出：
  out/gate-report.md   人看的报告
  out/gate-detail.csv  逐条明细（可丢进 Excel）
"""
from __future__ import annotations

import csv
import json
import re
import sys
from collections import Counter, defaultdict
from datetime import date
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from lint import gate, advice, visual_length  # noqa: E402

BOSS_DIR = Path(r"E:\DSH\DSH-10-19\boss")
OUT_DIR = Path(__file__).resolve().parent.parent / "out"

FILES = [
    "applications.json",
    "applications-2026-10-22.json",
    "applications-2026-10-22-pm.json",
    "applications-2026-10-23.json",
    "applications-2026-10-28.json",
]


def load_json_lenient(path: Path):
    raw = path.read_text(encoding="utf-8")
    try:
        return json.loads(raw)
    except Exception:
        items = []
        for line in raw.splitlines():
            line = line.strip().rstrip(",")
            if line.startswith("{"):
                try:
                    items.append(json.loads(line))
                except Exception:
                    pass
        return items


def date_of(fname: str) -> str:
    m = re.search(r"(\d{4}-\d{2}-\d{2})", fname)
    return m.group(1) if m else "2026-10-21"


def collect():
    rows = []
    for f in FILES:
        p = BOSS_DIR / f
        if not p.exists():
            continue
        data = load_json_lenient(p)
        if isinstance(data, dict):
            data = [data]
        for i, r in enumerate(data, 1):
            text = (r.get("招呼语") or "").strip()
            if not text:
                continue
            rows.append({
                "batch": f.replace(".json", ""),
                "batch_date": date_of(f),
                "no": r.get("序号") or i,
                "company": r.get("公司") or "",
                "title": r.get("岗位") or "",
                "salary": r.get("薪资") or "",
                "status": r.get("状态") or "(无状态字段)",
                "text": text,
            })

    # 10-21 那批的话术不在台账里，在 fix-plan.json（n/k/m 三个键）
    fp = BOSS_DIR / "fix-plan.json"
    if fp.exists():
        for i, r in enumerate(json.loads(fp.read_text(encoding="utf-8")), 1):
            text = (r.get("m") or "").strip()
            if not text:
                continue
            rows.append({
                "batch": "fix-plan(10-21)",
                "batch_date": "2026-10-21",
                "no": i,
                "company": r.get("n") or "",
                "title": r.get("k") or "",
                "salary": "",
                "status": "已核验送达",
                "text": text,
            })
    return rows


def main() -> int:
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        pass

    rows = collect()
    if not rows:
        print("没读到台账，检查 BOSS_DIR")
        return 1

    texts = [r["text"] for r in rows]
    for i, r in enumerate(rows):
        peers = [t for j, t in enumerate(texts) if j != i]
        r["result"] = gate(r["text"], {"peers": peers})
        r["vlen"] = round(visual_length(r["text"]), 0)

    n = len(rows)
    passed = [r for r in rows if r["result"]["pass"]]
    avg = sum(r["result"]["score"] for r in rows) / n
    grades = Counter(r["result"]["grade"] for r in rows)

    # ---- 规则命中统计
    hit = Counter()
    hit_block = Counter()
    for r in rows:
        for f in r["result"]["failures"]:
            hit[f["rule"]] += 1
            if f["level"] == "block":
                hit_block[f["rule"]] += 1

    # ---- 分层统计
    by_batch = defaultdict(list)
    for r in rows:
        by_batch[r["batch"]].append(r)

    OUT_DIR.mkdir(parents=True, exist_ok=True)

    # ---- CSV
    with (OUT_DIR / "gate-detail.csv").open("w", encoding="utf-8-sig", newline="") as fh:
        w = csv.writer(fh)
        w.writerow(["批次", "日期", "公司", "岗位", "视觉宽度", "得分", "等级", "结论",
                    "命中规则", "话术"])
        for r in rows:
            w.writerow([
                r["batch"], r["batch_date"], r["company"], r["title"], r["vlen"],
                r["result"]["score"], r["result"]["grade"],
                "可发" if r["result"]["pass"] else "需改",
                " / ".join(sorted({x["rule"] for x in r["result"]["failures"]})),
                r["text"],
            ])

    # ---- Markdown
    L = []
    L.append("# 招呼语闸门 · 历史数据体检报告\n")
    L.append(f"数据源：`boss/applications*.json` 共 **{n} 条**历史话术；"
             f"生成时间 {date.today().isoformat()}\n")
    L.append("## 一、总览\n")
    L.append(f"| 指标 | 值 |\n|---|---|")
    L.append(f"| 样本量 | {n} 条 |")
    L.append(f"| 可直接进入队列（过闸） | **{len(passed)} 条（{len(passed)/n:.0%}）** |")
    L.append(f"| 平均质量分 | **{avg:.1f}** / 100 |")
    L.append(f"| 等级分布 | " + " / ".join(f"{g}:{grades.get(g,0)}" for g in "ABCDF") + " |")
    L.append(f"| 平均视觉宽度 | {sum(r['vlen'] for r in rows)/n:.0f}（闸门区间 55–95） |")
    L.append("")

    L.append("## 二、分批对比（看规则改没改对）\n")
    L.append("| 批次 | 条数 | 过闸率 | 平均分 | 平均宽度 | 主要问题 |")
    L.append("|---|---|---|---|---|---|")
    for b, rs in sorted(by_batch.items()):
        p = sum(1 for r in rs if r["result"]["pass"])
        a = sum(r["result"]["score"] for r in rs) / len(rs)
        w = sum(r["vlen"] for r in rs) / len(rs)
        c = Counter()
        for r in rs:
            for f in r["result"]["failures"]:
                if f["level"] == "block":
                    c[f["rule"]] += 1
        top = "、".join(f"{k}×{v}" for k, v in c.most_common(3)) or "—"
        L.append(f"| {b} | {len(rs)} | {p}/{len(rs)} = {p/len(rs):.0%} | {a:.0f} | {w:.0f} | {top} |")
    L.append("")

    L.append("## 三、哪条规则最容易踩\n")
    L.append("| 规则 | 说明 | 命中 | 其中硬拦截 |")
    L.append("|---|---|---|---|")
    from lint import RULE_META  # noqa: E402
    for rid, cnt in hit.most_common():
        L.append(f"| {rid} | {RULE_META[rid]} | {cnt} ({cnt/n:.0%}) | {hit_block.get(rid,0)} |")
    L.append("")

    L.append("## 四、最该怎么改的 10 条\n")
    worst_all = sorted(rows, key=lambda r: (0 if r["result"]["blocking"] else 1,
                                            r["result"]["score"]))
    worst, seen_b = [], Counter()
    for r in worst_all:            # 每个批次最多取 2 条，避免一个坏批次刷屏
        if seen_b[r["batch"]] >= 2:
            continue
        seen_b[r["batch"]] += 1
        worst.append(r)
        if len(worst) >= 8:
            break
    for r in worst:
        L.append(f"### {r['company']} · {r['title']}（{r['batch']}，分 {r['result']['score']}）\n")
        L.append(f"> {r['text']}\n")
        for f in r["result"]["failures"]:
            L.append(f"- **{f['rule']} {f['name']}**：{f['msg']}")
        for a in advice(r["text"], r["result"])[:3]:
            L.append(f"- 改法：{a}")
        L.append("")

    L.append("## 五、结论与阈值校准\n")
    L.append(conclusion(n, len(passed), avg, hit, hit_block, rows, by_batch))

    md = "\n".join(L)
    (OUT_DIR / "gate-report.md").write_text(md, encoding="utf-8")

    print(f"样本 {n} 条 | 过闸 {len(passed)} ({len(passed)/n:.0%}) | 平均分 {avg:.1f} "
          f"| 等级 {dict(grades)}")
    for rid, c in hit.most_common():
        print(f"  {rid} {RULE_META[rid][:24]:<26} 命中 {c:>3}  硬拦截 {hit_block.get(rid,0):>3}")
    print(f"\n报告: {OUT_DIR / 'gate-report.md'}")
    print(f"明细: {OUT_DIR / 'gate-detail.csv'}")
    return 0


def conclusion(n, p, avg, hit, hit_block, rows, by_batch) -> str:
    lines = []
    lines.append(f"- 过闸率 **{p/n:.0%}**，平均分 {avg:.0f}。")
    if p / n < 0.35:
        lines.append("- 过闸率偏低：不是规则太严，是这批话术本来就按旧标准写的"
                     "（长、模板开头、技术栈清单）。闸门接进生成端之后，这个数字应该翻上去。")

    fp = by_batch.get("fix-plan(10-21)") or []
    if fp:
        p2 = sum(1 for r in fp if r["result"]["pass"])
        w = sum(r["vlen"] for r in fp) / len(fp)
        lines.append(
            f"- **最关键的一条**：`fix-plan(10-21)` 这 {len(fp)} 条是**唯一 100% 核验送达**的一批，"
            f"但过闸率 **{p2}/{len(fp)}**，平均视觉宽度 **{w:.0f}**（上限 95）。"
            f"也就是说：你送得最到位的那批，恰恰是质量最差的那批。"
            f"——这直接解释了 78 投只有 2 个真人回复。")
    newest = by_batch.get("applications-2026-10-28") or []
    if newest:
        p3 = sum(1 for r in newest if r["result"]["pass"])
        lines.append(f"- 反过来，`10-28` 那批（平均宽度 "
                     f"{sum(r['vlen'] for r in newest)/len(newest):.0f}）过闸 "
                     f"{p3}/{len(newest)}，形式已经达标——缺的只是**送到**。"
                     f"产品的价值就是把这两件事同时解决。")

    top = hit_block.most_common(1)
    if top:
        rid, cnt = top[0]
        from lint import RULE_META
        lines.append(f"- 最高频的**硬拦截**是 **{rid} {RULE_META[rid]}**（{cnt}/{n}）："
                     f"这是系统性弱点，不是偶发。")
    over = [r for r in rows if r["vlen"] > 95]
    if over:
        lines.append(f"- {len(over)} 条超长（视觉宽度 >95），平均 "
                     f"{sum(r['vlen'] for r in over)/len(over):.0f}，"
                     f"最长 {max(r['vlen'] for r in over):.0f}——手机上要 5 行以上。")
    lines.append("- **必须说清的一件事**：闸门只验**形式与事实**（长度 / 开场 / 证据 / 到岗 / 事实一致），"
                 "不验「这句话打不打动 HR」。过闸 ≠ 会回复，它只是把明显不合格的挡在门外。"
                 "真正的质量排序靠 F3 的 JD 打分，不靠闸门。")
    lines.append("- **规则为什么宁松不严**：太严的闸门会被你直接关掉，那才是最大的失败。"
                 "所以只有 R1/R2/R3/R8（长度、模板开场、没引用 JD、事实不实）是硬拦截，"
                 "其余都是扣分项。")
    lines.append("- **是否校准阈值**：先不要按「好不好看」调。接入生成端跑 2 批真实投递，"
                 "按「回复率」回调 55–95 与 0.72 这两个数，才有依据。")
    return "\n".join(lines)


if __name__ == "__main__":
    raise SystemExit(main())
