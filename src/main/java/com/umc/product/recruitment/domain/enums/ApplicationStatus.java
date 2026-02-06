package com.umc.product.recruitment.domain.enums;

public enum ApplicationStatus {
    APPLIED,              // 제출 완료(지원 완료) (결정 전 기본 상태)
    DOC_PASSED,           // 서류 합격
    DOC_FAILED,           // 서류 불합격 (현재 미사용)
    // 현재 UI에 불합격 처리 버튼이 없어 결과 발표 단계에서 PASS 미처리 시 "불합격"으로 계산하여 표시함
    INTERVIEW_SCHEDULED,  // 면접 배정 완료. 슬롯 배정용 상태, 사용자 노출 없음
    INTERVIEW_PASSED,     // 면접 합격 (현재 미사용)
    INTERVIEW_FAILED,     // 면접 불합격 (현재 미사용)
    FINAL_ACCEPTED,       // 최종 합격(선발 확정)
    FINAL_REJECTED,       // 최종 불합격 (현재 미사용)
    // 현재 FINAL_ACCEPTED 미처리 + 결과 발표 단계에서 "불합격"으로 계산하여 표시함
    WITHDRAWN             // 지원 철회(사용자/운영진) (현재 미사용)
}