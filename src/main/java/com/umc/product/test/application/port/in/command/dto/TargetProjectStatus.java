package com.umc.product.test.application.port.in.command.dto;

/**
 * 시나리오 시딩이 도달 가능한 프로젝트 상태 집합.
 * <p>
 * {@code COMPLETED} / {@code ABORTED} 는 기수 종료 / 운영진 abort 등 별도 도메인 액션이 필요해
 * 시나리오 시딩 범위 밖이다.
 */
public enum TargetProjectStatus {
    DRAFT,
    PENDING_REVIEW,
    IN_PROGRESS
}
