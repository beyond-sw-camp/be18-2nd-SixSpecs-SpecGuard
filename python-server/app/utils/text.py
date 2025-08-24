import re
import hashlib

# 아주 얕은 마스킹(필요 시 패턴 확장 가능)
_PII_EMAIL = re.compile(r"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}")
_PII_PHONE = re.compile(r"\b01[0-9]-?\d{3,4}-?\d{4}\b")

def mask_pii(text: str) -> str:
    if not text:
        return text
    text = _PII_EMAIL.sub("[email]", text)
    text = _PII_PHONE.sub("[phone]", text)
    return text

def content_hash(text: str, fallback: str = "") -> str:
    """
    본문 기반 해시. 본문이 없을 땐 fallback(url 등)으로 해시.
    """
    data = (text or fallback).encode("utf-8", "ignore")
    return hashlib.md5(data).hexdigest()
