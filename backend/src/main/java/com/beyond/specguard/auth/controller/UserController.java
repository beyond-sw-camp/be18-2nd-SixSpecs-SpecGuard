package com.beyond.specguard.auth.controller;

import com.beyond.specguard.auth.dto.SignupResponseDto;
import com.beyond.specguard.auth.service.CustomUserDetails;
import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.common.exception.errorcode.AuthErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/")
public class UserController {

    @GetMapping("/companies/{slug}/users/me")
    public ResponseEntity<SignupResponseDto.UserDTO> getMyInfo(@PathVariable String slug, Authentication authentication) {
        // 인증된 유저 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // ✅ slug 검증
        if (!userDetails.getCompany().getSlug().equals(slug)) {
            throw new CustomException(AuthErrorCode.ACCESS_DENIED);
        }

        return ResponseEntity.ok(SignupResponseDto.UserDTO.from(userDetails.getUser()));
    }
}
