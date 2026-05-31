package com.umc.product.project.application.port.out;

import com.umc.product.project.domain.ProjectApplicationFormPolicy;
import java.util.List;

/**
 * ProjectApplicationFormPolicy 조회 Port (Driven / Port Out).
 */
public interface LoadProjectApplicationFormPolicyPort {

    /**
     * 특정 지원 폼에 속한 모든 섹션 정책을 조회합니다.
     */
    List<ProjectApplicationFormPolicy> listByApplicationFormId(Long applicationFormId);
}
