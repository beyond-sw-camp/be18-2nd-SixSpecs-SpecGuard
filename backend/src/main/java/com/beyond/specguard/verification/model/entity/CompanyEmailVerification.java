package com.beyond.specguard.verification.model.entity;

import com.beyond.specguard.company.common.model.entity.ClientCompany;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "company_email_verify_status",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_comp_email_company", columnNames={"email","company_id"}),
                @UniqueConstraint(name="uk_comp_email_account", columnNames={"email","account_scope"})
        },
        indexes = {
                @Index(name = "idx_company_status", columnList = "status"),
                @Index(name = "idx_company_verified_at", columnList = "verifiedAt"),
                @Index(name="idx_company_company", columnList="company_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.experimental.SuperBuilder(toBuilder = true)
public class CompanyEmailVerification extends EmailVerificationBase {

        @ManyToOne(fetch = FetchType.LAZY, optional = true)
        @JoinColumn(name = "company_id",
                foreignKey = @ForeignKey(name = "fk_company_verif_company"))
        private ClientCompany company;

        @Column(name = "account_scope", nullable = false)
        private boolean accountScope;

        @Override
        protected boolean accountScopeFlag() { return accountScope; }

        @Override
        protected boolean hasScopeRef() { return company != null; }

        @Override
        protected String scopeLabel() { return "company"; }

        public static CompanyEmailVerification forAccountScope(String email) {
                return CompanyEmailVerification.builder()
                        .email(email)
                        .accountScope(true)
                        .status(EmailVerifyStatus.PENDING)
                        .attempts(0)
                        .build();
        }

        public static CompanyEmailVerification forCompany(String email, ClientCompany company) {
                return CompanyEmailVerification.builder()
                        .email(email)
                        .company(company)
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


