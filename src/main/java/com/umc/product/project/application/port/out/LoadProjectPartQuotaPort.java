package com.umc.product.project.application.port.out;

import com.umc.product.project.domain.ProjectPartQuota;
import java.util.List;

/**
 * ProjectPartQuota 조회 Port (Driven / Port Out).
 */
// TODO: PROJECT-105 UpdatePartQuotas 구현 시 SaveProjectPartQuotaPort 추가
public interface LoadProjectPartQuotaPort {

    List<ProjectPartQuota> listByProjectId(Long projectId);
}
