package com.beyond.specguard.company.management.controller;

import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.company.common.model.repository.ClientUserRepository;
import com.beyond.specguard.company.common.model.service.CustomUserDetails;
import com.beyond.specguard.company.management.model.dto.request.ChangePasswordRequestDto;
import com.beyond.specguard.company.management.model.dto.request.UpdateCompanyRequestDto;
import com.beyond.specguard.company.management.model.dto.request.UpdateUserRequestDto;
import com.beyond.specguard.company.common.model.dto.response.SignupResponseDto;
import com.beyond.specguard.company.management.model.service.CompanyService;
import com.beyond.specguard.company.management.model.service.UserService;
import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.auth.exception.errorcode.AuthErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CompanyService companyService;
    private final ClientUserRepository clientUserRepository;

    @GetMapping("/company/{slug}/users/me")
    public ResponseEntity<SignupResponseDto> getMyInfo(
            @PathVariable String slug,
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getMyInfo(slug, userDetails));
    }



    //회원 정보 수정(이름, 전화번호)
    @PatchMapping("/me")
    public ResponseEntity<SignupResponseDto.UserDTO> updateMyInfo(@Valid @RequestBody UpdateUserRequestDto request, Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(userService.updateMyInfo(userDetails.getUser().getId(), request));
    }

    //회원 전화번호 수정
    @PatchMapping("/me/password")
    public ResponseEntity<Map<String, String >> changePassword(@Valid @RequestBody ChangePasswordRequestDto request, Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        userService.changePassword(userDetails.getUser().getId(), request);
        return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다"));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deleteMyAccount(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        userService.deleteMyAccount(userDetails.getUser().getId());
        return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 완료되었습니다."));
    }

    @PatchMapping("company/{slug}")
    public ResponseEntity<SignupResponseDto.CompanyDTO> updateCompany(
            @PathVariable String slug,
            @Valid @RequestBody UpdateCompanyRequestDto request,
            Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        SignupResponseDto.CompanyDTO updated = companyService.updateCompany(slug, request, userDetails.getUser().getId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("company/{slug}")
    public ResponseEntity<Map<String,String>> deleteCompany(
            @PathVariable String slug,
            Authentication authentication
    ){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        companyService.deleteCompany(slug, userDetails.getUser().getId());
        return ResponseEntity.ok(Map.of("message", "회사가 성공적으로 삭제 되었습니다"));
    }

}
