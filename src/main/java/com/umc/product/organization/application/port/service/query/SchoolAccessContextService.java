package com.umc.product.organization.application.port.service.query;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.GetSchoolAccessContextUseCase;
import com.umc.product.organization.application.port.in.query.dto.SchoolAccessContext;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SchoolAccessContextService implements GetSchoolAccessContextUseCase {

    private final GetMemberUseCase getMemberUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;

    @Override
    public SchoolAccessContext getContext(Long memberId) {
        // 1. 회원 정보 조회 (schoolId 획득)
        MemberInfo memberInfo = getMemberUseCase.getMemberInfoById(memberId);
        Long schoolId = memberInfo.schoolId();

        // 2. 현재 활성 기수 조회
        Long activeGisuId = getGisuUseCase.getActiveGisuId();

        // 3. 학교 운영진 여부 확인 (학교 운영진이 아니면 접근 불가)
        if (!getChallengerRoleUseCase.isSchoolAdminInGisu(memberId, activeGisuId, schoolId)) {
            throw new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_ACCESS_DENIED);
        }

        // 4. 파트 제한 적용
        // 회장/부회장(isSchoolCore): 모든 파트 조회 가능 (part = null)
        // 파트장/기타 운영진: 본인 담당 파트만 조회 가능
        ChallengerPart part = getChallengerRoleUseCase.isSchoolCoreInGisu(memberId, activeGisuId, schoolId)
            ? null
            : getChallengerRoleUseCase.getResponsiblePartsByMemberAndGisu(memberId, activeGisuId)
                .stream().findFirst().orElse(null);

        return new SchoolAccessContext(schoolId, part);
    }
}
