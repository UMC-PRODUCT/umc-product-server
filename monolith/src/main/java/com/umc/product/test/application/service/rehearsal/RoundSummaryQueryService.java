package com.umc.product.test.application.service.rehearsal;

import java.util.List;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.test.application.port.in.query.GetRoundSummaryUseCase;
import com.umc.product.test.application.port.out.SeedProjectReadPort;
import com.umc.product.test.domain.exception.SeedErrorCode;
import com.umc.product.test.domain.exception.SeedRehearsalException;
import com.umc.product.test.domain.rehearsal.ChapterRoles;
import com.umc.product.test.domain.rehearsal.RoundSnapshot;
import com.umc.product.test.domain.rehearsal.RoundSummary;

import lombok.RequiredArgsConstructor;

@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoundSummaryQueryService implements GetRoundSummaryUseCase {

    private final SeedProjectReadPort seedProjectReadPort;
    private final RehearsalContextLoader contextLoader;

    @Override
    public RoundSummary get(Long roundId) {
        RoundSnapshot snapshot = load(seedProjectReadPort, roundId);
        return RoundSummary.of(snapshot, roles(contextLoader, snapshot));
    }

    static RoundSnapshot load(SeedProjectReadPort port, Long roundId) {
        return port.findRoundSnapshot(roundId)
            .orElseThrow(() -> new SeedRehearsalException(SeedErrorCode.ROUND_NOT_FOUND, "차수가 없습니다: " + roundId));
    }

    /**
     * 11기가 없는 DB의 차수는 배역표 지부일 수 없으니 QA 몫이 없다.
     */
    static ChapterRoles roles(RehearsalContextLoader contextLoader, RoundSnapshot snapshot) {
        return contextLoader.find()
            .map(context -> ChapterRoles.of(context.cast(), snapshot.round().chapterName(), context::email))
            .orElseGet(() -> new ChapterRoles(Set.of(), List.of()));
    }
}
