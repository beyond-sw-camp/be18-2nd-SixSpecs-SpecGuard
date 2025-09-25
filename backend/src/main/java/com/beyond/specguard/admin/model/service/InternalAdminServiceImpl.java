package com.beyond.specguard.admin.model.service;

import com.beyond.specguard.admin.model.dto.request.InternalAdminRequestDto;
import com.beyond.specguard.admin.model.dto.response.InternalAdminResponseDto;
import com.beyond.specguard.admin.model.entity.InternalAdmin;
import com.beyond.specguard.admin.model.repository.InternalAdminRepository;
import com.beyond.specguard.auth.exception.errorcode.AuthErrorCode;
import com.beyond.specguard.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InternalAdminServiceImpl implements InternalAdminService {
    private final InternalAdminRepository internalAdminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public InternalAdminResponseDto createAdmin(InternalAdminRequestDto request) {

        // 중복 여부 확인
        if (internalAdminRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        // 저장 전 패스워드 인코딩
        request.encodePassword(passwordEncoder);

        InternalAdmin admin = request.toEntity();

        return InternalAdminResponseDto.fromEntity(internalAdminRepository.save(admin));

    }
}
