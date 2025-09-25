package com.beyond.specguard.companytemplate.model.spec;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class CompanyTemplateSpecification {
    public static Specification<CompanyTemplate> hasDepartment(String department) {
        return department == null ? null : (root, query, cb) -> cb.equal(root.get("department"), department);
    }

    public static Specification<CompanyTemplate> hasCategory(String category) {
        return category == null ? null : (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    public static Specification<CompanyTemplate> startDateAfter(LocalDateTime startDate) {
        return startDate == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startDate"), startDate);
    }

    public static Specification<CompanyTemplate> endDateBefore(LocalDateTime endDate) {
        return endDate == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("endDate"), endDate);
    }

    public static Specification<CompanyTemplate> hasStatus(CompanyTemplate.TemplateStatus status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<CompanyTemplate> hasYearsOfExperience(Integer yearsOfExperience) {
        return yearsOfExperience == null ? null : (root, query, cb) -> cb.equal(root.get("yearsOfExperience"), yearsOfExperience);
    }

    public static Specification<CompanyTemplate> belongsToCompany(UUID companyId) {
        return companyId == null ? null : (root, q, cb) ->
                cb.equal(root.get("clientCompany").get("id"), companyId);
    }
}
