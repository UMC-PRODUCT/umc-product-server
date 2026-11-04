package com.umc.product.test.application.service.matching;

import java.time.Clock;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.umc.product.project.application.port.in.command.ResyncMatchingRoundScheduleUseCase;
import com.umc.product.project.application.port.out.dto.MatchingRoundTimers;
import com.umc.product.test.application.port.in.command.AdvanceMatchingRoundUseCase;
import com.umc.product.test.application.port.in.command.dto.AdvanceMatchingRoundResult;
import com.umc.product.test.application.port.in.command.dto.AdvanceMatchingRoundResult.Outcome;
import com.umc.product.test.application.port.out.RewriteMatchingRoundTimesPort;
import com.umc.product.test.domain.exception.SeedErrorCode;
import com.umc.product.test.domain.exception.SeedRehearsalException;
import com.umc.product.test.domain.rehearsal.AdvanceDecision;
import com.umc.product.test.domain.rehearsal.AdvanceDecision.AlreadyReached;
import com.umc.product.test.domain.rehearsal.AdvanceDecision.Refused;
import com.umc.product.test.domain.rehearsal.AdvanceDecision.Rewrite;
import com.umc.product.test.domain.rehearsal.AdvancePlanner;
import com.umc.product.test.domain.rehearsal.AdvanceStep;
import com.umc.product.test.domain.rehearsal.MatchingSequence;
import com.umc.product.test.domain.rehearsal.RoundTimeChange;

/**
 * 차수 시각을 한 트랜잭션으로 고쳐 커밋한 뒤, 같은 묶음 차수의 알람을 새 시각으로 다시 맞춘다.
 * 자동 선발과 발표는 직접 실행하지 않고 실제 스케줄러에 맡긴다.
 */
@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MatchingRoundAdvanceService implements AdvanceMatchingRoundUseCase {

    private final RewriteMatchingRoundTimesPort rewriteMatchingRoundTimesPort;
    private final ResyncMatchingRoundScheduleUseCase resyncMatchingRoundScheduleUseCase;
    private final Clock clock;
    private final TransactionTemplate transactionTemplate;

    public MatchingRoundAdvanceService(
        RewriteMatchingRoundTimesPort rewriteMatchingRoundTimesPort,
        ResyncMatchingRoundScheduleUseCase resyncMatchingRoundScheduleUseCase,
        Clock clock,
        PlatformTransactionManager transactionManager
    ) {
        this.rewriteMatchingRoundTimesPort = rewriteMatchingRoundTimesPort;
        this.resyncMatchingRoundScheduleUseCase = resyncMatchingRoundScheduleUseCase;
        this.clock = clock;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public AdvanceMatchingRoundResult advance(Long roundId, AdvanceStep step) {
        Planned planned = transactionTemplate.execute(status -> rewrite(roundId, step));
        List<MatchingRoundTimers> timers = planned.sequence().rounds().stream()
            .map(round -> resyncMatchingRoundScheduleUseCase.resync(round.roundId()))
            .toList();
        return new AdvanceMatchingRoundResult(roundId, step, planned.outcome(), planned.changes(), timers);
    }

    private Planned rewrite(Long roundId, AdvanceStep step) {
        MatchingSequence sequence = rewriteMatchingRoundTimesPort.lockSequence(roundId)
            .orElseThrow(() -> new SeedRehearsalException(SeedErrorCode.ROUND_NOT_FOUND));
        AdvanceDecision decision = AdvancePlanner.plan(sequence, roundId, step, clock.instant());
        return switch (decision) {
            case Refused refused -> throw refused.reason() == null
                ? new SeedRehearsalException(refused.code())
                : new SeedRehearsalException(refused.code(), refused.reason());
            case AlreadyReached ignored -> new Planned(sequence, Outcome.ALREADY_REACHED, List.of());
            case Rewrite next -> {
                rewriteMatchingRoundTimesPort.rewrite(next.next());
                List<RoundTimeChange> changes = next.next().stream()
                    .flatMap(after -> RoundTimeChange.between(sequence.get(after.roundId()), after).stream())
                    .toList();
                yield new Planned(sequence, Outcome.APPLIED, changes);
            }
        };
    }

    private record Planned(MatchingSequence sequence, Outcome outcome, List<RoundTimeChange> changes) {
    }
}
