package com.beyond.specguard.admin.model.service;

import com.beyond.specguard.admin.model.dto.InternalAdminRequestDto;
import com.beyond.specguard.admin.model.entity.InternalAdmin;
import com.beyond.specguard.admin.model.repository.InternalAdminRepository;
import com.beyond.specguard.auth.exception.errorcode.AuthErrorCode;
import com.beyond.specguard.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InternalAdminServiceImpl implements InternalAdminService {
    private final InternalAdminRepository internalAdminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public InternalAdmin createAdmin(InternalAdminRequestDto request) {

        if (internalAdminRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        // 저장 전 패스워드 인코딩
        request.encodePassword(passwordEncoder);

        InternalAdmin admin = request.toEntity();

        return internalAdminRepository.save(admin);

    }
}
