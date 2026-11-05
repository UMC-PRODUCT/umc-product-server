package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.ScheduleMatchingRoundDeadlinePort;
import com.umc.product.project.application.port.out.dto.MatchingRoundTimers;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;

@ExtendWith(MockitoExtension.class)
class MatchingRoundScheduleResyncServiceTest {

    @Mock
    LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    @Mock
    ScheduleMatchingRoundDeadlinePort scheduleMatchingRoundDeadlinePort;
    @InjectMocks
    MatchingRoundScheduleResyncService sut;

    @Test
    @DisplayName("차수가 있으면 DB에서 읽은 차수로 알람을 다시 등록하고 등록한 시각을 돌려준다")
    void resyncRegistersAlarmsFromLoadedRound() {
        // given
        Instant startsAt = Instant.parse("2026-11-10T00:00:00Z");
        ProjectMatchingRound round = ProjectMatchingRound.create("1차", null, MatchingType.PLAN_DESIGN,
            MatchingPhase.FIRST, 1L, startsAt, startsAt.plusSeconds(3600), startsAt.plusSeconds(7200),
            startsAt.plusSeconds(10800));
        MatchingRoundTimers registered = new MatchingRoundTimers(7L, startsAt.plusSeconds(7260),
            startsAt.plusSeconds(10800));
        given(loadProjectMatchingRoundPort.findById(7L)).willReturn(Optional.of(round));
        given(scheduleMatchingRoundDeadlinePort.schedule(round)).willReturn(registered);

        // when
        MatchingRoundTimers result = sut.resync(7L);

        // then
        assertThat(result).isEqualTo(registered);
        then(scheduleMatchingRoundDeadlinePort).should(never()).cancel(7L);
    }

    @Test
    @DisplayName("차수가 없으면 남은 알람을 취소하고 등록된 알람이 없다고 돌려준다")
    void resyncCancelsAlarmsOfMissingRound() {
        // given
        given(loadProjectMatchingRoundPort.findById(7L)).willReturn(Optional.empty());

        // when
        MatchingRoundTimers result = sut.resync(7L);

        // then
        assertThat(result).isEqualTo(new MatchingRoundTimers(7L, null, null));
        then(scheduleMatchingRoundDeadlinePort).should().cancel(7L);
    }
}
