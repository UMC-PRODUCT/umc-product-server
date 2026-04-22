package com.umc.product.project.application.port.out;

import com.umc.product.project.domain.ProjectPartQuota;
import java.util.List;

/**
 * ProjectPartQuota 조회 Port (Driven / Port Out).
 * <p>
 * Phase 1 범위: 단건 프로젝트의 파트 TO 목록 조회.
 * 파트 TO 생성/수정은 Phase 2의 PROJECT-105 UpdatePartQuotas에서 다룹니다.
 */
public interface LoadProjectPartQuotaPort {

    List<ProjectPartQuota> listByProjectId(Long projectId);
}
