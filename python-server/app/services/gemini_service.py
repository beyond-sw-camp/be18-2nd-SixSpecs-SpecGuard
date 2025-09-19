from app.services.gemini_client import client
import json
import re
from fastapi import HTTPException
from app.utils.codec import decompress_gzip
import json

from app.db import (
    SessionLocal,
    SQL_FIND_CRAWLING_RESULTS_BY_RID,
    SQL_INSERT_PORTFOLIO_RESULT
)

MODEL = "gemini-2.0-flash-001"



async def extract_keywrods_with_resume_id(resume_id: str):
    async with SessionLocal() as session:
        # 1. crawling_result 조회
        res = await session.execute(
            SQL_FIND_CRAWLING_RESULTS_BY_RID,
            {"rid": resume_id},
        )

        rows = res.all()

        if not rows:
        # 주어진 resume_id로 crawling_result 행을 못 찾음
            raise HTTPException(
                status_code=404,
                detail={"errorCode": "NOT_FOUND", "message": "resume_link(row) not found for given resume_id/url"},
            )
        
        portfolio_entries = []

        for row in rows:
            # print(row)
            # 2. gzip 해제 후 JSON 로드
            try:
                raw_contents = await decompress_gzip(row.contents)

                data_json = json.loads(raw_contents)
            except Exception as e:
                print(e)
                continue  # 해당 row 스킵
            
            try:
                if row.link_type == "VELOG":
                    dumped_data = json.dumps(data_json["recent_activity"], indent=2, ensure_ascii=False)
                    processed_data = {
                        "keywords": await extract_keywords(dumped_data),
                        "count": int(data_json.get("postCount", 0)),
                        "dateCount": int(await extract_dateCount(dumped_data)),
                    }
                elif row.link_type == "GITHUB":
                    print("GITHUB")
                    dumped_data = json.dumps(data_json["repoReadme"], indent=2, ensure_ascii=False)
                    processed_data = {
                        "keywords": await extract_keywords(dumped_data),
                        "tech": await extract_keywords(dumped_data, "기술 스택 키워드"),
                        "commits": int(data_json.get("commitCount", 0)),
                        "repos": int(data_json.get("repositoryCount", 0)),
                    }
                elif row.link_type == "NOTION":
                    dumped_data = json.dumps(data_json["content"], indent=2, ensure_ascii=False)
                    processed_data = {
                        "keywords": await extract_keywords(dumped_data),
                    }
                else:
                    continue  # 지원하지 않는 링크 타입 스킵

                status = "COMPLETED"

            except Exception as e:
                # 지원하지 않는 타입
                    processed_data = {}
                    status = "FAILED"
                    await session.execute(
                        SQL_INSERT_PORTFOLIO_RESULT,
                        {
                            "crawling_result_id": row.crawling_result_id,
                            "processed_contents": json.dumps(processed_data, ensure_ascii=False),
                            "status": status
                        }
                    )
                    continue
            
            # 4. portfolio_result 삽입
            await session.execute(
                SQL_INSERT_PORTFOLIO_RESULT,
                {
                    "crawling_result_id": row.crawling_result_id,
                    "processed_contents": json.dumps(processed_data, indent=2, ensure_ascii=False),
                    "status": status
                }
            )

            portfolio_entries.append({"crawling_result_id": row.crawling_result_id, "contents": processed_data})

        await session.commit()

    return {"resumeId": resume_id, "processed": portfolio_entries}


async def extract_dateCount(text: str) -> list:
    prompt = f"""
    다음 텍스트에서 최근 1년 안에 작성된 게시글 수 반환해줘
    - 시간은 쿼리를 날린 현재시점 기준이야
    - 텍스트 형태는 "2020-02-02 | [제목][본문]"일때 앞에 시간이 작성 날짜야.
    - 정확히 단 한 개의 integer로 반환해.
    텍스트: {text.strip()}
    """

    try:
        # 4) Gemini API 호출
        response = client.models.generate_content(
            model=MODEL,
            contents=prompt
        )
        raw_output = response.text.strip()
        return raw_output
    
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail={
                "error": "DATE_EXTRACTION_FAILED",
                "message": f"최근 시간 검색 에 실패했습니다. ({str(e)})"
            }
        )



async def extract_keywords(text: str, type="기술 키워드") -> list:
    prompt = f"""
    다음 텍스트에서 {type} 위주로 최대한 많이 뽑아줘.
    - 출력은 JSON 배열 형식으로만 반환해.
    - 예시: ["AI", "백엔드", "Docker", "라즈베리파이", "MQTT"]
    - 코드 블록 표시(````json`, ```), 설명 문장, 줄바꿈 같은 건 절대 포함하지 마.
    - 지원자의 활동 위주로 키워드를 뽑아야해.
    - 기업 사업 관련 키워드는 넣지 말아줘.
    텍스트: {text.strip()}
    """

    try:
        # 4) Gemini API 호출
        response = client.models.generate_content(
            model=MODEL,
            contents=prompt
        )
        raw_output = response.text.strip()

        # 5) 전처리: 코드블록 제거
        clean_output = re.sub(r"```(?:json)?", "", raw_output)
        clean_output = clean_output.replace("```", "").strip()

        # 6) JSON 배열 파싱
        try:
            keywords = json.loads(clean_output)
        except json.JSONDecodeError:
            raise HTTPException(
                status_code=500,
                detail={
                    "error": "INVALID_NLP_RESPONSE",
                    "message": f"NLP 서버 응답이 올바른 JSON 배열이 아닙니다: {raw_output}"
                }
            )

        # 7) 결과 타입 검증
        if not isinstance(keywords, list):
            raise HTTPException(
                status_code=500,
                detail={
                    "error": "INVALID_KEYWORD_FORMAT",
                    "message": "키워드 응답이 배열 형식이 아닙니다."
                }
            )
        
        return keywords
    
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail={
                "error": "KEYWORD_EXTRACTION_FAILED",
                "message": f"키워드 추출에 실패했습니다. ({str(e)})"
            }
        )