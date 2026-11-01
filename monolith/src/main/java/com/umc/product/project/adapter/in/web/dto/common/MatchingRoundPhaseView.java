package com.umc.product.project.adapter.in.web.dto.common;

import com.umc.product.project.domain.enums.MatchingPhase;

/**
 * 매칭 라운드 차수를 화면에 노출할 때 쓰는 표시용 enum.
 * <p>
 * 도메인 enum {@link MatchingPhase} 는 실제 라운드 엔티티가 갖는 차수(FIRST/SECOND/THIRD/RANDOM_MATCHING)를 표현한다.
 * 라운드 엔티티가 없는 경우(예: 운영진이 직접 추가한 {@code ProjectMember})도 화면에는 라벨을 보여줘야 해서,
 * web 응답 전용으로 별도 enum 을 둔다. 랜덤 매칭과 지원서 없이 추가한 멤버는 RANDOM_MATCHING 으로 표시한다.
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
     * 자동 랜덤 매칭 또는 운영진이 직접 추가한 케이스.
     * <p>
     * 본인 지원 내역에서는 두 케이스 모두 {@code ProjectMember.application=null} 기준으로 본 라벨에 포함한다.
     * 향후 멤버 추가 경로별 표시가 필요하면 분리할 수 있다.
     */
    RANDOM_MATCHING;

    public static MatchingRoundPhaseView from(MatchingPhase phase) {
        return switch (phase) {
            case FIRST -> FIRST;
            case SECOND -> SECOND;
            case THIRD -> THIRD;
            case RANDOM_MATCHING -> RANDOM_MATCHING;
        };
    }
}
