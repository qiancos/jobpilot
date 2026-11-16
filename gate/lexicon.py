# -*- coding: utf-8 -*-
"""词表与事实库：闸门的判据来源。

原则：闸门只认这里写死的东西。简历里没有的经历，AI 不许说；
这里没有的技能词，不算"对口"。
"""

import json
from pathlib import Path

# ---------------------------------------------------------------- 技能词表
# 同一技术的不同叫法，归并成一个规范名，避免重复计数
ALIASES = {
    "SpringBoot": "Spring Boot",
    "Spring Boot": "Spring Boot",
    "Spring": "Spring",
    "SpringMVC": "Spring MVC",
    "MyBatis-Plus": "MyBatis-Plus",
    "MyBatisPlus": "MyBatis-Plus",
    "MyBatis": "MyBatis",
    "MySQL": "MySQL",
    "Redis": "Redis",
    "Vue3": "Vue",
    "Vue2": "Vue",
    "Vue": "Vue",
    "uni-app": "uni-app",
    "uniapp": "uni-app",
    "微信支付": "微信支付",
    "OSS": "OSS",
    "PostgreSQL": "PostgreSQL",
    "Oracle": "Oracle",
    "Nginx": "Nginx",
    "SpringCloud": "Spring Cloud",
    "Spring Cloud": "Spring Cloud",
    "Docker": "Docker",
    "Linux": "Linux",
    "JWT": "JWT",
    "WebSocket": "WebSocket",
    "Python": "Python",
    "FastAPI": "FastAPI",
    "LangChain": "LangChain",
    "LangGraph": "LangGraph",
    "RAG": "RAG",
    "Agent": "Agent",
    "Function Call": "Function Call",
    "FunctionCall": "Function Call",
    "向量检索": "向量检索",
    "向量库": "向量检索",
    "大模型": "大模型",
    "LLM": "LLM",
    "Prompt": "Prompt",
    "Element Plus": "Element Plus",
    "ElementPlus": "Element Plus",
    "若依": "若依",
    "Java": "Java",
}

# 按长度倒序，保证 "Spring Boot" 先于 "Spring" 命中
_TECH_KEYS = sorted(ALIASES.keys(), key=len, reverse=True)

# 出现即算"技术名词"（用于规则 6：技术词 ≤3 个，防堆砌）
TECH_TERMS = set(ALIASES.values())

# 业务/场景词：用于规则 3 的"引用 JD 具体词"
DOMAIN_WORDS = [
    "电商", "商城", "订单", "支付", "权限", "数据统计", "后台", "管理后台",
    "预约", "医疗", "问诊", "内容", "文章", "报表", "接口", "联调", "部署",
    "客服", "知识库", "工作流", "多轮", "上下文", "剪辑", "素材", "SaaS",
    "会员", "订阅", "小程序", "直播", "游戏", "物流", "ERP", "OA", "MES",
    "自动化", "爬虫", "数据可视化", "ETL",
]

# ---------------------------------------------------------------- 事实库
# 只有这里列出的，才允许出现在招呼语里（规则 8）
#
# 本仓库为公开版本，个人信息一律脱敏为示例值。
# 使用前请把真实经历写进 gate/facts.local.json（已在 .gitignore 中，不会上传），
# 下面这些默认值会被同名 key 覆盖。
FACTS = {
    "degree": "本科在读（20XX 届）",
    "school": "某某大学 · 计算机科学与技术（20XX–20XX）",
    "college": "某某学院 · 软件应用技术（大专）",
    "internships": [
        "某某信息（20XX.XX–20XX.XX，Java）",
        "某某网络（20XX.XX–20XX.XX，Java 全栈）",
    ],
    "projects": [
        "电商商城（商品/订单/支付）",
        "运营后台（权限/数据统计）",
        "预约类小程序（uni-app + 微信支付 + OSS）",
        "RAG 客服问答（示例）",
        "Agent 工作流（示例）",
    ],
    "availability": "可随时到岗 / 每周 3–5 天 / 可稳定实习 3–6 个月",
    "expected_salary": "面议",
    "city": "某某城市（可接受周边）",
    "interview": "可线下面试，工作日下午可到",
}

# 本地真实事实库（可选）：同名 key 覆盖上面的示例值
_facts_local = Path(__file__).with_name("facts.local.json")
if _facts_local.exists():
    FACTS.update(json.loads(_facts_local.read_text(encoding="utf-8")))

# 明确禁止的"无依据声明"（规则 8 硬拦截）
FORBIDDEN_CLAIMS = [
    (r"硕士|研究生|博士", "学历：你是本科在读，不能写硕士/研究生/博士"),
    (
        r"(?:[3-9]|[1-9]\d)\s*年(?:以上)?(?:工作|开发|从业|经验)",
        "经验年限：你只有 2 段实习，不能声称 3 年以上正式工作经验",
    ),
    (
        r"(?:在|曾在|于)\s*(?:阿里|腾讯|字节|美团|百度|京东|网易|华为)",
        "大厂经历：事实库里没有大厂，写了就是造假",
    ),
    (r"正式员工|全职已入职|已毕业", "身份：你是在读学生，2027 届"),
]

# 规则 2：禁止的开场（命中即判"一眼群发"）
BAD_OPENINGS = [
    (r"^您好[，,]\s*我是", "禁「您好，我是…」开场"),
    (r"^您好[，,]?\s*$", "禁单独的「您好，」占一行开场"),
    (r"^我是\s*27\s*届", "禁「我是27届本科…」开场"),
    (r"^我(?:是|这边是)[^。！？]{0,8}(?:本科|应届|在校)", "禁自报学历开场"),
    (r"^看到(?:岗位|贵司|你们)(?:是|做)?[^。！？]{0,12}(?:\d+\s*[-~]\s*\d+\s*[Kk千]|\d+\s*[-~]\s*\d+\s*元)", "禁抄薪资开场"),
    (r"^贵(?:岗位|公司|司)", "禁「贵岗位是…」套话开场"),
]

# 规则 5：到岗信息
AVAILABILITY_PAT = [
    r"每周\s*\d\s*[-~到至]?\s*\d?\s*天",
    r"随时到岗", r"立即到岗", r"可随时到岗", r"随时可到岗",
    r"实习\s*\d+\s*个?月", r"稳定(?:实习)?\s*\d+\s*个?月", r"长期实习",
    r"到岗", r"可线下", r"线下面试",
]

# 规则 4：证据（数字或结果）
PROOF_PAT = [
    r"\d+\s*%",                      # 85%
    r"\d+\s*万",                     # 1.2 万
    r"\d+\s*倍",
    r"\d+\s*[条篇个人次单]",
    r"\d+\s*个?(?:月|天|周|年)",
    r"从\s*0\s*到\s*1",
    r"独立(?:做|完成|交付|推)",
    r"解决率",
    r"QPS|并发|延迟|响应时间",
    r"上线|发布|交付",
]

# 规则 9：结尾要收在"请对方回应"上
ENDING_PAT = [
    r"吗[？?]?$", r"呢[？?]?$", r"看下[？?]?$", r"看吗[？?]?$",
    r"聊聊[？?]?$", r"想聊[？?]?$", r"方便[^。！？]{0,10}[？?]$",
    r"可以[^。！？]{0,8}[？?]$", r"期待[^。！？]{0,10}$", r"[？?]$",
]
