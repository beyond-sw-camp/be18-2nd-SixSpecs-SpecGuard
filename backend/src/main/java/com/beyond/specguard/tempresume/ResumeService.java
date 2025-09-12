package com.beyond.specguard.tempresume;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ResumeResponseDto createResume(ResumeRequestDto dto) {
        resumeRepository.findByEmailAndTemplateId(dto.getEmail(), dto.getTemplateId())
                .ifPresent(r -> { throw new RuntimeException("이미 존재하는 이력서입니다."); });

        Resume resume = Resume.builder()
                .templateId(dto.getTemplateId())
                .name(dto.getName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .status(Resume.ResumeStatus.DRAFT)
                .build();

        resumeRepository.saveAndFlush(resume);
        return toDto(resume);
    }

    public ResumeResponseDto getResume(UUID id, String email) {
        Resume resume = resumeRepository.findByEmailAndTemplateId(email, id)
                .orElseThrow(() -> new RuntimeException("이력서를 찾을 수 없습니다."));
        return toDto(resume);
    }

    private ResumeResponseDto toDto(Resume resume) {
        return ResumeResponseDto.builder()
                .id(resume.getId().toString())
                .templateId(resume.getTemplateId().toString())
                .name(resume.getName())
                .phone(resume.getPhone())
                .email(resume.getEmail())
                .status(resume.getStatus().name())
                .build();
    }
}