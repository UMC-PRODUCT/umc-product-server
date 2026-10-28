package com.umc.product.project.application.port.in.query.dto;

/**
 * 프로젝트 팀원 명단 조회 기준. 매칭 현황 표(PROJECT-009)에서 사용합니다.
 */
public enum ProjectMemberView {
    /** 공개된 팀원 명단. */
    PUBLISHED,

    /**
     * 랜덤 매칭 이후 운영진이 최종 공개할 팀원 구성을 확인하는 조회 방식.
     * 조정 명단을 보여줄 시점이 아니면 공개 명단을 반환하며, 조회 대상 지부의 조정 권한이 필요합니다.
     */
    ADJUSTMENT
}
