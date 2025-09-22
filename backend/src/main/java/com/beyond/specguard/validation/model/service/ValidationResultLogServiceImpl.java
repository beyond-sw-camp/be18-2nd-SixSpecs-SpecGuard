package com.beyond.specguard.validation.model.service;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.common.exception.errorcode.CommonErrorCode;
import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.validation.exception.errorcode.ValidationErrorCode;
import com.beyond.specguard.validation.model.dto.response.ValidationResultLogResponseDto;
import com.beyond.specguard.validation.model.entity.ValidationResultLog;
import com.beyond.specguard.validation.model.repository.ValidationResultLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationResultLogServiceImpl implements ValidationResultLogService{
    private final ValidationResultLogRepository validationResultLogRepository;

    private void validateReadRole(ClientUser.Role role) {
        if (!EnumSet.of(ClientUser.Role.VIEWER, ClientUser.Role.OWNER, ClientUser.Role.MANAGER).contains(role)) {
            throw new CustomException(ValidationErrorCode.FORBIDDEN);
        }
    }





    @Override @Transactional(readOnly = true)
    public List<ValidationResultLogResponseDto> getLogsByResumeId(ClientUser clientUser, UUID resumeId) {
        validateReadRole(clientUser.getRole());
        return validationResultLogRepository.findAllDtosByResumeId(resumeId);
    }


}
