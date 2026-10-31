package com.umc.product.member.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.member.application.port.in.query.CheckNicknameAvailabilityUseCase;
import com.umc.product.member.application.port.in.query.dto.NicknameAvailabilityInfo;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.organization.application.port.in.query.CheckSchoolExistenceUseCase;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NicknameAvailabilityQueryService implements CheckNicknameAvailabilityUseCase {

    private final LoadMemberPort loadMemberPort;
    private final CheckSchoolExistenceUseCase checkSchoolExistenceUseCase;

    @Override
    public NicknameAvailabilityInfo check(Long schoolId, String nickname) {
        if (!checkSchoolExistenceUseCase.existsById(schoolId)) {
            throw new OrganizationDomainException(OrganizationErrorCode.SCHOOL_NOT_FOUND);
        }
        return NicknameAvailabilityInfo.of(!loadMemberPort.existsNicknameInSchool(schoolId, nickname));
    }
}
