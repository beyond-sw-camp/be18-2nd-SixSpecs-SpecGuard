package com.beyond.specguard.company.management.model.service;

import com.beyond.specguard.auth.exception.errorcode.AuthErrorCode;
import com.beyond.specguard.company.common.model.service.CustomUserDetails;
import com.beyond.specguard.company.management.model.dto.request.ChangePasswordRequestDto;
import com.beyond.specguard.company.management.model.dto.request.UpdateUserRequestDto;
import com.beyond.specguard.company.common.model.dto.response.SignupResponseDto;
import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.company.common.model.repository.ClientUserRepository;
import com.beyond.specguard.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final ClientUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public SignupResponseDto getMyInfo(String slug, CustomUserDetails userDetails) {
        // slug 검증
        if (!userDetails.getCompany().getSlug().equals(slug)) {
            throw new CustomException(AuthErrorCode.ACCESS_DENIED);
        }

        SignupResponseDto.SignupResponseDtoBuilder builder = SignupResponseDto.builder()
                .user(SignupResponseDto.UserDTO.from(userDetails.getUser()));

        // 오너일 경우 회사 + 직원 목록 추가
        if (userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"))) {

            List<ClientUser> employees = userRepository.findAllByCompany_Slug(slug)
                    .stream()
                    .filter(u -> !u.getRole().equals(ClientUser.Role.OWNER))
                    .toList();

            builder.company(SignupResponseDto.CompanyDTO.from(userDetails.getCompany()))
                    .employees(employees.stream()
                            .map(SignupResponseDto.UserDTO::from)
                            .toList());
        }

        return builder.build();
    }


    @Transactional
    public SignupResponseDto.UserDTO updateMyInfo(UUID userid, UpdateUserRequestDto dto){
        ClientUser clientUser = userRepository.findById(userid)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));
        clientUser.update(dto);
        return SignupResponseDto.UserDTO.from(clientUser);
    }


    @Transactional
    public void changePassword(UUID userid, ChangePasswordRequestDto dto){
        ClientUser clientUser = userRepository.findById(userid)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        if(!"local".equals(clientUser.getProvider())){
            throw new CustomException(AuthErrorCode.PASSWORD_CHANGE_NOT_ALLOWED);
        }
        if(!passwordEncoder.matches(dto.getOldPassword(), clientUser.getPasswordHash())){
            throw new CustomException(AuthErrorCode.INVALID_PASSWORD);
        }
        clientUser.changePassword(passwordEncoder.encode(dto.getNewPassword()));
    }

    public void deleteMyAccount(UUID id) {
        ClientUser clientUser = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        if(clientUser.getRole() == ClientUser.Role.OWNER){
            boolean isLastOwner = !userRepository.existsByCompanyIdAndRoleAndIdNot(
                    clientUser.getCompany().getId(),
                    ClientUser.Role.OWNER,
                    clientUser.getId()
            );
            if(isLastOwner){
                throw new CustomException(AuthErrorCode.LAST_OWNER_CANNOT_DELETE);
            }
        }
        userRepository.delete(clientUser);
    }
}
