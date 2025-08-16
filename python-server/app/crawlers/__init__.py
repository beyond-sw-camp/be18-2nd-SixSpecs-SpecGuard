#크롤링 공통 설정(CONF), 코드블록 언어 정규화 테이블 등 상수만 선언
# 공통 설정
CONF = {
    "headers": {"User-Agent": "SpecGuardBot/1.0 (+https://example.com)"},
    "viewport": {"width": 1280, "height": 900},
    "list": {"max_scrolls": 150, "pause_sec": 1.0, "timeout_ms": 25000, "stagnant_rounds": 3},
    "post": {"timeout_ms": 20000, "hard_extra_sec": 4},
    "dirs": {"snapshots": ".snapshots"},
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
