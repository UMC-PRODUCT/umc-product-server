package com.umc.product.curriculum.domain.enums;

public enum WorkbookStatus {
    PENDING,       // 배포됨, 미제출
    SUBMITTED,     // 제출됨, 심사 대기
    PASS,          // 통과
    FAIL,          // 불합격
    BEST           // 베스트
}
