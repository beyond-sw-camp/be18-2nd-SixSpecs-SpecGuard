package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.common.enums.ResumeStatus;
import com.beyond.specguard.resume.entity.core.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResumeRepository extends JpaRepository<Resume, UUID> {


    // 단건 조회 시 1:1 기본정보(ResumeBasic)까지 함께 로딩
    @EntityGraph(attributePaths = {"basic"})
    Optional<Resume> findById(UUID id);

    // 이메일 기반 조회/중복 체크
    Optional<Resume> findByEmail(String email);
    boolean existsByEmail(String email);
}
