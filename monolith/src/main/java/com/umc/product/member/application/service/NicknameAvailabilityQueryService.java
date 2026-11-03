package com.umc.product.member.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.member.application.port.in.query.CheckNicknameAvailabilityUseCase;
import com.umc.product.member.application.port.in.query.dto.NicknameAvailabilityInfo;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.application.port.out.LoadNicknameReservationPort;
import com.umc.product.member.domain.NicknamePolicy;
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
    private final LoadNicknameReservationPort loadNicknameReservationPort;
    private final MemberGraduationQueryService graduationQueryService;

    @Override
    public NicknameAvailabilityInfo check(Long schoolId, String nickname) {
        NicknamePolicy.validate(nickname);
        if (!checkSchoolExistenceUseCase.existsById(schoolId)) {
            throw new OrganizationDomainException(OrganizationErrorCode.SCHOOL_NOT_FOUND);
        }
        if (loadMemberPort.existsNonWithdrawnNicknameInSchool(schoolId, nickname)
            || graduationQueryService.hasGraduatedHistory(
                loadMemberPort.listWithdrawnIdsBySchoolAndNickname(schoolId, nickname))) {
            return NicknameAvailabilityInfo.of(false);
        }
        // 회원 삭제와 예약 저장은 같은 트랜잭션이다. 회원 조회 이후 예약을 확인해 전환 중의 누락을 막는다.
        return NicknameAvailabilityInfo.of(!loadNicknameReservationPort.isReserved(schoolId, nickname));
    }
}
