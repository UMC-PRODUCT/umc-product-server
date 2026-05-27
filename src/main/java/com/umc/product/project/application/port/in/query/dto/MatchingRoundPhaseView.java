package com.umc.product.project.application.port.in.query.dto;

import com.umc.product.project.domain.enums.MatchingPhase;

/**
 * 매칭 라운드 차수를 화면에 노출하기 위한 표시용 enum.
 * <p>
 * 도메인 enum {@link MatchingPhase} 는 {@code ProjectMatchingRound} 엔티티 컬럼이 가지는 실제 차수 값(FIRST/SECOND/THIRD)만 표현해야 하므로, 라운드
 * 엔티티가 존재하지 않는 시나리오(예: 랜덤 매칭/운영진 강제 배정으로 합류한 {@code ProjectMember}) 의 라벨을 표현하려면 도메인 enum 을 흔들지 않고 별도 표시용 enum 을 둔다.
 * <p>
 * 본 enum 은 {@link ProjectApplicationViewStatus} 와 동일한 패턴(도메인 enum -> 표시용 enum) 으로, 응답 DTO 에서 도메인 enum 직접 노출을 막기 위한
 * 용도다.
 * <p>
 * 사용처:
 * <ul>
 *   <li>본인 지원 내역 카드 (APPLY-004) -- application 기반 카드는 {@link #from(MatchingPhase)} 로 도메인 phase 를 매핑하고,
 *       {@code ProjectMember.application=null} 인 랜덤 매칭/운영진 강제 배정 카드는 {@link #RANDOM_MATCHING} 으로 표시한다.</li>
 * </ul>
 */
public enum MatchingRoundPhaseView {
    FIRST,
    SECOND,
    THIRD,
    /**
     * THIRD 라운드 종료 후 자동 랜덤 매칭 또는 운영진 강제 배정으로 합류한 케이스.
     * <p>
     * 도메인상 두 케이스는 현재 구분되지 않으므로({@code ProjectMember.application=null} 시맨틱으로 통합) 본 라벨이 둘 다 포함한다. 향후 도메인 보강 (예:
     * assignmentSource enum 추가) 시 분리될 수 있다.
     */
    RANDOM_MATCHING;

    public static MatchingRoundPhaseView from(MatchingPhase phase) {
        return switch (phase) {
            case FIRST -> FIRST;
            case SECOND -> SECOND;
            case THIRD -> THIRD;
        };
    }
}
