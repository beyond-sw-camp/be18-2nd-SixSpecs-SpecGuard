from fastapi import FastAPI, Request, HTTPException
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from starlette.exceptions import HTTPException as StarletteHTTPException

#서비스 에러코드 매핑 
ERROR_CODE_BY_STATUS = {
    400: "INVALID_INPUT_VALUE",
    404: "NOT_FOUND",
    422: "VALIDATION_ERROR",
    500: "INTERNAL_SERVER_ERROR",
}

#최소 응답 JSON 만드는 작은 헬퍼 
def _pack(status: int, message: str, code: str | None = None) -> dict:
    return {
        "status": status,
        "errorCode": code or ERROR_CODE_BY_STATUS.get(status, "ERROR"),
        "message": message,
    }

def install_error_handlers(app: FastAPI) -> None:
    @app.exception_handler(RequestValidationError)
    async def on_validation(request: Request, exc: RequestValidationError):
        return JSONResponse(
            status_code=400,
            content=_pack(400, "Invalid parameters", "INVALID_INPUT_VALUE"),
        )

    @app.exception_handler(HTTPException)
    async def on_http_exc(request: Request, exc: HTTPException):
        det = exc.detail
        if isinstance(det, dict):
            msg = det.get("message") or det.get("detail") or ""
            code = det.get("errorCode") or det.get("error") or ERROR_CODE_BY_STATUS.get(exc.status_code, "ERROR")
            return JSONResponse(status_code=exc.status_code, content=_pack(exc.status_code, msg, code))
        return JSONResponse(
            status_code=exc.status_code,
            content=_pack(exc.status_code, str(det), ERROR_CODE_BY_STATUS.get(exc.status_code, "ERROR")),
        )

    @app.exception_handler(Exception)
    async def on_unhandled(request: Request, exc: Exception):
        return JSONResponse(status_code=500, content=_pack(500, "서버 내부 에러"))
    
    @app.exception_handler(StarletteHTTPException)
    async def on_starlette_http_exc(request: Request, exc: StarletteHTTPException):
        # 존재하지 않는 라우트 등
        msg = exc.detail if isinstance(exc.detail, str) else "Not Found"
        code = ERROR_CODE_BY_STATUS.get(exc.status_code, "ERROR")
        return JSONResponse(status_code=exc.status_code, content=_pack(exc.status_code, msg, code))