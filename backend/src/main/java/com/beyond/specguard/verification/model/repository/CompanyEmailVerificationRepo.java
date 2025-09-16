package com.beyond.specguard.verification.model.repository;

import com.beyond.specguard.verification.model.entity.CompanyEmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.*;

public interface CompanyEmailVerificationRepo
        extends JpaRepository<CompanyEmailVerification, UUID> {
    Optional<CompanyEmailVerification> findByEmailAndAccountScopeTrue(String email);
    Optional<CompanyEmailVerification> findByEmailAndCompanyId(String email, UUID companyId);
}
