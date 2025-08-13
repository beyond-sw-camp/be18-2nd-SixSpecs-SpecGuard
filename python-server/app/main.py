from fastapi import FastAPI

import os


app = FastAPI()

@app.get("/")
async def root():
    return {"message": "Hello, FastAPI with Poetry!"}