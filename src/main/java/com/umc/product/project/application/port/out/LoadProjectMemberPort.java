package com.umc.product.project.application.port.out;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.ProjectMember;
import java.util.List;
import java.util.Map;

/**
 * ProjectMember 조회 Port (Driven / Port Out).
 */
// TODO: ManageProjectMember 구현 시 SaveProjectMemberPort 추가
public interface LoadProjectMemberPort {

    /**
     * 특정 프로젝트의 특정 파트에 속한 멤버 목록을 조회합니다.
     * PLAN 파트 조회 시 보조 PM(coPM) 추출에 사용됩니다.
     */
    List<ProjectMember> listByProjectIdAndPart(Long projectId, ChallengerPart part);

    /**
     * 특정 프로젝트의 파트별 활성 멤버 수를 집계합니다.
     * {@code PartQuotaInfo.currentCount} 계산에 사용됩니다.
     *
     * @return 파트 → 인원수 맵. 활성 멤버가 없는 파트는 0 또는 엔트리 없음
     */
    Map<ChallengerPart, Long> countByProjectIdGroupByPart(Long projectId);
}
