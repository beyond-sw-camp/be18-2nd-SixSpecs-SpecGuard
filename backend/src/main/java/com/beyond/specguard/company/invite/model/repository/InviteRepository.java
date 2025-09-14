package com.beyond.specguard.company.invite.model.repository;

import com.beyond.specguard.auth.model.entity.ClientCompany;
import com.beyond.specguard.company.invite.model.entity.InviteEntity;
import com.beyond.specguard.company.invite.model.entity.InviteEntity.InviteStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InviteRepository extends JpaRepository<InviteEntity, String> {

    /**
     * 초대 토큰으로 초대 조회
     */
    Optional<InviteEntity> findByInviteToken(String inviteToken);

    Optional<InviteEntity> findByInviteTokenAndStatus(String inviteToken, InviteStatus status);

    Optional<InviteEntity> findByEmailAndCompanyAndStatus(
            @Email @NotBlank(message = "이메일은 필수입니다.") String email,
            ClientCompany company,
            InviteStatus inviteStatus
    );
}
