package com.umc.product.authorization.application.port.in.query;

import com.umc.product.common.domain.enums.ChallengerRoleType;

/**
 * ChallengerRole 기반 권한 판정 전용 UseCase입니다.
 */
public interface CheckChallengerAuthorityUseCase {

    boolean isSuperAdmin(Long memberId);

    /**
     * 프로덕트 문의 답변·관리 가능 여부입니다. 시스템 역할 UMC_PRODUCT 또는 SUPER_ADMIN(전역 override)이면 true입니다.
     */
    boolean isUmcProduct(Long memberId);

    boolean hasRoleTypeInGisu(Long memberId, Long gisuId, ChallengerRoleType role);

    boolean hasAnyRoleTypeInGisu(Long memberId, Long gisuId, ChallengerRoleType... roles);

    boolean hasAllRoleTypeInGisu(Long memberId, Long gisuId, ChallengerRoleType... roles);

    boolean isCentralCoreInAnyGisu(Long memberId);

    boolean isCentralMemberInAnyGisu(Long memberId);

    boolean isSchoolCoreInAnyGisu(Long memberId, Long schoolId);

    boolean isSchoolAdminInAnyGisu(Long memberId, Long schoolId);

    boolean isChapterPresidentInAnyGisu(Long memberId, Long chapterId);

    boolean isCentralCoreInGisu(Long memberId, Long gisuId);

    boolean isCentralMemberInGisu(Long memberId, Long gisuId);

    boolean isSchoolCoreInGisu(Long memberId, Long gisuId, Long schoolId);

    boolean isSchoolAdminInGisu(Long memberId, Long gisuId, Long schoolId);

    boolean isChapterPresidentInGisu(Long memberId, Long gisuId, Long chapterId);
}
