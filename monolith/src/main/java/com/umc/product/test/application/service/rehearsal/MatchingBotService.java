package com.umc.product.test.application.service.rehearsal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.project.application.port.in.command.DecideApplicationUseCase;
import com.umc.product.project.application.port.in.command.dto.ApplicationDecisionStatus;
import com.umc.product.project.application.port.in.query.GetMatchingSelectionRuleUseCase;
import com.umc.product.test.application.port.in.command.RunMatchingBotUseCase;
import com.umc.product.test.application.port.in.command.dto.BotRunResult;
import com.umc.product.test.application.port.in.command.dto.BotRunResult.Outcome;
import com.umc.product.test.application.port.in.query.GetRoundSummaryUseCase;
import com.umc.product.test.application.port.out.SeedProjectReadPort;
import com.umc.product.test.application.service.qa.SeedFailureCode;
import com.umc.product.test.domain.exception.SeedErrorCode;
import com.umc.product.test.domain.exception.SeedRehearsalException;
import com.umc.product.test.domain.rehearsal.ApplyPlan;
import com.umc.product.test.domain.rehearsal.BotApplyParams;
import com.umc.product.test.domain.rehearsal.BotApplyPlanner;
import com.umc.product.test.domain.rehearsal.BotDecisionParams;
import com.umc.product.test.domain.rehearsal.BotDecisionPlanner;
import com.umc.product.test.domain.rehearsal.ChapterRoles;
import com.umc.product.test.domain.rehearsal.DecisionPlan;
import com.umc.product.test.domain.rehearsal.RoundSnapshot;
import com.umc.product.test.domain.rehearsal.SkipReason;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 스냅샷 → 순수 계획 → 운영 UseCase 실행 → 새 스냅샷 요약. 지원자 한 명, 결정 하나가 각자 커밋되므로 하나가 실패해도
 * 나머지는 남고, 기수 잠금을 루프 내내 잡지 않는다.
 */
@Slf4j
@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@RequiredArgsConstructor
public class MatchingBotService implements RunMatchingBotUseCase {

    private static final String DECISION_REASON = "[봇] PM 결정";

    private final SeedProjectReadPort seedProjectReadPort;
    private final RehearsalContextLoader contextLoader;
    private final GetRoundSummaryUseCase getRoundSummaryUseCase;
    private final GetMatchingSelectionRuleUseCase getMatchingSelectionRuleUseCase;
    private final BotApplicationWriter botApplicationWriter;
    private final DecideApplicationUseCase decideApplicationUseCase;

    @Override
    public BotRunResult apply(Long roundId, BotApplyParams params) {
        RoundSnapshot snapshot = RoundSummaryQueryService.load(seedProjectReadPort, roundId);
        if (!snapshot.round().isApplicationOpenAt(snapshot.observedAt())) {
            throw new SeedRehearsalException(SeedErrorCode.NOT_IN_APPLICATION_WINDOW,
                "차수 " + roundId + "는 지금 " + snapshot.round().stageAt(snapshot.observedAt()) + " 단계입니다.");
        }
        ApplyPlan plan = BotApplyPlanner.plan(snapshot, roles(snapshot), params);
        Map<String, Integer> failed = new HashMap<>();
        int processed = run(plan.applications(), failed,
            planned -> botApplicationWriter.write(planned, roundId));
        return result(roundId, processed, plan.skipped(), failed);
    }

    @Override
    public BotRunResult decide(Long roundId, BotDecisionParams params) {
        RoundSnapshot snapshot = RoundSummaryQueryService.load(seedProjectReadPort, roundId);
        if (!snapshot.round().isDecisionOpenAt(snapshot.observedAt())) {
            throw new SeedRehearsalException(SeedErrorCode.NOT_IN_DECISION_WINDOW,
                "차수 " + roundId + "는 지금 " + snapshot.round().stageAt(snapshot.observedAt()) + " 단계입니다.");
        }
        DecisionPlan plan = BotDecisionPlanner.plan(snapshot, roles(snapshot), params,
            (applicants, remaining) -> getMatchingSelectionRuleUseCase.minimumRequired(
                snapshot.round().type(), applicants, remaining));
        Map<String, Integer> failed = new HashMap<>();
        int processed = run(plan.decisions(), failed, decision -> decideApplicationUseCase.decide(
            decision.projectId(), decision.applicationId(), ApplicationDecisionStatus.valueOf(decision.target().name()),
            DECISION_REASON, decision.decidedBy()));
        return result(roundId, processed, plan.skipped(), failed);
    }

    private ChapterRoles roles(RoundSnapshot snapshot) {
        return RoundSummaryQueryService.roles(contextLoader, snapshot);
    }

    private static <T> int run(List<T> items, Map<String, Integer> failed, Consumer<T> action) {
        int processed = 0;
        for (T item : items) {
            try {
                action.accept(item);
                processed++;
            } catch (RuntimeException e) {
                log.warn("매칭 봇 실행 실패: {}", item, e);
                failed.merge(SeedFailureCode.of(e), 1, Integer::sum);
            }
        }
        return processed;
    }

    private BotRunResult result(Long roundId, int processed, Map<SkipReason, Integer> skipped,
                                Map<String, Integer> failed) {
        return new BotRunResult(processed > 0 ? Outcome.APPLIED : Outcome.UNCHANGED, processed, skipped, failed,
            getRoundSummaryUseCase.get(roundId));
    }
}
