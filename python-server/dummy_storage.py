from fastapi import FastAPI, Request
app = FastAPI()
@app.post("/api/v1/storage/resume-links")
async def receive(req: Request):
    body = await req.json()
    return {"status": "ok", "inserted": len(body.get("records", []))}
