package com.beyond.specguard.verification.model.entity;

import com.beyond.specguard.company.common.model.entity.ClientCompany;
import jakarta.persistence.*;

@Entity
@Table(
        name = "company_email_verify_status",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_app_email_company", columnNames={"email","company_id"}),
                @UniqueConstraint(name="uk_app_email_account", columnNames={"email","account_scope"})
        },
        indexes = {
                @Index(name = "idx_company_status", columnList = "status"),
                @Index(name = "idx_company_verified_at", columnList = "verifiedAt"),
                @Index(name="idx_company_company", columnList="company_id")
        }
)

public class CompanyEmailVerification extends EmailVerificationBase{
        @ManyToOne(fetch = FetchType.LAZY, optional = true)
        @JoinColumn(name = "company_id", foreignKey = @ForeignKey(name = "fk_company_verif_company"))
        private ClientCompany company;

        @Column(name = "account_scope", nullable = false)
        private boolean accountScope;

        public ClientCompany getCompany(){ return company; }
        public void setCompany(ClientCompany c){ this.company = c; }
        public boolean isAccountScope(){ return accountScope; }
        public void setAccountScope(boolean v){ this.accountScope = v; }
}
