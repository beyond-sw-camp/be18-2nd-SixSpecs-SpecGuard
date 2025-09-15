package com.beyond.specguard.resume.model.entity.core;

import com.beyond.specguard.resume.model.dto.request.ResumeBasicCreateRequest;
import com.beyond.specguard.resume.model.entity.common.BaseEntity;
import com.beyond.specguard.resume.model.entity.common.enums.Gender;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Entity
@Table(
        name = "resume_basic",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_resume_basic_resume",
                columnNames = "resume_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class ResumeBasic extends BaseEntity {

    //일대일
    //resume_id는 FK
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "resume_id",
            nullable = false,
            columnDefinition = "CHAR(36)",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @JsonIgnore
    private Resume resume;


    //영어 이름
    @Column(name = "english_name", nullable = false, length = 100)
    private String englishName;

    //성별
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    //생년월일
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    //국적
    @Column(name = "nationality", nullable = false, length = 50)
    private String nationality;

    //지원 분야
    @Column(name = "apply_field", nullable = false, length = 100)
    private String applyField;

    //프로필 사진 - 일단 URL로
    @Column(name = "profile_image_url", length = 512)
    private String profileImageUrl;

    //주소
    @Column(name = "address", nullable = false, length = 255)
    private String address;

    //특기
    @Column(name = "specialty", length = 255)
    private String specialty;

    //취미
    @Column(name = "hobbies", length = 255)
    private String hobbies;


    public void changeEnglishName(String v) { this.englishName = v; }
    public void changeGender(Gender v)      { this.gender = v; }
    public void changeBirthDate(LocalDate v){ this.birthDate = v; }
    public void changeNationality(String v) { this.nationality = v; }
    public void changeApplyField(String v)  { this.applyField = v; }
    public void changeProfileImageUrl(String v) { this.profileImageUrl = v; }
    public void changeAddress(String v)     { this.address = v; }
    public void changeSpecialty(String v)   { this.specialty = v; }
    public void changeHobbies(String v)     { this.hobbies = v; }

    @Builder
    public ResumeBasic(Resume resume, String englishName, Gender gender, LocalDate birthDate, String nationality, String applyField, String profileImageUrl, String address, String specialty, String hobbies) {
        this.resume = resume;
        this.englishName = englishName;
        this.gender = gender;
        this.birthDate = birthDate;
        this.nationality = nationality;
        this.applyField = applyField;
        this.profileImageUrl = profileImageUrl;
        this.address = address;
        this.specialty = specialty;
        this.hobbies = hobbies;
    }

    public void update(ResumeBasicCreateRequest req) {
        if (req.englishName() != null) this.englishName = req.englishName();
        if (req.gender() != null)      this.gender = req.gender();
        if (req.birthDate() != null)   this.birthDate = req.birthDate();
        if (req.nationality() != null) this.nationality = req.nationality();
        if (req.applyField() != null)  this.applyField = req.applyField();
        if (req.address() != null)     this.address = req.address();
        if (req.specialty() != null)   this.specialty = req.specialty();
        if (req.hobbies() != null)     this.hobbies = req.hobbies();
        if (req.profileImage() != null) this.profileImageUrl = req.profileImage();
    }
}
