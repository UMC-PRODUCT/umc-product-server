package com.umc.product.project.application.port.out;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.ProjectPartQuota;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ProjectPartQuota 조회 Port (Driven / Port Out).
 */
public interface LoadProjectPartQuotaPort {

    List<ProjectPartQuota> listByProjectId(Long projectId);

    /** 해당 프로젝트가 특정 파트를 모집 중인지 (정원 등록 여부) 확인합니다. */
    boolean existsByProjectIdAndPart(Long projectId, ChallengerPart part);

    /**
     * 여러 프로젝트의 partQuota 를 한 번에 조회해 projectId 기준 Map 으로 반환한다 (N+1 방지).
     * <p>
     * 호출자는 {@code quota.getProject().getId()} 로 프록시에 접근할 필요 없이 키로 그룹을 얻는다.
     */
    Map<Long, List<ProjectPartQuota>> listByProjectIdsGroupedByProjectId(Collection<Long> projectIds);
}
