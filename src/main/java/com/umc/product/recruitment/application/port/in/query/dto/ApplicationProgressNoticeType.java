package com.umc.product.recruitment.application.port.in.query.dto;

public enum ApplicationProgressNoticeType {
    APPLY_DEADLINE, // 지원 마감 예정일
    DOC_RESULT_ANNOUNCE, // 서류 합불 발표 예정일
    FINAL_RESULT_ANNOUNCE, // 최종 합불 발표 예정일
    NEXT_RECRUITMENT_EXPECTED, // 불합격 시 다음 기수 모집 예정일 (xx년 xx월)
    CHALLENGER_NOTICE_IN_APP // 최종 합격자 전용 문구
}
