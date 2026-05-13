package com.umc.product.schedule.application.service.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.schedule.application.port.out.DeleteScheduleParticipantPort;
import com.umc.product.schedule.application.port.out.DeleteSchedulePort;
import com.umc.product.schedule.application.port.out.LoadScheduleParticipantPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.out.SaveScheduleParticipantPort;
import com.umc.product.schedule.application.port.out.SaveSchedulePort;
import com.umc.product.schedule.application.service.query.ScheduleCapabilitiesService;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.ScheduleParticipant;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleCommandService 일정 삭제")
class ScheduleCommandServiceDeleteTest {

    private static final Long SCHEDULE_ID = 100L;
    @Mock
    SaveSchedulePort saveSchedulePort;
    @Mock
    LoadSchedulePort loadSchedulePort;
    @Mock
    DeleteSchedulePort deleteSchedulePort;
    @Mock
    SaveScheduleParticipantPort saveScheduleParticipantPort;
    @Mock
    DeleteScheduleParticipantPort deleteScheduleParticipantPort;
    @Mock
    LoadScheduleParticipantPort loadScheduleParticipantPort;
    @Mock
    ScheduleCapabilitiesService capabilitiesService;
    @Mock
    GetChallengerUseCase getChallengerUseCase;
    @Mock
    GetGisuUseCase getGisuUseCase;
    @Mock
    GetMemberUseCase getMemberUseCase;
    @InjectMocks
    ScheduleCommandService sut;

    private Schedule scheduleStub() {
        Schedule schedule = new Schedule() {
        };
        ReflectionTestUtils.setField(schedule, "id", SCHEDULE_ID);
        ReflectionTestUtils.setField(schedule, "authorMemberId", 10L);
        return schedule;
    }

    private ScheduleParticipant participantStub(Long memberId) {
        ScheduleParticipant participant = new ScheduleParticipant() {
        };
        ReflectionTestUtils.setField(participant, "memberId", memberId);
        return participant;
    }

    @Nested
    @DisplayName("delete - 일반 삭제")
    class delete {

        @Test
        @DisplayName("일정과 모든 참여자를 함께 삭제한다")
        void 일정과_모든_참여자를_함께_삭제한다() {
            // given
            Schedule schedule = scheduleStub();
            List<ScheduleParticipant> participants = List.of(
                participantStub(1L),
                participantStub(2L)
            );

            given(loadSchedulePort.findById(SCHEDULE_ID)).willReturn(Optional.of(schedule));
            given(loadScheduleParticipantPort.existsAttendanceStatusByScheduleId(SCHEDULE_ID))
                .willReturn(false);
            given(loadScheduleParticipantPort.findAllByScheduleId(SCHEDULE_ID))
                .willReturn(participants);

            // when
            sut.delete(SCHEDULE_ID);

            // then
            then(deleteScheduleParticipantPort).should().deleteAll(participants);
            then(deleteSchedulePort).should().delete(SCHEDULE_ID);
        }

        @Test
        @DisplayName("참여자가 없는 일정도 정상 삭제된다")
        void 참여자가_없는_일정도_정상_삭제된다() {
            // given
            Schedule schedule = scheduleStub();

            given(loadSchedulePort.findById(SCHEDULE_ID)).willReturn(Optional.of(schedule));
            given(loadScheduleParticipantPort.existsAttendanceStatusByScheduleId(SCHEDULE_ID))
                .willReturn(false);
            given(loadScheduleParticipantPort.findAllByScheduleId(SCHEDULE_ID))
                .willReturn(List.of());

            // when
            sut.delete(SCHEDULE_ID);

            // then
            then(deleteScheduleParticipantPort).should(never()).deleteAll(anyList());
            then(deleteSchedulePort).should().delete(SCHEDULE_ID);
        }

        @Test
        @DisplayName("출석 기록이 존재하면 SCHEDULE-0033 예외가 발생한다")
        void 출석_기록이_존재하면_삭제_불가() {
            // given
            Schedule schedule = scheduleStub();

            given(loadSchedulePort.findById(SCHEDULE_ID)).willReturn(Optional.of(schedule));
            given(loadScheduleParticipantPort.existsAttendanceStatusByScheduleId(SCHEDULE_ID))
                .willReturn(true);

            // when & then
            assertThatThrownBy(() -> sut.delete(SCHEDULE_ID))
                .isInstanceOf(ScheduleDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ScheduleErrorCode.SCHEDULE_HAS_ATTENDANCE_RECORD);

            then(deleteSchedulePort).should(never()).delete(anyLong());
            then(deleteScheduleParticipantPort).should(never()).deleteAll(anyList());
        }

        @Test
        @DisplayName("존재하지 않는 일정 삭제 시 SCHEDULE_NOT_FOUND 예외가 발생한다")
        void 존재하지_않는_일정_삭제_시_예외() {
            // given
            given(loadSchedulePort.findById(SCHEDULE_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.delete(SCHEDULE_ID))
                .isInstanceOf(ScheduleDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ScheduleErrorCode.SCHEDULE_NOT_FOUND);

            then(deleteSchedulePort).should(never()).delete(anyLong());
        }
    }

    @Nested
    @DisplayName("forceDelete - 강제 삭제")
    class forceDelete {

        @Test
        @DisplayName("출석 기록 존재 여부와 무관하게 일정과 참여자를 함께 삭제한다")
        void 출석_기록_존재_여부와_무관하게_삭제된다() {
            // given
            Schedule schedule = scheduleStub();
            List<ScheduleParticipant> participants = List.of(
                participantStub(1L),
                participantStub(2L)
            );

            given(loadSchedulePort.findById(SCHEDULE_ID)).willReturn(Optional.of(schedule));
            given(loadScheduleParticipantPort.findAllByScheduleId(SCHEDULE_ID))
                .willReturn(participants);

            // when
            sut.forceDelete(SCHEDULE_ID);

            // then
            then(loadScheduleParticipantPort).should(never())
                .existsAttendanceStatusByScheduleId(any());
            then(deleteScheduleParticipantPort).should().deleteAll(participants);
            then(deleteSchedulePort).should().delete(SCHEDULE_ID);
        }

        @Test
        @DisplayName("참여자가 없으면 일정만 삭제된다")
        void 참여자가_없으면_일정만_삭제된다() {
            // given
            Schedule schedule = scheduleStub();

            given(loadSchedulePort.findById(SCHEDULE_ID)).willReturn(Optional.of(schedule));
            given(loadScheduleParticipantPort.findAllByScheduleId(SCHEDULE_ID))
                .willReturn(List.of());

            // when
            sut.forceDelete(SCHEDULE_ID);

            // then
            then(deleteScheduleParticipantPort).should(never()).deleteAll(anyList());
            then(deleteSchedulePort).should().delete(SCHEDULE_ID);
        }

        @Test
        @DisplayName("존재하지 않는 일정 강제 삭제 시 SCHEDULE_NOT_FOUND 예외가 발생한다")
        void 존재하지_않는_일정_강제_삭제_시_예외() {
            // given
            given(loadSchedulePort.findById(SCHEDULE_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.forceDelete(SCHEDULE_ID))
                .isInstanceOf(ScheduleDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ScheduleErrorCode.SCHEDULE_NOT_FOUND);

            then(deleteSchedulePort).should(never()).delete(anyLong());
        }
    }
}
