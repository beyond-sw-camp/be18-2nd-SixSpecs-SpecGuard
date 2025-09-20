package com.beyond.specguard.resume.model.dto.request;

import com.beyond.specguard.resume.model.entity.Resume;
import com.beyond.specguard.resume.model.entity.ResumeCertificate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ResumeCertificateUpsertRequest(
        @Valid
        List<Item> certificates
) {
        public record Item(
                @Schema(description = "자격증 항목 ID (없으면 생성)")
                UUID id,

                @Schema(description = "자격증 명", example = "정보처리기사")
                @NotBlank
                String certificateName,

                @Schema(description = "자격증 번호", example = "09-1-123456A")
                @NotBlank
                String certificateNumber,

                @Schema(description = "발행 기관", example = "한국산업인력공단")
                @NotBlank
                String issuer,

                @Schema(description = "취득일", type = "string", format = "date", example = "2020-05-20")
                @NotNull
                LocalDate issuedDate
        ) {
                public ResumeCertificate toEntity(Resume resume) {
                        return ResumeCertificate.builder()
                                .certificateName(certificateName)
                                .certificateNumber(certificateNumber)
                                .issuer(issuer)
                                .issuedDate(issuedDate)
                                .id(id)
                                .resume(resume)
                                .build();
                }
        }
}
