package com.beyond.specguard.verification.model.entity;

import com.beyond.specguard.resume.model.entity.core.Resume;
import jakarta.persistence.*;

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
public class ApplicantEmailVerification extends EmailVerificationBase {

        @ManyToOne(fetch = FetchType.LAZY, optional = true) // 계정 스코프는 null 허용
        @JoinColumn(name = "resume_id", foreignKey = @ForeignKey(name = "fk_applicant_verif_resume"))
        private Resume resume;

        @Column(name = "account_scope", nullable = false)
        private boolean accountScope;

        public Resume getResume(){ return resume; }
        public void setResume(Resume r){ this.resume = r; }
        public boolean isAccountScope(){ return accountScope; }
        public void setAccountScope(boolean v){ this.accountScope = v; }
}
