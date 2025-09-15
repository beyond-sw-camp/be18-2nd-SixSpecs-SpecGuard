package com.beyond.specguard.verification.model.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "applicant_email_verify_status",
        indexes = {
                @Index(name = "idx_applicant_status", columnList = "status"),
                @Index(name = "idx_applicant_verified_at", columnList = "verifiedAt")
        })
public class ApplicantEmailVerification extends EmailVerificationBase {
}
