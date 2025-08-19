package com.beyond.specguard.auth.service;

import com.beyond.specguard.auth.dto.SignupRequestDTO;
import com.beyond.specguard.auth.dto.SignupResponseDTO;
import com.beyond.specguard.auth.entity.UserEntity;
import com.beyond.specguard.auth.repository.UserRepository;
import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.common.exception.errorcode.AuthErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupResponseDTO signup(SignupRequestDTO request) {

        // 1. 이메일 중복 체크 (비즈니스 검증)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        // 2. 엔티티 생성
        UserEntity user = UserEntity.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(UserEntity.Role.APPLICANT)
                .build();

        // 3. 저장
        UserEntity savedUser = userRepository.save(user);

        // 4. 응답 DTO 변환
        return SignupResponseDTO.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .role(savedUser.getRole().name())
                .createdAt(savedUser.getCreatedAt().toString())
                .build();
    }
}
