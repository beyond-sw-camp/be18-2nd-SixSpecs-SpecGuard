package com.beyond.specguard.verification.model.entity;

import com.beyond.specguard.resume.model.entity.core.Resume;
import jakarta.persistence.*;

@Entity
@Table(
        name = "applicant_email_verify_status",
        indexes = {
                @Index(name = "idx_applicant_status", columnList = "status"),
                @Index(name = "idx_applicant_verified_at", columnList = "verifiedAt"),
                @Index(name="idx_applicant_resume", columnList="resume_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name="uk_applicant_email_resume", columnNames={"email","resume_id"})
        }
        )
public class ApplicantEmailVerification extends EmailVerificationBase {
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "resume_id",
                nullable = false,
                foreignKey = @ForeignKey(name = "fk_applicant_verif_resume"))
        private Resume resume;
}
