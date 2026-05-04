package com.umc.product.project.application.port.out;

import com.umc.product.project.domain.ProjectPartQuota;
import java.util.List;

/**
 * ProjectPartQuota 조회 Port (Driven / Port Out).
 */
public interface LoadProjectPartQuotaPort {

    List<ProjectPartQuota> listByProjectId(Long projectId);
}
