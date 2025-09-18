package com.beyond.specguard.validation.model.repository;

import com.beyond.specguard.validation.model.entity.ValidationResultLog;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

//merge하면 위치 이동 예정
@Repository
public interface ResumeRepository extends JpaRepository<ValidationResultLog, UUID> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE resume SET status = :status WHERE id = :resumeId", nativeQuery = true)
    int updateStatus(@Param("resumeId") UUID resumeId, @Param("status") String status);
}
