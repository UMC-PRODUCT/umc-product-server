package com.umc.product.project.application.port.in.query.dto;

/**
 * 프로젝트 팀원 일괄 조회의 명단 기준.
 */
public enum ProjectMemberView {
    /** 공개된 팀원 명단. */
    PUBLISHED,

    /**
     * 랜덤 매칭 이후 운영진이 최종 공개할 팀원 구성을 확인하는 조회 방식.
     * 조정 명단을 보여줄 시점이 아니면 공개 명단을 반환하며, 모든 프로젝트에 조정 권한이 필요합니다.
     */
    ADJUSTMENT
}
