package com.beyond.specguard.result.model.repository;

import com.beyond.specguard.result.model.entity.ValidationIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ValidationIssueRepository extends JpaRepository<ValidationIssue, UUID> {
}
