package com.umc.product.project.domain.enums;

/**
 * 프로젝트의 파트별 TO 상태를 나타냅니다.
 * <p>
 * DB에 저장되지 않는 <b>실시간 계산값</b>입니다.
 * {@code currentCount}와 {@code quota}를 비교하여 서비스 단에서 산출합니다.
 */
public enum PartQuotaStatus {
    RECRUITING, // currentCount < quota — 아직 모집 중 (자리 있음)
    COMPLETED,  // currentCount >= quota — 다 찼음 (모집 완료)
}
