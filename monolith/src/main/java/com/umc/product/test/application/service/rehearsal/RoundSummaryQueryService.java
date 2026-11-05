package com.umc.product.test.application.service.rehearsal;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.test.application.port.in.query.GetRoundSummaryUseCase;
import com.umc.product.test.domain.rehearsal.RoundSnapshot;
import com.umc.product.test.domain.rehearsal.RoundSummary;

import lombok.RequiredArgsConstructor;

@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoundSummaryQueryService implements GetRoundSummaryUseCase {

    private final RoundSnapshotReader snapshotReader;

    @Override
    public RoundSummary get(Long roundId) {
        RoundSnapshot snapshot = snapshotReader.load(roundId);
        return RoundSummary.of(snapshot, snapshotReader.roles(snapshot));
    }
}
