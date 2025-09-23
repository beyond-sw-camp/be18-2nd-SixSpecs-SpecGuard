from app.services.gemini_client import client
import re
from fastapi import HTTPException
from app.utils.codec import decompress_gzip
import json
import logging

from app.db import (
    SessionLocal,
    SQL_FIND_CRAWLING_RESULTS_BY_RID,
    SQL_UPSERT_PORTFOLIO_RESULT
)

MODEL = "gemini-2.5-flash"

logger = logging.getLogger(__name__)

async def insert_failed_data(session, row):
    processed_data = {"keywords": {}}
    status = "FAILED"
    await session.execute(
        SQL_UPSERT_PORTFOLIO_RESULT,
        {
            "crawling_result_id": row.crawling_result_id,
            "processed_contents": json.dumps(processed_data, ensure_ascii=False),
            "status": status
        }
    )

async def extract_keywrods_with_resume_id(resume_id: str):
    async with SessionLocal() as session:
        # 1. crawling_result 조회
        res = await session.execute(
            SQL_FIND_CRAWLING_RESULTS_BY_RID,
            {"rid": resume_id},
        )

        rows = res.fetchall()

        if not rows:
        # 주어진 resume_id로 crawling_result 행을 못 찾음
            raise HTTPException(
                status_code=404,
                detail={"errorCode": "NOT_FOUND", "message": "resume_link(row) not found for given resume_id/url"},
            )
        
        portfolio_entries = []

        for row in rows:

            crawling_status = row.crawling_status
            
            if crawling_status in ["FAILED", "NOTEXISTED"]:

                await insert_failed_data(session, row)
                continue

            elif crawling_status in ["RUNNING", "PENDING"]:
                continue

            try:
                raw_contents = await decompress_gzip(row.contents)

                print(raw_contents[:100])
                data_json = json.loads(raw_contents)
            except Exception as e:
                logger.error("Decompress/JSON parse error: {%s}", e)
                await insert_failed_data(session, row)
                continue  # 해당 row 스킵
            
            try:
                if row.link_type == "VELOG":
                    dumped_data = json.dumps(data_json.get("recent_activity", []), ensure_ascii=False)
                    logger.warning("===== VELOG raw_contents ===== %s", raw_contents)
                    logger.warning("===== VELOG dumped_data ===== %s", dumped_data)
                    processed_data = {
                        "keywords": await extract_keywords(dumped_data),
                        "count": int(data_json.get("postCount", 0)),
                        "dateCount": await extract_dateCount(dumped_data),
                    }
                elif row.link_type == "GITHUB":
                    dumped_data = json.dumps(data_json.get("repoReadme", ""), ensure_ascii=False)
                    processed_data = {
                        "keywords": await extract_keywords(dumped_data),
                        "tech": await extract_keywords(dumped_data, "기술 스택 키워드"),
                        "commits": int(data_json.get("commitCount", 0)),
                        "repos": int(data_json.get("repositoryCount", 0)),
                    }
                elif row.link_type == "NOTION":
                    dumped_data = json.dumps(data_json.get("content", ""), ensure_ascii=False)
                    processed_data = {
                        "keywords": await extract_keywords(dumped_data),
                    }
                else:
                    logger.warning("Unsupported link type: {%s}", row.link_type)
                    await insert_failed_data(session, row)
                    continue

                status = "COMPLETED"

            except KeywordExtractionError as e:
                    logger.warning("키워드 추출 실패: %s", e)
                    await insert_failed_data(session, row)
                    continue
            
            except Exception as e:
                # 지원하지 않는 타입
                    logger.error("Processing failed for row {%s}: {%s}", row.crawling_result, e)
                    await insert_failed_data(session, row)
                    continue
            
            # 4. portfolio_result 삽입
            await session.execute(
                SQL_UPSERT_PORTFOLIO_RESULT,
                {
                    "crawling_result_id": row.crawling_result_id,
                    "processed_contents": json.dumps(processed_data, indent=2, ensure_ascii=False),
                    "status": status
                }
            )

            portfolio_entries.append({"crawling_result_id": row.crawling_result_id, "contents": processed_data})

        await session.commit()

    return {"resumeId": resume_id, "processed": portfolio_entries}


async def extract_dateCount(text: str) -> int:
    prompt = f"""
    다음 텍스트에서 최근 1년 안에 작성된 게시글 수 반환해줘
    - 시간은 쿼리를 날린 현재시점 기준이야
    - 본문 내용에서 "2025-00-00" 형태인 날짜들의 개수를 세줘
    - 이러한 날짜들의 개수를 모두 카운트 한 뒤에 그 수에 나누기 2를 해줘
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
        return int(re.sub(r"\D", "", raw_output))  # 숫자만 추출
    
    except Exception as e:
        logger.error("최근 시간 검색 에 실패했습니다. {%s}", str(e))
        return 0



async def extract_keywords(text: str, type="기술 키워드") -> list:
    prompt = f"""
    다음은 조건들이야. 이 조건들을 활용해서 '텍스트:' 이후 내용에서 {type} 위주로 모두 뽑아줘.
    - 출력은 JSON 배열 형식으로만 반환해.
    - 예시: ["AI", "백엔드", "Docker", "라즈베리파이", "MQTT"]
    - 코드 블록 표시(````json`, ```), 설명 문장, 줄바꿈 같은 건 절대 포함하지 마.
    - 지원자의 활동 위주로 키워드를 뽑아야해.
    - 기업 사업 관련 키워드는 넣지 말아줘.
    - 만약 키워드 추출에 실패하거나 텍스트에서 키워드를 찾을 수 없다면 반드시 빈 배열 [] 만 반환해.
    '텍스트': {text.strip()}
    """

    print(prompt)

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
        print(clean_output)
        # 6) JSON 배열 파싱
        try:
            keywords = json.loads(clean_output)
        except json.JSONDecodeError as e:
            logger.error("NLP 서버 응답이 올바른 JSON 배열이 아닙니다: %s", clean_output)
            raise KeywordExtractionError("JSON 파싱 실패") from e

        # 7) 결과 타입 검증
        if not isinstance(keywords, list):
            logger.error("키워드 응답이 배열 형식이 아닙니다.")
            raise KeywordExtractionError("리턴값이 리스트 아님")
        
        return keywords
    
    except Exception as e:
        logger.error("키워드 추출에 실패했습니다. : %s", e)
        raise KeywordExtractionError(e) from e

class KeywordExtractionError(Exception):
    """키워드 추출 실패 예외"""
    pass