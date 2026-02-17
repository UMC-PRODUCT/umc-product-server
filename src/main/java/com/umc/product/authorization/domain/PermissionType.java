package com.umc.product.authorization.domain;

/**
 * 권한 유형을 나타내는 enum
 * <p>
 * - READ: 조회 권한
 * <p>
 * - WRITE: 생성/수정 권한
 * <p>
 * - DELETE: 삭제 권한
 * <p>
 * - APPROVE: 승인 권한 (출석, 워크북 등)
 * <p>
 * - CHECK: 확인 권한 (공지사항 조회현황 확인)
 */
public enum PermissionType {
    READ,       // 조회
    WRITE,      // 생성/수정
    // TODO: 생성과 수정은 권한을 분리할 것 (EDIT 권한을 추가하며, 추후 Evaluator에 반영할 것)
    EDIT,       // 수정
    DELETE,     // 삭제
    APPROVE,    // 승인 (출석, 워크북 등)
    CHECK,      // 확인 (공지사항 조회현황 확인)
    MANAGE      // 관리 (운영진 전용)
}
