package com.umc.product.curriculum.domain.enums;

/**
 * 원본 워크북의 상태를 나타내는 열거형
 *
 * <pre>
 * 상태 전환 행렬:
 *   DRAFT   → READY    ✅ (배포 준비 등록)
 *   DRAFT   → RELEASED ❌ (READY 경유 필수)
 *   READY   → RELEASED ✅ (수동/자동 배포)
 *   READY   → DRAFT    ✅ (임시저장으로 롤백)
 *   RELEASED → any     ❌ (배포 완료 후 되돌리기 불가)
 * </pre>
 */
public enum OriginalWorkbookStatus {
    DRAFT,
    READY,
    RELEASED;

    /**
     * 현재 상태에서 목표 상태로 전환이 가능한지 확인합니다.
     *
     * @param target 전환하려는 목표 상태
     * @return 전환 가능 여부
     */
    public boolean canTransitionTo(OriginalWorkbookStatus target) {
        return switch (this) {
            case DRAFT -> target == READY;
            case READY -> target == RELEASED || target == DRAFT;
            case RELEASED -> false;
        };
    }

    /**
     * 배포 완료(RELEASED) 상태인지 확인합니다. RELEASED 상태에서는 어떠한 상태 변경도 불가합니다.
     */
    public boolean isReleased() {
        return this == RELEASED;
    }
}
