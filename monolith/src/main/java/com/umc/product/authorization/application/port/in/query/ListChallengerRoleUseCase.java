package com.umc.product.authorization.application.port.in.query;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleBasicInfo;
import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleInfo;
import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;

/**
 * ChallengerRole 조회 전용 UseCase입니다.
 */
public interface ListChallengerRoleUseCase {

    ChallengerRoleInfo getById(Long challengerRoleId);

    List<ChallengerRoleInfo> listByMemberId(Long memberId);

    List<ChallengerRoleBasicInfo> listBasicByMemberId(Long memberId);

    List<ChallengerRoleInfo> listByMemberIdAndGisuId(Long memberId, Long gisuId);

    List<ChallengerRoleInfo> listByChallengerIdsAndGisuId(Set<Long> challengerIds, Long gisuId);

    Map<Long, List<ChallengerRoleType>> mapRoleTypesByChallengerIds(Set<Long> challengerIds);

    Set<ChallengerPart> listResponsiblePartsByMemberIdAndGisuId(Long memberId, Long gisuId);

    /** 대상 기수의 역할과 기수에 관계없이 열람 가능한 총괄단 역할을 회원별로 조회한다. 기수가 없으면 전체 역할을 조회한다. */
    Map<Long, List<RoleAttribute>> mapRolesByMemberIncludingCentralCore(Long gisuId);
}
