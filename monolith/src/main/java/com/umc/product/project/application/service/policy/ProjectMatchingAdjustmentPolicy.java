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
     * 변경할 수 없는 기간은 다음과 같다.
     * <ul>
     *   <li>1~3차 결정 마감부터 해당 결과 공개 완료 전까지</li>
     *   <li>3차 결과 공개 완료 후 어드민 조정 시작 전까지</li>
     *   <li>어드민 조정 기간 중 랜덤 매칭이 완료되지 않은 경우</li>
     *   <li>어드민 조정 종료부터 최종 결과 공개 완료 전까지</li>
     * </ul>
     * 해당 지부의 디자인·개발 매칭 중 하나라도 변경 불가 기간이면 모두 차단한다.
     * 일괄 공개할 랜덤 차수는 배정·해제할 파트의 매칭 유형을 기준으로 반환한다.
     *
     * @param rounds 대상 지부의 매칭 차수 목록
     * @param type 배정·해제할 파트에 해당하는 매칭 유형
     * @param now 기간 판단 기준 시각
     */
    public Optional<ProjectMatchingRound> validateChange(List<ProjectMatchingRound> rounds, MatchingType type, Instant now) {
        if (!isChangeAllowedInPeriod(rounds, now)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MANUAL_MATCHING_NOT_ALLOWED_IN_PERIOD);
        }
        List<ProjectMatchingRound> matchingRounds = rounds.stream()
            .filter(round -> round.getType() == type)
            .toList();
        return findApplicableRandomRound(matchingRounds, now);
    }

    /**
     * 해당 지부의 디자인·개발 일정을 확인해 수동 배정·해제가 가능한 기간인지 판단한다.
     * 실제 변경 요청과 프로젝트 권한 조회에서 같은 기간 규칙을 사용한다.
     */
    public boolean isChangeAllowedInPeriod(List<ProjectMatchingRound> rounds, Instant now) {
        boolean regularResultsPending = rounds.stream()
            .filter(round -> !round.isRandomMatching())
            .anyMatch(round -> !now.isBefore(round.getDecisionDeadline()) && !round.isResultPublishedAt(now));
        if (regularResultsPending) {
            return false;
        }
        for (MatchingType type : MatchingType.values()) {
            List<ProjectMatchingRound> matchingRounds = rounds.stream()
                .filter(round -> round.getType() == type)
                .toList();
            Optional<ProjectMatchingRound> adjustment = findApplicableRandomRound(matchingRounds, now);
            if (adjustment.isEmpty()) {
                continue;
            }

            ProjectMatchingRound randomRound = adjustment.get();
            boolean withinAdjustmentPeriod = !now.isBefore(randomRound.getStartsAt()) && now.isBefore(randomRound.getEndsAt());
            if (!randomRound.isAutoDecisionExecuted() || !withinAdjustmentPeriod) {
                return false;
            }
        }
        return true;
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

    /**
     * 수동 배정·해제의 기간 판단에 사용할 미공개 랜덤 차수를 찾는다.
     * 3차 공개 직후부터 변경을 제한해야 하므로, 아직 실행되지 않은 랜덤 차수도 포함한다.
     */
    private Optional<ProjectMatchingRound> findApplicableRandomRound(List<ProjectMatchingRound> rounds, Instant now) {
        boolean thirdPublished = rounds.stream()
            .anyMatch(round -> round.getPhase() == MatchingPhase.THIRD && round.isResultPublishedAt(now));
        return rounds.stream()
            .filter(ProjectMatchingRound::isRandomMatching)
            .filter(round -> round.getResultPublishedAt() == null)
            .filter(round -> thirdPublished || round.isAutoDecisionExecuted() || !now.isBefore(round.getStartsAt()))
            .findFirst();
    }
}
