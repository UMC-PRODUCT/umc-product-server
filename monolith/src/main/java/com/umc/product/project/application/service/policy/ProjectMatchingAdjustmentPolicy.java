package com.umc.product.project.application.service.policy;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

/**
 * 팀원 수동 배정·해제 가능 기간과 매칭 유형별 결과 반영 시점을 판단하는 정책.
 */
@Component
public class ProjectMatchingAdjustmentPolicy {

    /**
     * 팀원 수동 배정·해제 가능 여부를 검증한다.
     * <ul>
     *   <li>최종 결과 공개 시 일괄 반영해야 하는 경우: 결과를 공개할 랜덤 차수를 반환한다.</li>
     *   <li>즉시 반영 가능한 기간인 경우: 빈 Optional을 반환한다.</li>
     *   <li>변경할 수 없는 기간인 경우: 예외를 발생시킨다.</li>
     * </ul>
     * 수동 배정·해제를 허용하는 구간은 다음과 같다.
     * <ul>
     *   <li>1차 지원 시작부터 1차 결정 마감까지</li>
     *   <li>1차 결과 공개 후부터 2차 결정 마감까지</li>
     *   <li>2차 결과 공개 후부터 3차 결정 마감까지</li>
     *   <li>랜덤 매칭 실행 완료 후 조정 시작부터 조정 종료까지</li>
     * </ul>
     * 배정·해제할 파트와 같은 매칭 유형의 허용 구간에서만 변경할 수 있다.
     * 일괄 공개할 랜덤 차수는 배정·해제할 파트의 매칭 유형을 기준으로 반환한다.
     *
     * @param rounds 대상 지부의 매칭 차수 목록
     * @param type 배정·해제할 파트에 해당하는 매칭 유형
     * @param now 기간 판단 기준 시각
     */
    public Optional<ProjectMatchingRound> validateChange(List<ProjectMatchingRound> rounds, MatchingType type, Instant now) {
        List<ProjectMatchingRound> matchingRounds = rounds.stream()
            .filter(round -> round.getType() == type)
            .toList();
        if (!isChangePeriod(matchingRounds, now)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MANUAL_MATCHING_NOT_ALLOWED_IN_PERIOD);
        }
        return matchingRounds.stream()
            .filter(round -> isAdjustmentPeriod(round, now))
            .findFirst();
    }

    /** 수동 배정 시점에 시작된 가장 최근 1~3차를 반환한다. 1차 결과 공개 후 2차 시작 전의 배정은 1차로 기록한다. */
    public ProjectMatchingRound getManualAssignmentRound(
        List<ProjectMatchingRound> rounds, MatchingType type, Instant now
    ) {
        return rounds.stream()
            .filter(round -> round.getType() == type && !round.isRandomMatching())
            .filter(round -> !now.isBefore(round.getStartsAt()))
            .max(Comparator.comparing(ProjectMatchingRound::getStartsAt))
            .orElseThrow(() -> new ProjectDomainException(
                ProjectErrorCode.PROJECT_MANUAL_MATCHING_NOT_ALLOWED_IN_PERIOD));
    }

    /**
     * 해당 지부에서 디자인·개발 중 변경 가능한 유형이 있는지 판단한다.
     * 프로젝트 단위 권한 조회에 사용하며, 실제 변경 요청에서는 대상 파트의 유형을 검증한다.
     */
    public boolean isChangeAllowedInPeriod(List<ProjectMatchingRound> rounds, Instant now) {
        for (MatchingType type : MatchingType.values()) {
            List<ProjectMatchingRound> matchingRounds = rounds.stream()
                .filter(round -> round.getType() == type)
                .toList();
            if (isChangePeriod(matchingRounds, now)) {
                return true;
            }
        }
        return false;
    }

    /** 같은 매칭 유형의 일반 차수 또는 랜덤 조정 허용 구간인지 판단한다. */
    private boolean isChangePeriod(List<ProjectMatchingRound> matchingRounds, Instant now) {
        return isRegularChangePeriod(matchingRounds, now)
            || matchingRounds.stream().anyMatch(round -> isAdjustmentPeriod(round, now));
    }

    /** 일반 차수의 지원 시작 또는 이전 결과 공개부터 결정 마감까지 변경을 허용한다. */
    private boolean isRegularChangePeriod(List<ProjectMatchingRound> rounds, Instant now) {
        List<ProjectMatchingRound> regularRounds = rounds.stream()
            .filter(round -> !round.isRandomMatching())
            .sorted(Comparator.comparing(ProjectMatchingRound::getPhase))
            .toList();
        ProjectMatchingRound previous = null;
        for (ProjectMatchingRound round : regularRounds) {
            boolean started = round.getPhase() == MatchingPhase.FIRST
                ? !now.isBefore(round.getStartsAt())
                : previous != null && previous.isResultPublishedAt(now);
            if (started && now.isBefore(round.getDecisionDeadline())) {
                return true;
            }
            previous = round;
        }
        return false;
    }

    /** 랜덤 매칭이 완료된 미공개 차수의 조정 시작부터 종료까지 변경을 허용한다. */
    private boolean isAdjustmentPeriod(ProjectMatchingRound round, Instant now) {
        return round.isRandomMatching()
            && round.isAutoDecisionExecuted()
            && round.getResultPublishedAt() == null
            && !now.isBefore(round.getStartsAt())
            && now.isBefore(round.getEndsAt());
    }

    /**
     * 조정 명단을 조회할 수 있는 랜덤 차수를 반환한다.
     * 랜덤 매칭 완료 후 조정 시작부터 최종 결과 공개 전까지 조회한다.
     * 조정이 끝난 뒤에도 운영진은 최종 공개할 명단을 확인해야 하므로, 조정 종료 시각으로 조회를 제한하지 않는다.
     */
    public List<ProjectMatchingRound> findViewableAdjustmentRounds(List<ProjectMatchingRound> rounds, Instant now) {
        return rounds.stream()
            .filter(ProjectMatchingRound::isRandomMatching)
            .filter(ProjectMatchingRound::isAutoDecisionExecuted)
            .filter(round -> !now.isBefore(round.getStartsAt()) && round.getResultPublishedAt() == null)
            .toList();
    }
}
