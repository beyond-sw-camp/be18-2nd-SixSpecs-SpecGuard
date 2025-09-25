package com.beyond.specguard.plan.model.repository;

import com.beyond.specguard.plan.model.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlanRepository extends JpaRepository<Plan, UUID> {
}
