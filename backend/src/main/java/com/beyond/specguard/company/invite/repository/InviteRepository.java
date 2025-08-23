package com.beyond.specguard.company.invite.repository;

import com.beyond.specguard.company.invite.entity.InviteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InviteRepository extends JpaRepository<InviteEntity, String> {
    boolean existsByEmailAndCompanyIdAndIsUsedFalse(String email, String companyId);
    Optional<InviteEntity> findByInviteToken(String inviteToken);
}
