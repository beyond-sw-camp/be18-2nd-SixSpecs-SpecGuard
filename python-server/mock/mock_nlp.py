from fastapi import FastAPI, Request
app = FastAPI()
@app.post("/ingest")
async def ingest(req: Request):
    data = await req.json()
    return {"ok": True, "received_posts": len(data.get("posts", []))}
