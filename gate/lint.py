# -*- coding: utf-8 -*-
"""招呼语质量闸门（9 条硬规则 + 质量分）。

用法：
    python lint.py "要检查的话术文本"
    python lint.py --file some.txt

设计原则：
1. 每条规则都能说出「为什么不过」，不给模糊结论。
2. 闸门宁可错杀模板话，也不放过"看着像人写的群发"。
3. 规则只管形式与事实，不评价"写得好不好"——那是人的事。
"""
from __future__ import annotations

import json
import re
import sys

from lexicon import (
    ALIASES, TECH_TERMS, DOMAIN_WORDS, FORBIDDEN_CLAIMS,
    BAD_OPENINGS, AVAILABILITY_PAT, PROOF_PAT, ENDING_PAT,
)

# ------------------------------------------------------------------ 基础工具

_PUNCT = "，。！？、；：\u201c\u201d\u2018\u2019（）()【】《》〈〉, .!?;:~—…·-+/\\|"


def normalize(text: str) -> str:
    """去标点、空白、转小写，用于相似度与字数计算。"""
    return "".join(ch for ch in text.lower() if ch not in _PUNCT and not ch.isspace())


def char_count(text: str) -> int:
    """纯字符数（去标点空白），仅作参考。"""
    return len(normalize(text))


_CJK = re.compile(r"[\u4e00-\u9fff]")
_ALNUM = re.compile(r"[A-Za-z0-9]")


def visual_length(text: str) -> float:
    """手机上的"视觉宽度"：一个汉字算 1，一个英文字母/数字算 0.5。

    为什么不用纯字符数：`Spring Boot` 是 11 个字符，但只占 5.5 个汉字的宽度。
    用纯字符数会把"技术这么写很正常"的话误判成"太长"。
    """
    cjk = len(_CJK.findall(text))
    alnum = len(_ALNUM.findall(text))
    return cjk + alnum * 0.5


def split_sentences(text: str) -> list[str]:
    parts = re.findall(r"[^。！？!?\n]+[。！？!?]?", text)
    return [p.strip() for p in parts if p.strip()]


def ngrams(text: str, n: int = 3) -> set[str]:
    s = normalize(text)
    if len(s) < n:
        return {s} if s else set()
    return {s[i:i + n] for i in range(len(s) - n + 1)}


def jaccard(a: set[str], b: set[str]) -> float:
    if not a or not b:
        return 0.0
    return len(a & b) / len(a | b)


def extract_tech_terms(text: str) -> dict[str, str]:
    """返回 {规范名: 原文命中片段}，同一技术多种叫法只算一次。

    必须「最长匹配后挖空」，否则 SpringBoot 会被重复算成 Spring + Spring Boot。
    """
    found: dict[str, str] = {}
    work = text
    for key in sorted(ALIASES, key=len, reverse=True):
        if key in work:
            found.setdefault(ALIASES[key], key)
            work = work.replace(key, "\u0000" * len(key))
    return found


def domain_hits(text: str) -> list[str]:
    return [w for w in DOMAIN_WORDS if w in text]


# ------------------------------------------------------------------ 规则实现

def rule1_length(text: str):
    n = visual_length(text)
    raw = char_count(text)
    sents = split_sentences(text)
    fails = []
    if n < 55:
        fails.append(("R1", f"太短（视觉宽度 {n:.0f}，下限 55），信息量不够，HR 判断不了你适不适合"))
    if n > 95:
        fails.append(("R1", f"太长（视觉宽度 {n:.0f}，上限 95），手机上 4 行以上 HR 不会读完"))
    if len(sents) > 3:
        fails.append(("R1", f"{len(sents)} 句，超过 3 句（第 4 句开始读者会跳）"))
    return fails, {"vlen": round(n, 1), "chars": raw, "sentences": len(sents)}


def rule2_opening(text: str):
    head = text.lstrip()
    for pat, why in BAD_OPENINGS:
        if re.search(pat, head[:24]):
            return [("R2", why)], {"opening": head[:12]}
    return [], {"opening": head[:12]}


def rule3_jd_quote(text: str, ctx: dict):
    """必须引用 JD 里的具体词，且这个词在你的能力范围内。"""
    jd = (ctx.get("jd_text") or "").strip()
    techs = extract_tech_terms(text)
    doms = domain_hits(text)

    if jd:
        jd_tech = extract_tech_terms(jd)
        jd_dom = set(domain_hits(jd))
        hit_tech = set(techs) & set(jd_tech)
        hit_dom = set(doms) & jd_dom
        if not hit_tech and not hit_dom:
            return ([("R3", "没有一个词来自这份 JD——HR 一眼能看出是通用模板")],
                    {"jd_hit": []})
        return [], {"jd_hit": sorted(hit_tech | hit_dom)}

    # 没有 JD 文本时退化为"至少一个技能词 + 一个业务词"
    if not techs and not doms:
        return [("R3", "既没有技术词也没有业务场景词，看不出你读过岗")], {"jd_hit": []}
    return [], {"jd_hit": sorted(list(techs)[:3] + doms[:2])}


def rule4_proof(text: str):
    for pat in PROOF_PAT:
        m = re.search(pat, text)
        if m:
            return [], {"proof": m.group(0)}
    return [("R4", "没有任何带数字/结果的证据，全是形容词")], {"proof": ""}


def rule5_availability(text: str):
    for pat in AVAILABILITY_PAT:
        m = re.search(pat, text)
        if m:
            return [], {"availability": m.group(0)}
    return [("R5", "没写可到岗时间——HR 最关心「这个月能不能来」")], {"availability": ""}


TECH_FAMILY = {
    "Spring": "Spring 家族", "Spring Boot": "Spring 家族", "Spring MVC": "Spring 家族",
    "Spring Cloud": "Spring 家族", "MyBatis": "MyBatis 家族", "MyBatis-Plus": "MyBatis 家族",
    "MySQL": "SQL 数据库", "PostgreSQL": "SQL 数据库", "Oracle": "SQL 数据库",
    "Vue": "前端", "Element Plus": "前端", "uni-app": "前端", "微信支付": "前端",
    "LangChain": "AI 框架", "LangGraph": "AI 框架", "RAG": "AI 框架", "Agent": "AI 框架",
    "Function Call": "AI 框架", "向量检索": "AI 框架", "大模型": "AI 框架",
    "LLM": "AI 框架", "Prompt": "AI 框架",
}


def tech_families(text: str) -> list[str]:
    """把同族技术合并（Spring / Spring Boot / Spring MVC 算一个），
    因为要防的是「技术栈清单」，不是「提到 5 个词」。"""
    fam = []
    for canon in extract_tech_terms(text):
        f = TECH_FAMILY.get(canon, canon)
        if f not in fam:
            fam.append(f)
    return fam


def rule6_tech_stack(text: str):
    fam = tech_families(text)
    n = len(fam)
    if n > 4:
        return ([("R6", f"技术栈堆了 {n} 类：{'、'.join(fam[:6])}（上限 4 类）")],
                {"tech_count": n, "techs": fam})
    return [], {"tech_count": n, "techs": fam}


def rule7_similarity(text: str, ctx: dict):
    """相似度分两级：
    ≥0.90 → 几乎一字不差，平台侧可能判重复内容，硬拦截；
    0.72–0.90 → 读起来像套模板，软提醒（不同 HR 互相看不到，不必一票否决）。
    """
    peers = ctx.get("peers") or []
    if not peers:
        return [], {"max_sim": 0.0, "nearest": ""}
    mine = ngrams(text)
    best, who = 0.0, ""
    for p in peers:
        s = jaccard(mine, ngrams(p))
        if s > best:
            best, who = s, p
    fails = []
    if best >= 0.90:
        fails.append(("R7", f"和另一条话术 {best:.2f} 几乎一字不差——平台侧可能判重复内容", "block"))
    elif best >= 0.72:
        fails.append(("R7", f"和另一条话术相似度 {best:.2f}，读起来像套模板", "soft"))
    head = normalize(text)[:8]
    if head and len(head) >= 6:
        same = [p for p in peers if normalize(p)[:8] == head]
        if same:
            fails.append(("R7", f"开头与另外 {len(same)} 条完全相同（建议换句式）", "soft"))
    return fails, {"max_sim": round(best, 3), "nearest": who[:30]}


def rule8_facts(text: str):
    fails = []
    for pat, why in FORBIDDEN_CLAIMS:
        if re.search(pat, text):
            fails.append(("R8", f"事实不实：{why}"))
    return fails, {}


def rule9_ending(text: str):
    tail = text.rstrip().rstrip("\u3002")
    for pat in ENDING_PAT:
        if re.search(pat, tail):
            return [], {"ending": tail[-12:]}
    return [("R9", "结尾没有留给对方回应的话口（建议收在「方便发您简历吗」）")], {
        "ending": tail[-12:]}


# ------------------------------------------------------------------ 汇总

# 硬拦截：命中任何一条，这句话不许进队列
BLOCKING = {"R1", "R2", "R3", "R8"}
# 软扣分：不拦，但拉低质量分（人说"这句还行"的时候，这些是提分项）
# 注：R7 是动态的——相似度 ≥0.90 时由规则自己升级成 block
SOFT = {"R4", "R5", "R6", "R7", "R9"}


RULE_META = {
    "R1": "长度与句数（55–95 字 / ≤3 句）",
    "R2": "开场不能是模板（禁「您好我是」「我是27届」）",
    "R3": "必须引用 JD 里的具体词",
    "R4": "必须有带数字的结果或证据",
    "R5": "必须写到岗时间",
    "R6": "技术栈不超过 4 类（防堆清单）",
    "R7": "与近期话术不雷同（≥0.90 拦 / 0.72 提醒）",
    "R8": "不得出现事实库之外的说法",
    "R9": "结尾收在邀约/问句上",
}

RULE_RULES = [
    rule1_length, rule2_opening, rule3_jd_quote, rule4_proof, rule5_availability,
    rule6_tech_stack, rule7_similarity, rule8_facts, rule9_ending,
]


def gate(text: str, ctx: dict | None = None) -> dict:
    ctx = ctx or {}
    failures, stats = [], {}
    for fn in RULE_RULES:
        if fn.__code__.co_argcount == 2:
            f, s = fn(text, ctx)
        else:
            f, s = fn(text)
        failures.extend(f)
        stats.update(s)

    # ---- 质量分（0–100）：个性化 40 / 对口 30 / 可信 20 / 简洁 10
    jd_hit = stats.get("jd_hit") or []
    techs = stats.get("techs") or []
    personal = 0
    personal += 20 if jd_hit else 0
    personal += 10 if stats.get("proof") else 0
    personal += 10 if not any(f[0] == "R2" for f in failures) else 0

    jd = (ctx.get("jd_text") or "").strip()
    if jd:
        jd_fam = set(tech_families(jd))
        mine = set(techs)
        cover = len(mine & jd_fam) / max(1, min(len(jd_fam), 6))
        fit = round(30 * min(1.0, cover + 0.3))
    else:
        # 没有 JD 就判断不了"对口"，顶多按技术栈覆盖给个近似值；
        # 产品/运营岗没有技术词，则看业务场景词
        nf = len(techs)
        if nf >= 3:
            fit = 30
        elif nf >= 1:
            fit = 24
        elif stats.get("jd_hit"):
            fit = 18
        else:
            fit = 0

    credible = 0
    credible += 10 if stats.get("proof") else 0
    credible += 10 if not any(f[0] == "R8" for f in failures) else 0

    concise = 0
    concise += 6 if 55 <= stats.get("vlen", 0) <= 95 else 0
    concise += 4 if stats.get("tech_count", 0) <= 4 else 0

    score = personal + fit + credible + concise
    score = max(0, min(100, score))
    grade = ("A" if score >= 88 else "B" if score >= 75
             else "C" if score >= 60 else "D")

    entries = []
    for item in failures:
        rid, msg = item[0], item[1]
        lv = item[2] if len(item) > 2 and item[2] else (
            "block" if rid in BLOCKING else "soft")
        entries.append({"rule": rid, "name": RULE_META[rid], "msg": msg, "level": lv})
    blocking = [e for e in entries if e["level"] == "block"]
    soft = [e for e in entries if e["level"] == "soft"]

    if blocking:          # 硬拦截项不过，质量分不得高于 55，避免"F 级但 80 分"的自相矛盾
        grade = "F"
        score = min(score, 55)

    return {
        # 能进队列的条件：无硬拦截 且 质量分 ≥ 65。
        # 阈值刻意低于 70：闸门太严会被用户整个关掉，那才是最大的失败。
        "pass": (not blocking) and score >= 65,
        "score": score,
        "grade": grade,
        "blocking": blocking,
        "soft": soft,
        "failures": blocking + soft,
        "stats": stats,
    }


def advice(text: str, result: dict) -> list[str]:
    """按失败规则给出可执行的改法，而不是"再改改"。"""
    out = []
    st = result["stats"]
    rules = [f["rule"] for f in result["failures"]]
    if "R1" in rules:
        if st.get("vlen", 0) > 95:
            out.append(f"砍到 90 以内：现在视觉宽度 {st['vlen']:.0f}，删掉第 2 句里罗列的技术栈")
        elif st.get("vlen", 0) < 55:
            out.append("补一句证据：把某个项目的「做了什么 + 结果数字」塞进第一句")
    if "R2" in rules:
        out.append('换个开头：直接用「你们这个岗要…」「JD 里提到…」切入，别自报家门')
    if "R3" in rules:
        out.append("从 JD 里挑一个具体的词（技术或业务），用它当第一句的抓手")
    if "R4" in rules:
        out.append("加数字：解决率 85% / 每周 5 天 / 6 个月 / 从 0 到 1，任选一个真实结果")
    if "R5" in rules:
        out.append('结尾前补一句到岗信息：「可每周 5 天，随时到岗」')
    if "R6" in rules:
        out.append(f"技术栈砍到 4 类以内（现在 {st.get('tech_count')} 类），留最贴 JD 的")
    if "R7" in rules:
        out.append("换开头和第一句的证据——这句已经和另一条撞了")
    if "R8" in rules:
        out.append("删掉没有依据的说法，只留事实库里的经历")
    if "R9" in rules:
        out.append('结尾加一句「方便发您一份简历吗？」')
    return out


# ------------------------------------------------------------------ CLI

def main(argv: list[str]) -> int:
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        pass
    if len(argv) < 2:
        print("用法: python lint.py \"话术文本\"  或  python lint.py --file x.txt")
        return 2
    if argv[1] == "--file":
        text = open(argv[2], encoding="utf-8").read().strip()
    else:
        text = " ".join(argv[1:])
    r = gate(text)
    print(json.dumps({"pass": r["pass"], "score": r["score"], "grade": r["grade"],
                      "failures": r["failures"], "advice": advice(text, r)},
                     ensure_ascii=False, indent=2))
    return 0 if r["pass"] else 1


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
