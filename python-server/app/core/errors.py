from fastapi import FastAPI, Request, HTTPException
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from starlette.exceptions import HTTPException as StarletteHTTPException


ERROR_CODE_BY_STATUS = {
    400: "INVALID_INPUT_VALUE",
    401: "UNAUTHORIZED",
    403: "ACCESS_DENIED",
    404: "NOT_FOUND",
    422: "VALIDATION_ERROR",
    500: "INTERNAL_SERVER_ERROR",
}

def _pack(status: int, message: str, code: str | None = None) -> dict:
    return {
        "status": status,
        "error": code or ERROR_CODE_BY_STATUS.get(status, "ERROR"),
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
            return JSONResponse(
                status_code=exc.status_code,
                content=_pack(exc.status_code, det.get("message", ""), det.get("error")),
            )
        return JSONResponse(
            status_code=exc.status_code,
            content=_pack(exc.status_code, str(det)),
        )

    @app.exception_handler(Exception)
    async def on_unhandled(request: Request, exc: Exception):
        return JSONResponse(
            status_code=500,
            content=_pack(500, "서버 내부 에러"),
        )
