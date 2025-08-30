package com.beyond.specguard.resume.entity.core;

import com.beyond.specguard.resume.entity.common.BaseEntity;
import com.beyond.specguard.resume.entity.common.enums.Gender;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

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
public class ResumeBasic extends BaseEntity {

    //PK
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    //일대일
    //resume_id는 FK
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
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


    @Builder

    public ResumeBasic(String id, Resume resume, String englishName, Gender gender, LocalDate birthDate, String nationality, String applyField, String profileImageUrl, String address, String specialty, String hobbies) {
        this.id = (id != null) ? id : UUID.randomUUID().toString();
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
}
