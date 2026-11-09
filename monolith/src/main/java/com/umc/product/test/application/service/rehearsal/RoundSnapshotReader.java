package com.umc.product.test.application.service.rehearsal;

import java.util.List;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.umc.product.test.application.port.out.SeedProjectReadPort;
import com.umc.product.test.domain.exception.SeedErrorCode;
import com.umc.product.test.domain.exception.SeedRehearsalException;
import com.umc.product.test.domain.rehearsal.ChapterRoles;
import com.umc.product.test.domain.rehearsal.RoundSnapshot;

import lombok.RequiredArgsConstructor;

@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
class RoundSnapshotReader {

    private final SeedProjectReadPort seedProjectReadPort;
    private final RehearsalContextLoader contextLoader;

    RoundSnapshot load(Long roundId) {
        return seedProjectReadPort.findRoundSnapshot(roundId)
            .orElseThrow(() -> new SeedRehearsalException(SeedErrorCode.ROUND_NOT_FOUND, "차수가 없습니다: " + roundId));
    }

    /** 11기가 없으면 QA 몫이 없는 것으로 본다. 11기 행이 둘 이상이면 하나를 고르지 않고 실패한다. */
    ChapterRoles roles(RoundSnapshot snapshot) {
        return contextLoader.find()
            .map(context -> ChapterRoles.of(context.cast(), snapshot.round().chapterName()))
            .orElseGet(() -> new ChapterRoles(Set.of(), List.of()));
    }
}
