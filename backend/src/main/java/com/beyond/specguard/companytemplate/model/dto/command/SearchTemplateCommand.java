package com.beyond.specguard.companytemplate.model.dto.command;

import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import lombok.Builder;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

@Builder
public record SearchTemplateCommand(
    ClientUser clientUser,
     String department,
     String category,
     LocalDate startDate,
     LocalDate endDate,
     CompanyTemplate.TemplateStatus status,
     Integer yearsOfExperience,
     Pageable pageable
) {
}
