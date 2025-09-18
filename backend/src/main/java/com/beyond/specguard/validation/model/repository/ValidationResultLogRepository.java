package com.beyond.specguard.validation.model.repository;

import com.beyond.specguard.validation.model.dto.response.ValidationResultLogResponseDto;
import com.beyond.specguard.validation.model.entity.ValidationResultLog;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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


    //코멘트
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ValidationResultLog l set l.descriptionComment = :comment where l.id = :logId")
    int updateDescriptionComment(@Param("logId") UUID logId, @Param("comment") String comment);

    @Query("""
    select new com.beyond.specguard.validation.model.dto.response.ValidationResultLogResponseDto(
        l.id, r.id, l.validationScore, l.validatedAt, l.descriptionComment
    )
    from ValidationResultLog l
    join l.validationResult vr
    join vr.resume r
    where l.id = :logId
    """)
    Optional<ValidationResultLogResponseDto> findDtoByLogId(@Param("logId") UUID logId);

}