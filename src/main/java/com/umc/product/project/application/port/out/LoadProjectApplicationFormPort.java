package com.umc.product.project.application.port.out;

/**
 * ProjectApplicationForm 조회 Port (Driven / Port Out).
 */
public interface LoadProjectApplicationFormPort {

    /**
     * 특정 프로젝트에 연결된 지원 폼이 하나라도 존재하는지 확인합니다.
     * PROJECT-107 submit 검증에 사용됩니다.
     */
    boolean existsByProjectId(Long projectId);
}
