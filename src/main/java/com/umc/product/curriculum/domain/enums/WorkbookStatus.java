package com.umc.product.curriculum.domain.enums;

public enum WorkbookStatus {
    NOT_RELEASED,  // 워크북 미배포 (커리큘럼만 있음)
    PENDING,       // 배포됨, 미제출
    SUBMITTED,     // 제출됨, 심사 대기
    PASS,          // 통과
    FAIL           // 불합격
}
