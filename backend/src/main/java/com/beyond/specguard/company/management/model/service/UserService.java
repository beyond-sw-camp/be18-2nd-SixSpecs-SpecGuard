package com.beyond.specguard.company.management.model.service;

import com.beyond.specguard.auth.exception.errorcode.AuthErrorCode;
import com.beyond.specguard.company.management.model.dto.request.ChangePasswordRequestDto;
import com.beyond.specguard.company.management.model.dto.request.UpdateUserRequestDto;
import com.beyond.specguard.auth.model.dto.response.SignupResponseDto;
import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.auth.model.repository.ClientUserRepository;
import com.beyond.specguard.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final ClientUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
