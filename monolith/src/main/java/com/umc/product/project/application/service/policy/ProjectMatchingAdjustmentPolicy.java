package com.umc.product.project.application.service.policy;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

/**
 * 매칭 유형별 팀원 수동 배정·해제 가능 기간과 결과 반영 시점을 판단하는 정책.
 */
@Component
public class ProjectMatchingAdjustmentPolicy {

    /**
     * 팀원 수동 배정·해제 가능 여부를 검증한다.
     * <ul>
     *   <li>최종 발표에 일괄 반영해야 하는 경우: 결과를 공개할 랜덤 차수를 반환한다.</li>
     *   <li>즉시 반영 가능한 기간인 경우: 빈 Optional을 반환한다.</li>
     *   <li>변경할 수 없는 기간인 경우: 예외를 발생시킨다.</li>
     * </ul>
     * 변경할 수 없는 기간은 다음과 같다.
     * <ul>
     *   <li>1~3차 결정 마감부터 해당 결과 공개 완료 전까지</li>
     *   <li>3차 결과 공개 완료 후 어드민 조정 시작 전까지</li>
     *   <li>어드민 조정 기간 중 랜덤 매칭이 완료되지 않은 경우</li>
     *   <li>어드민 조정 종료부터 최종 결과 공개 완료 전까지</li>
     * </ul>
     * 디자이너는 디자인 매칭 일정, 개발자는 개발자 매칭 일정만 적용한다.
     *
     * @param rounds 대상 지부의 매칭 차수 목록
     * @param type 배정·해제할 파트에 해당하는 매칭 유형
     * @param now 기간 판단 기준 시각
     */
    public Optional<ProjectMatchingRound> validateChange(List<ProjectMatchingRound> rounds, MatchingType type, Instant now) {
        List<ProjectMatchingRound> matchingRounds = rounds.stream()
            .filter(round -> round.getType() == type).toList();
        validateRegularRoundPublicationGap(matchingRounds, now);
        Optional<ProjectMatchingRound> adjustment = findApplicableRandomRound(matchingRounds, now);
        adjustment.ifPresent(round -> {
            if (!round.isAutoDecisionExecuted() || now.isBefore(round.getStartsAt())
                || !now.isBefore(round.getEndsAt())) {
                throw changeNotAllowed();
            }
        });
        return adjustment;
    }

    /**
     * 조정 명단을 조회할 수 있는 랜덤 차수를 반환한다.
     * 랜덤 매칭 완료 후 조정 시작부터 최종 공개 전까지 조회하며, 조정 종료 후에도 명단은 조회할 수 있다.
     */
    public List<ProjectMatchingRound> findViewableAdjustmentRounds(List<ProjectMatchingRound> rounds, Instant now) {
        return rounds.stream()
            .filter(ProjectMatchingRound::isRandomMatching)
            .filter(ProjectMatchingRound::isAutoDecisionExecuted)
            .filter(round -> !now.isBefore(round.getStartsAt()) && round.getResultPublishedAt() == null)
            .toList();
    }

    private void validateRegularRoundPublicationGap(List<ProjectMatchingRound> rounds, Instant now) {
        boolean blocked = rounds.stream().filter(round -> !round.isRandomMatching())
            .anyMatch(round -> !now.isBefore(round.getDecisionDeadline())
                && !round.isResultPublishedAt(now));
        if (blocked) {
            throw changeNotAllowed();
        }
    }

    /**
     * 수동 배정·해제의 기간 판단에 사용할 미공개 랜덤 차수를 찾는다.
     * 3차 공개 직후부터 변경을 제한해야 하므로, 아직 실행되지 않은 랜덤 차수도 포함한다.
     */
    private Optional<ProjectMatchingRound> findApplicableRandomRound(List<ProjectMatchingRound> rounds, Instant now) {
        boolean thirdPublished = rounds.stream()
            .anyMatch(round -> round.getPhase() == MatchingPhase.THIRD && round.isResultPublishedAt(now));
        return rounds.stream().filter(ProjectMatchingRound::isRandomMatching)
            .filter(round -> round.getResultPublishedAt() == null)
            .filter(round -> thirdPublished || round.isAutoDecisionExecuted() || !now.isBefore(round.getStartsAt()))
            .findFirst();
    }

    private ProjectDomainException changeNotAllowed() {
        return new ProjectDomainException(ProjectErrorCode.PROJECT_MANUAL_MATCHING_NOT_ALLOWED_IN_PERIOD);
    }
}
