package com.umc.product.project.application.port.out;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.ProjectPartQuota;
import java.util.List;

/**
 * ProjectPartQuota 조회 Port (Driven / Port Out).
 */
public interface LoadProjectPartQuotaPort {

    List<ProjectPartQuota> listByProjectId(Long projectId);

    /** 해당 프로젝트가 특정 파트를 모집 중인지 (정원 등록 여부) 확인합니다. */
    boolean existsByProjectIdAndPart(Long projectId, ChallengerPart part);
}
