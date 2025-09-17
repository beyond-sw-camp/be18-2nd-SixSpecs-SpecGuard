package com.beyond.specguard.result.model.repository;

import com.beyond.specguard.result.model.entity.ValidationResultLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ValidationResultLogRepository extends JpaRepository<ValidationResultLog, UUID> {
}
