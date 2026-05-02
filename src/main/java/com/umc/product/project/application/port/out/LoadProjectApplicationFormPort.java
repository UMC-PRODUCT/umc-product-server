package com.umc.product.project.application.port.out;

import com.umc.product.project.domain.ProjectApplicationForm;
import java.util.Optional;

/**
 * ProjectApplicationForm 조회 Port (Driven / Port Out).
 */
public interface LoadProjectApplicationFormPort {

    /**
     * 특정 프로젝트에 연결된 지원 폼이 하나라도 존재하는지 확인합니다.
     * PROJECT-107 submit 검증에 사용됩니다.
     */
    boolean existsByProjectId(Long projectId);

    /**
     * 특정 프로젝트에 연결된 지원 폼을 조회합니다.
     * <p>
     * 운영상 프로젝트당 폼 1개로 사용하지만 스키마는 1:N 을 허용하므로 가장 먼저 생성된 row 를 반환합니다.
     */
    Optional<ProjectApplicationForm> findByProjectId(Long projectId);
}
