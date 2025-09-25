import gzip, json
from io import BytesIO

def to_gzip_bytes_from_json(data: dict) -> bytes:
    raw = json.dumps(data, ensure_ascii=False).encode("utf-8")
    return gzip.compress(raw, compresslevel=6)

def to_gzip_bytes_from_text(text: str) -> bytes:
    return gzip.compress((text or "").encode("utf-8"), compresslevel=6)

async def decompress_gzip(data: bytes) -> str:
    with gzip.GzipFile(fileobj=BytesIO(data)) as f:
        return f.read().decode("utf-8")
    
def compress_gzip(data: str) -> bytes:
    buf = BytesIO()
    with gzip.GzipFile(fileobj=buf, mode="wb") as f:
        f.write(data.encode("utf-8"))
    return buf.getvalue()