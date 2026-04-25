package com.umc.product.notice.domain.enums;

/**
 * 공지사항 조회 탭 구분.
 * 클라이언트가 어떤 탭을 보고 있는지를 나타내며, 각 탭마다 다른 조회 로직이 적용됩니다.
 */
public enum NoticeTab {
    CHALLENGER,    // 일반 공지 (챌린저 대상, 분류 필터 적용)
    CENTRAL_STAFF, // 중앙운영진 공지 (조회자의 중앙 역할 기반 자동 필터링)
    SCHOOL_STAFF   // 교내운영진 공지 (조회자의 교내 역할 + 소속 학교 기반 자동 필터링)
}
