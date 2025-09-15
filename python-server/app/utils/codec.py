import gzip, json

def to_gzip_bytes_from_json(data: dict) -> bytes:
    raw = json.dumps(data, ensure_ascii=False).encode("utf-8")
    return gzip.compress(raw, compresslevel=6)

def to_gzip_bytes_from_text(text: str) -> bytes:
    return gzip.compress((text or "").encode("utf-8"), compresslevel=6)
