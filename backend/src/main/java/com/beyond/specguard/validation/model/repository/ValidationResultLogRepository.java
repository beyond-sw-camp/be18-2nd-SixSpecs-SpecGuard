package com.beyond.specguard.validation.model.repository;

import com.beyond.specguard.validation.model.dto.response.ValidationResultLogResponseDto;
import com.beyond.specguard.validation.model.entity.ValidationResultLog;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ValidationResultLogRepository extends JpaRepository<ValidationResultLog, UUID> {
    @Query("""
    select new com.beyond.specguard.validation.model.dto.response.ValidationResultLogResponseDto(
        l.id, r.id, l.validationScore, l.validatedAt, l.descriptionComment
    )
    from ValidationResultLog l
    join l.validationResult vr
    join vr.resume r
    where r.id = :resumeId
    order by l.validatedAt desc
    """)
    List<ValidationResultLogResponseDto> findAllDtosByResumeId(@Param("resumeId") UUID resumeId);

//    //최신 로그 keyword_list(JSON)만 가져오기
//    @Query(value = """
//        SELECT l.keyword_list
//          FROM validation_result_log l
//          JOIN validation_result vr ON l.validation_result_id = vr.id
//         WHERE vr.resume_id = :resumeId
//         ORDER BY l.validated_at DESC
//         LIMIT 1
//        """, nativeQuery = true)
//    Optional<String> findLatestKeywordListJson(@Param("resumeId") UUID resumeId);


}