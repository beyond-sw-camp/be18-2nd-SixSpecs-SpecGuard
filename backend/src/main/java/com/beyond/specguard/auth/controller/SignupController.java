package com.beyond.specguard.auth.controller;

import com.beyond.specguard.auth.dto.SignupRequestDTO;
import com.beyond.specguard.auth.dto.SignupResponseDTO;
import com.beyond.specguard.auth.service.SignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/signup")
@RequiredArgsConstructor
public class SignupController {

    private final SignupService signupService;

    @PostMapping("/user")
    /*
         회원가입 요청을 처리하는 메서드.
        1. 클라이언트가 보낸 HTTP 요청 Body(JSON)를 @RequestBody를 통해 SignupRequestDTO로 변환.
        2. @Valid로 DTO의 유효성 검증 수행.
        3. 변환·검증된 DTO를 서비스 계층(signupService.signup)에 전달해 회원가입 로직 실행.
        4. 서비스에서 반환한 SignupResponseDTO를 HTTP 200 OK 응답으로 클라이언트에 반환.
     */
    public ResponseEntity<SignupResponseDTO> signup(@Valid @RequestBody SignupRequestDTO request) {
        SignupResponseDTO response = signupService.signup(request);
        return ResponseEntity.ok(response);
    }
}
