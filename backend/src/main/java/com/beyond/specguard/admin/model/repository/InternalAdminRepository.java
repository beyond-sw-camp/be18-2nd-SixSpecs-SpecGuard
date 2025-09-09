package com.beyond.specguard.admin.model.repository;

import com.beyond.specguard.admin.model.entity.InternalAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InternalAdminRepository extends JpaRepository<InternalAdmin, UUID> {
    Optional<InternalAdmin> findByEmail(String email);
}
