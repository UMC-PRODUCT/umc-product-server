package com.umc.product.support.fixture;

import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.project.application.port.out.SaveProjectMatchingRoundPort;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;

@Component
@Transactional
public class ProjectUpdateFixture {
    private final SaveProjectMatchingRoundPort saveProjectMatchingRoundPort;

    public ProjectUpdateFixture(SaveProjectMatchingRoundPort saveProjectMatchingRoundPort) {
        this.saveProjectMatchingRoundPort = saveProjectMatchingRoundPort;
    }

    public void 매칭차수(Long chapterId, MatchingType type, MatchingPhase phase, Instant startsAt, Instant endsAt) {
        Instant deadline = phase == MatchingPhase.RANDOM_MATCHING ? endsAt : endsAt.plusSeconds(3600);
        saveProjectMatchingRoundPort.save(ProjectMatchingRound.create("정보 수정 검증", null, type, phase, chapterId,
            startsAt, endsAt, deadline, deadline.plusSeconds(3600)));
    }
}
