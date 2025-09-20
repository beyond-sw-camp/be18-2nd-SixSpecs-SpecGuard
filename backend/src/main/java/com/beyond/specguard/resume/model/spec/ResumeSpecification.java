package com.beyond.specguard.resume.model.spec;

import com.beyond.specguard.resume.model.entity.Resume;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ResumeSpecification {
    public static Specification<Resume> hasCompany(UUID companyId) {
        return (root, query, cb) -> {
            if (companyId == null) return null;
            // Resume → CompanyTemplate Join
            var templateJoin = root.join("template"); // Resume.template

            // CompanyTemplate → ClientCompany Join
            var companyJoin = templateJoin.join("clientCompany"); // template.company

            return cb.equal(companyJoin.get("id"), companyId);
        };
    }

    public static Specification<Resume> hasStatus(Resume.ResumeStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Resume> nameContains(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Resume> emailContains(String email) {
        return (root, query, cb) -> {
            if (email == null || email.isBlank()) return null;
            return cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }

    //템플릿ID 필터
    public static Specification<Resume> hasTemplate(UUID templateId) {
        return (root, query, cb) -> templateId == null
                ? cb.conjunction()
                : cb.equal(root.get("template").get("id"), templateId);
    }
}
