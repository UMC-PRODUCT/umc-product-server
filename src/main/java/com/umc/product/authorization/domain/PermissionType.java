package com.umc.product.authorization.domain;

/**
 * 리소스에 대한 권한 타입
 */
public enum PermissionType {
    READ,       // 조회
    WRITE,      // 생성/수정
    DELETE,     // 삭제
    APPROVE,    // 승인 (출석, 워크북 등)
    CHECK,      // 확인 (공지사항 조회현황 확인)
    MANAGE      // 관리 (운영진 전용)
}
