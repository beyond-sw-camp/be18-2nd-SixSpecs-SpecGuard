# 크롤링 공통 설정/상수

UA = "SpecGuardBot/1.0 (+https://example.com)"
CONF = {
    "headers": {"User-Agent": UA},
    "viewport": {"width": 1280, "height": 900},
    "list": {"max_scrolls": 150, "pause_sec": 1.0, "timeout_ms": 25000, "stagnant_rounds": 3},
    "post": {"timeout_ms": 20000, "hard_extra_sec": 4},
}

# 코드 언어 정규화/화이트리스트/노이즈
LANG_NORMALIZE = {
    "js": "javascript", "jsx": "javascript",
    "ts": "typescript", "tsx": "typescript",
    "c++": "cpp", "c#": "csharp", "sh": "bash",
    "yml": "yaml", "py": "python",
}
WHITELIST = {
    "java","javascript","typescript","python","sql","html","css","scss",
    "cpp","c","csharp","kotlin","swift","go","rust","bash","shell",
    "yaml","json","xml","markdown",
}
NOISE = {"flow","null","plaintext","text","md","none","unknown"}
