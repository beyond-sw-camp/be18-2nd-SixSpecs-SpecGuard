package com.beyond.specguard.auth.model.provider;

import com.beyond.specguard.applicant.model.dto.ApplicantLoginRequestDto;
import com.beyond.specguard.applicant.model.service.ApplicantDetails;
import com.beyond.specguard.auth.model.token.ApplicantAuthenticationToken;
import com.beyond.specguard.resume.entity.core.Resume;
import com.beyond.specguard.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ApplicantAuthenticationProvider implements AuthenticationProvider {

    private final ResumeRepository resumeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) {
        ApplicantLoginRequestDto loginDto = (ApplicantLoginRequestDto) authentication.getDetails();

        UUID templateUuid = loginDto.getTemplateId();
        String email = loginDto.getEmail();
        String password = loginDto.getPassword();

        Resume applicant = resumeRepository.findByEmailAndTemplateId(email, templateUuid)
                .orElseThrow(() -> new BadCredentialsException("이메일 또는 템플릿이 올바르지 않습니다."));

        if (!passwordEncoder.matches(password, applicant.getPasswordHash())) {
            throw new BadCredentialsException("비밀번호가 올바르지 않습니다.");
        }

        ApplicantDetails applicantDetails = new ApplicantDetails(applicant);

        // ROLE_APPLICANT 권한 부여
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_APPLICANT"));

        return new ApplicantAuthenticationToken(applicantDetails, null, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ApplicantAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
