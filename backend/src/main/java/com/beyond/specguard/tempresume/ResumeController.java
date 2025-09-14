package com.beyond.specguard.tempresume;

import com.beyond.specguard.applicant.model.service.ApplicantDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyInfo(
            @AuthenticationPrincipal ApplicantDetails applicantDetails
    ) {
        UUID templateId = applicantDetails.getResume().getTemplateId();
        String email = applicantDetails.getUsername();

        // DB 조회하거나 바로 반환
        return ResponseEntity.ok(Map.of(
                "email", email,
                "templateId", templateId
        ));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    // Resume 생성 (로그인 필요)
    @PostMapping
    public ResponseEntity<ResumeResponseDto> createResume(@RequestBody @Valid ResumeRequestDto request,
                                                          HttpServletRequest httpRequest) {

        ResumeResponseDto response = resumeService.createResume(request);
        return ResponseEntity.ok(response);
    }

    // Resume 조회 (로그인 필요)
    @GetMapping("/")
    public ResponseEntity<ResumeResponseDto> getResume(
            @AuthenticationPrincipal ApplicantDetails applicantDetails
    ) {
        String email = applicantDetails.getUsername();
        UUID templateId = applicantDetails.getResume().getTemplateId();

        ResumeResponseDto response = resumeService.getResume(templateId, email);
        return ResponseEntity.ok(response);
    }
}