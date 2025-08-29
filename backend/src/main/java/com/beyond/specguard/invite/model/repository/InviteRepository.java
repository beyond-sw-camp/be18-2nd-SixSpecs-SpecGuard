package com.beyond.specguard.invite.model.repository;

import com.beyond.specguard.invite.model.entity.InviteEntity;
import com.beyond.specguard.invite.model.entity.InviteEntity.InviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InviteRepository extends JpaRepository<InviteEntity, String> {

    /**
     * 특정 회사 + 이메일 기준으로
     * 아직 PENDING 상태인 초대가 있는지 확인 (중복 초대 방지용)
     */
    boolean existsByEmailAndCompanyIdAndStatus(String email, String companyId, InviteStatus status);

    /**
     * 초대 토큰으로 초대 조회
     */
    Optional<InviteEntity> findByInviteToken(String inviteToken);

    Optional<InviteEntity> findByInviteTokenAndStatus(String inviteToken, InviteEntity.InviteStatus status);
}
