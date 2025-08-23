import re, hashlib
EMAIL_RX = re.compile(r'[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}')
PHONE_RX = re.compile(r'(?:\+?\d{1,3}[-.\s]?)?(?:\d{2,4}[-.\s]?){2,3}\d{3,4}')

def mask_pii(text: str) -> str:
    if not text:
        return text
    return PHONE_RX.sub('***-****-****', EMAIL_RX.sub('***@***', text))

def content_hash(text: str, fallback: str = "") -> str:
    base = text if text else fallback
    return hashlib.sha256(base.encode("utf-8", "ignore")).hexdigest()
