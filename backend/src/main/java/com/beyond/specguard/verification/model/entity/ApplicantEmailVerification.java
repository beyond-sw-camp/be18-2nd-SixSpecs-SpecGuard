package com.beyond.specguard.verification.model.entity;

import com.beyond.specguard.resume.model.entity.Resume;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "applicant_email_verify_status",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_app_email_resume", columnNames={"email","resume_id"}),
                @UniqueConstraint(name="uk_app_email_account", columnNames={"email","account_scope"})
        },
        indexes = {
                @Index(name="idx_applicant_status", columnList="status"),
                @Index(name="idx_applicant_verified_at", columnList="verifiedAt"),
                @Index(name="idx_applicant_resume", columnList="resume_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.experimental.SuperBuilder(toBuilder = true)
public class ApplicantEmailVerification extends EmailVerificationBase {

        @ManyToOne(fetch = FetchType.LAZY, optional = true)
        @JoinColumn(name = "resume_id", foreignKey = @ForeignKey(name = "fk_applicant_verif_resume"))
        private Resume resume;

        @Column(name = "account_scope", nullable = false)
        private boolean accountScope;

        @Override
        protected boolean accountScopeFlag() { return accountScope; }

        @Override
        protected boolean hasScopeRef() { return resume != null; }

        @Override
        protected String scopeLabel() { return "resume"; }

        public static ApplicantEmailVerification forAccountScope(String email) {
                return ApplicantEmailVerification.builder()
                        .email(email)
                        .accountScope(true)
                        .status(EmailVerifyStatus.PENDING)
                        .attempts(0)
                        .build();
        }

        public static ApplicantEmailVerification forResume(String email, Resume resume) {
                return ApplicantEmailVerification.builder()
                        .email(email)
                        .resume(resume)
                        .accountScope(false)
                        .status(EmailVerifyStatus.PENDING)
                        .attempts(0)
                        .build();
        }

        public void markPending(String ip, LocalDateTime now) {
                this.status = EmailVerifyStatus.PENDING;
                this.attempts = (this.attempts == null ? 1 : this.attempts + 1);
                this.lastRequestedAt = now;
                this.lastIp = ip;
        }

        public void markVerified(LocalDateTime now) {
                this.status = EmailVerifyStatus.VERIFIED;
                this.verifiedAt = now;
        }
}

