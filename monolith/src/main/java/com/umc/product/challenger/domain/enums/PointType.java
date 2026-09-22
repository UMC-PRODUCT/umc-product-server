package com.umc.product.challenger.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PointType {
    // 과거 기록의 타입과 배점을 해석하므로 프론트의 신규 선택지에서 숨긴 뒤에도 유지한다.
    // 제거를 검토하려면 구형 입력 계약 종료와 과거 점수를 보존하는 DB 기록 및 클라이언트 참조 이관이 선행되어야 한다.
    // JPA가 문자열 enum으로 읽으므로 기존 DB 참조가 남아 있을 때 삭제하면 과거 기록 조회가 실패한다.
    BEST_WORKBOOK(-0.5),
    WARNING(0.0),
    OUT(1.0),

    // 10기부터 사용하는 개편 배점. BEST_WORKBOOK의 신규 부여 제한은 서비스에서 별도로 검증한다.

    CUSTOM(0.0), // 가천대 등 자체 제도 운영하는 곳

    // 긍정 포인트
    BLOG_CHALLENGE(3.0),          // 블로그 챌린지 참여 (매주 최대 +3)
    BEST_WORKBOOK_V2(2.0),        // 베스트 워크북 선정 (매주 각 스터디당 1명)
    UMC_EVENT_REVIEW(1.0),        // 행사 리뷰어 (구글폼 제출 확인 시)
    PEER_REVIEW_SUBMISSION(1.0),  // PeerReview 작성 (매주 최대 +1)

    // 과제 관련
    NO_WORKBOOK_MISSION(-4.0),    // 과제 미수행 (스터디 시작 전날 23:59까지)

    // 스터디 관련
    STUDY_LATE(-2.0),             // 스터디 무단 지각 (시작 ~ 10분 경과)
    STUDY_ABSENT(-4.0),           // 스터디 무단 불참 (시작 10분 이후)

    // 행사 관련
    EVENT_LATE(-2.0),             // 행사 무단 지각
    EVENT_EARLY_LEAVE(-2.0),      // 행사 중도 퇴실
    EVENT_LATE_CANCEL(-4.0),      // 행사 기간 외 취소
    EVENT_NO_SHOW(-10.0),         // 노쇼 (무단 결석)

    // 피드백 관련
    PART_LEAD_FEEDBACK_LATE(-4.0),          // 기간 외 피드백

    // 회의/업무 관련
    SCHOOL_CORE_MEETING_ABSENT(-4.0),         // 회의 무단 불참
    SCHOOL_CORE_TASK_NOT_COMPLETED(-4.0),     // 업무 무단 불이행
    ;

    private final double value;
}
