package com.beyond.specguard.validation.model.repository;

import com.beyond.specguard.validation.model.dto.response.ValidationResultLogResponseDto;
import com.beyond.specguard.validation.model.entity.ValidationResultLog;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ValidationResultLogRepository extends JpaRepository<ValidationResultLog, UUID> {
    @Query("""
        select new com.beyond.specguard.validation.model.dto.response.ValidationResultLogResponseDto(
            l.id, r.id, l.validationScore, l.validatedAt, l.keywordList, l.mismatchFields, l.matchFields
        )
          from ValidationResultLog l
          join l.validationResult vr
          join vr.resume r
         where r.id = :resumeId
         order by l.validatedAt desc
    """)
    List<ValidationResultLogResponseDto> findAllDtosByResumeId(@Param("resumeId") UUID resumeId);

    @Query("""
        select l
          from ValidationResultLog l
         where l.validationResult.id = :resultId
    """)
    List<ValidationResultLog> findAllByResultId(@Param("resultId") UUID resultId);
}