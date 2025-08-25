from fastapi import FastAPI, Request
app = FastAPI()
@app.post("/api/v1/storage/resume-links")
async def save_links(req: Request):
    data = await req.json()
    return {"ok": True, "stored": len(data)}
