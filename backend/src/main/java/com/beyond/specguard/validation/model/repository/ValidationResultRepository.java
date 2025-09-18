package com.beyond.specguard.validation.model.repository;

import com.beyond.specguard.validation.model.entity.ValidationResult;
import com.beyond.specguard.validation.model.entity.ValidationResultLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ValidationResultRepository extends JpaRepository<ValidationResult, UUID> {
}
