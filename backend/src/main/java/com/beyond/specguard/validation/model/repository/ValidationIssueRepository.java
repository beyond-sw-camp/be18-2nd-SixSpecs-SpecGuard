package com.beyond.specguard.validation.model.repository;

import com.beyond.specguard.validation.model.entity.ValidationIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ValidationIssueRepository extends JpaRepository<ValidationIssue, UUID> {
}
