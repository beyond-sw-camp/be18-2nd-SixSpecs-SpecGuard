import gzip, json, base64

def to_gzip_base64_from_json(data: dict) -> str:
    raw = json.dumps(data, ensure_ascii=False).encode("utf-8")
    gz = gzip.compress(raw, compresslevel=6)
    return base64.b64encode(gz).decode("ascii")

def to_gzip_base64_from_text(text: str) -> str:
    gz = gzip.compress((text or "").encode("utf-8"), compresslevel=6)
    return base64.b64encode(gz).decode("ascii")

def to_gzip_bytes_from_json(data: dict) -> bytes:
    raw = json.dumps(data, ensure_ascii=False).encode("utf-8")
    return gzip.compress(raw, compresslevel=6)

def to_gzip_bytes_from_text(text: str) -> bytes:
    return gzip.compress((text or "").encode("utf-8"), compresslevel=6)
