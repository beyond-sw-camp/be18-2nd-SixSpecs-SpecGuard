package com.beyond.specguard.verification.model.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "company_email_verify_status",
        indexes = {
                @Index(name = "idx_company_status", columnList = "status"),
                @Index(name = "idx_company_verified_at", columnList = "verifiedAt")
        })
public class CompanyEmailVerification extends EmailVerificationBase{
}
