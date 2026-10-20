package com.umc.product.project.adapter.in.web.dto.common;

import com.umc.product.project.domain.enums.MatchingPhase;

/**
 * 매칭 라운드 차수를 화면에 노출할 때 쓰는 표시용 enum.
 * <p>
 * 1~3차는 해당 차수로, 4차와 지원서 없는 배정은 RANDOM_MATCHING 으로 표시한다.
 * <p>
 * 사용처:
 * <ul>
 *   <li>APPLY-004 본인 지원 내역 카드 — 일반 카드는 {@link #from(MatchingPhase)} 로 매핑, 랜덤 매칭 카드는 {@link #RANDOM_MATCHING} 표시</li>
 *   <li>APPLY-101 PM/운영진 지원자 목록 — 도메인 enum 직접 노출을 피하기 위해 본 enum 으로 통일</li>
 *   <li>APPLY-102 지원서 단건 상세 — 동상</li>
 * </ul>
 */
public enum MatchingRoundPhaseView {
    FIRST,
    SECOND,
    THIRD,
    /**
     * 4차 랜덤 매칭 또는 운영진 강제 배정으로 합류한 케이스.
     * <p>
     * 도메인상 두 케이스는 현재 구분되지 않으므로({@code ProjectMember.application=null} 시맨틱으로 통합) 본 라벨이 둘 다 포함한다.
     * 향후 도메인 보강 (예: assignmentSource enum 추가) 시 분리될 수 있다.
     */
    RANDOM_MATCHING;

    public static MatchingRoundPhaseView from(MatchingPhase phase) {
        return switch (phase) {
            case FIRST -> FIRST;
            case SECOND -> SECOND;
            case THIRD -> THIRD;
            case FOURTH -> RANDOM_MATCHING;
        };
    }
}
