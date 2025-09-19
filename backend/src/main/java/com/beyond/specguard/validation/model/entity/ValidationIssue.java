package com.beyond.specguard.validation.model.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name="validation_issue"
)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@ToString
public class ValidationIssue {
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    @Column(length = 36, columnDefinition = "CHAR(36)")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name="validation_result", nullable = false)
    private ValidationResult validationResult;



    public enum ValidationResult{
        SUCCESS,
        FAILED
    }

}
