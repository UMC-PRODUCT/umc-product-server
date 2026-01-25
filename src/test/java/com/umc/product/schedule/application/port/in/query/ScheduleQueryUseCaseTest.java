package com.umc.product.schedule.application.port.in.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.util.GeometryUtils;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.schedule.application.port.in.query.dto.MyScheduleCalendarInfo;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleDetailInfo;
import com.umc.product.schedule.application.port.out.SaveAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.SaveAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.SaveSchedulePort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import com.umc.product.support.UseCaseTestSupport;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ScheduleQueryUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private GetMyScheduleUseCase getMyScheduleUseCase;

    @Autowired
    private GetScheduleDetailUseCase getScheduleDetailUseCase;

    @Autowired
    private SaveSchedulePort saveSchedulePort;

    @Autowired
    private SaveAttendanceSheetPort saveAttendanceSheetPort;

    @Autowired
    private SaveAttendanceRecordPort saveAttendanceRecordPort;

    @Autowired
    private SaveMemberPort saveMemberPort;

    private Member member;

    @BeforeEach
    void setUp() {
        member = saveMemberPort.save(createMember("테스터", "tester", "tester@test.com", 1L, 1L));
    }

    @Nested
    class 월별_나의_일정_조회 {

        @Test
        void 해당_월의_참여_일정을_조회한다() {
            // given
            Schedule schedule = saveScheduleWithAttendance(
                    "3월 스터디",
                    LocalDateTime.of(2026, 3, 15, 14, 0),
                    LocalDateTime.of(2026, 3, 15, 16, 0),
                    member.getId()
            );

            // when
            List<MyScheduleCalendarInfo> result = getMyScheduleUseCase.getMyMonthlySchedules(
                    member.getId(), 2026, 3);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).scheduleId()).isEqualTo(schedule.getId());
            assertThat(result.get(0).name()).isEqualTo("3월 스터디");
        }

        @Test
        void 다른_월의_일정은_조회되지_않는다() {
            // given
            saveScheduleWithAttendance(
                    "4월 스터디",
                    LocalDateTime.of(2026, 4, 15, 14, 0),
                    LocalDateTime.of(2026, 4, 15, 16, 0),
                    member.getId()
            );

            // when
            List<MyScheduleCalendarInfo> result = getMyScheduleUseCase.getMyMonthlySchedules(
                    member.getId(), 2026, 3);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void 참여하지_않는_일정은_조회되지_않는다() {
            // given
            Member otherMember = saveMemberPort.save(
                    createMember("다른사람", "other", "other@test.com", 1L, 2L));

            saveScheduleWithAttendance(
                    "다른 사람의 스터디",
                    LocalDateTime.of(2026, 3, 15, 14, 0),
                    LocalDateTime.of(2026, 3, 15, 16, 0),
                    otherMember.getId()
            );

            // when
            List<MyScheduleCalendarInfo> result = getMyScheduleUseCase.getMyMonthlySchedules(
                    member.getId(), 2026, 3);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void 종료된_일정의_상태는_종료됨이다() {
            // given
            saveScheduleWithAttendance(
                    "과거 일정",
                    LocalDateTime.of(2024, 1, 10, 10, 0),
                    LocalDateTime.of(2024, 1, 10, 12, 0),
                    member.getId()
            );

            // when
            List<MyScheduleCalendarInfo> result = getMyScheduleUseCase.getMyMonthlySchedules(
                    member.getId(), 2024, 1);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).status()).isEqualTo("종료됨");
        }

        @Test
        void 미래_일정의_상태는_참여_예정이다() {
            // given
            saveScheduleWithAttendance(
                    "미래 일정",
                    LocalDateTime.of(2030, 6, 15, 14, 0),
                    LocalDateTime.of(2030, 6, 15, 16, 0),
                    member.getId()
            );

            // when
            List<MyScheduleCalendarInfo> result = getMyScheduleUseCase.getMyMonthlySchedules(
                    member.getId(), 2030, 6);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).status()).isEqualTo("참여 예정");
        }

        @Test
        void 여러_일정을_시작시간_순으로_정렬하여_조회한다() {
            // given
            saveScheduleWithAttendance(
                    "늦은 일정",
                    LocalDateTime.of(2026, 3, 20, 14, 0),
                    LocalDateTime.of(2026, 3, 20, 16, 0),
                    member.getId()
            );
            saveScheduleWithAttendance(
                    "빠른 일정",
                    LocalDateTime.of(2026, 3, 5, 10, 0),
                    LocalDateTime.of(2026, 3, 5, 12, 0),
                    member.getId()
            );

            // when
            List<MyScheduleCalendarInfo> result = getMyScheduleUseCase.getMyMonthlySchedules(
                    member.getId(), 2026, 3);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).name()).isEqualTo("빠른 일정");
            assertThat(result.get(1).name()).isEqualTo("늦은 일정");
        }
    }

    @Nested
    class 일정_상세_조회 {

        @Test
        void 일정_상세_정보를_조회한다() {
            // given
            Schedule schedule = saveSchedulePort.save(Schedule.builder()
                    .name("상세 조회 일정")
                    .description("상세 설명입니다")
                    .tags(Set.of(ScheduleTag.STUDY, ScheduleTag.WORKSHOP))
                    .authorChallengerId(1L)
                    .startsAt(LocalDateTime.of(2030, 5, 20, 14, 0))
                    .endsAt(LocalDateTime.of(2030, 5, 20, 16, 0))
                    .isAllDay(false)
                    .locationName("강남역 스터디룸")
                    .location(GeometryUtils.createPoint(37.4979, 127.0276))
                    .build());

            // when
            ScheduleDetailInfo result = getScheduleDetailUseCase.getScheduleDetail(schedule.getId());

            // then
            assertThat(result.scheduleId()).isEqualTo(schedule.getId());
            assertThat(result.name()).isEqualTo("상세 조회 일정");
            assertThat(result.description()).isEqualTo("상세 설명입니다");
            assertThat(result.tags()).containsExactlyInAnyOrder(ScheduleTag.STUDY, ScheduleTag.WORKSHOP);
            assertThat(result.startsAt()).isEqualTo(LocalDateTime.of(2030, 5, 20, 14, 0));
            assertThat(result.endsAt()).isEqualTo(LocalDateTime.of(2030, 5, 20, 16, 0));
            assertThat(result.isAllDay()).isFalse();
            assertThat(result.locationName()).isEqualTo("강남역 스터디룸");
            assertThat(result.latitude()).isEqualTo(37.4979);
            assertThat(result.longitude()).isEqualTo(127.0276);
            assertThat(result.status()).isEqualTo("예정");
        }

        @Test
        void 위치_정보가_없는_일정을_조회한다() {
            // given
            Schedule schedule = saveSchedulePort.save(Schedule.builder()
                    .name("온라인 일정")
                    .description("온라인으로 진행됩니다")
                    .tags(Set.of(ScheduleTag.MEETING))
                    .authorChallengerId(1L)
                    .startsAt(LocalDateTime.of(2030, 5, 25, 10, 0))
                    .endsAt(LocalDateTime.of(2030, 5, 25, 12, 0))
                    .isAllDay(false)
                    .build());

            // when
            ScheduleDetailInfo result = getScheduleDetailUseCase.getScheduleDetail(schedule.getId());

            // then
            assertThat(result.locationName()).isNull();
            assertThat(result.latitude()).isNull();
            assertThat(result.longitude()).isNull();
        }

        @Test
        void 존재하지_않는_일정을_조회하면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> getScheduleDetailUseCase.getScheduleDetail(99999L))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        void 종료된_일정의_상태가_올바르게_반환된다() {
            // given
            Schedule schedule = saveSchedulePort.save(Schedule.builder()
                    .name("과거 일정")
                    .authorChallengerId(1L)
                    .startsAt(LocalDateTime.of(2020, 1, 10, 10, 0))
                    .endsAt(LocalDateTime.of(2020, 1, 10, 12, 0))
                    .isAllDay(false)
                    .tags(Set.of(ScheduleTag.GENERAL))
                    .build());

            // when
            ScheduleDetailInfo result = getScheduleDetailUseCase.getScheduleDetail(schedule.getId());

            // then
            assertThat(result.status()).isEqualTo("종료됨");
            assertThat(result.dDay()).isNegative();
        }

        @Test
        void 미래_일정의_디데이가_양수로_반환된다() {
            // given
            Schedule schedule = saveSchedulePort.save(Schedule.builder()
                    .name("미래 일정")
                    .authorChallengerId(1L)
                    .startsAt(LocalDateTime.of(2030, 12, 25, 10, 0))
                    .endsAt(LocalDateTime.of(2030, 12, 25, 18, 0))
                    .isAllDay(false)
                    .tags(Set.of(ScheduleTag.GENERAL))
                    .build());

            // when
            ScheduleDetailInfo result = getScheduleDetailUseCase.getScheduleDetail(schedule.getId());

            // then
            assertThat(result.dDay()).isPositive();
            assertThat(result.status()).isEqualTo("예정");
        }
    }

    // ========== Fixture 메서드 ==========

    private Schedule saveScheduleWithAttendance(String name, LocalDateTime startsAt,
                                                LocalDateTime endsAt, Long participantMemberId) {
        Schedule schedule = saveSchedulePort.save(Schedule.builder()
                .name(name)
                .authorChallengerId(1L)
                .startsAt(startsAt)
                .endsAt(endsAt)
                .isAllDay(false)
                .tags(Set.of(ScheduleTag.STUDY))
                .build());

        AttendanceSheet sheet = saveAttendanceSheetPort.save(AttendanceSheet.builder()
                .scheduleId(schedule.getId())
                .window(AttendanceWindow.ofDefault(startsAt))
                .requiresApproval(false)
                .build());

        saveAttendanceRecordPort.save(AttendanceRecord.builder()
                .attendanceSheetId(sheet.getId())
                .memberId(participantMemberId)
                .status(AttendanceStatus.PENDING)
                .build());

        return schedule;
    }

    private Member createMember(String name, String nickname, String email, Long schoolId, Long profileImageId) {
        return Member.builder()
                .email(email)
                .name(name)
                .nickname(nickname)
                .schoolId(schoolId)
                .profileImageId(profileImageId)
                .build();
    }

}
