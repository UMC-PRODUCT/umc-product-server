package com.umc.product.schedule.application.port.in.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.schedule.application.port.in.command.CheckAttendanceUseCase;
import com.umc.product.schedule.application.port.in.command.CreateScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.dto.CheckAttendanceCommand;
import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleCommand;
import com.umc.product.schedule.application.port.in.query.dto.AvailableAttendanceInfo;
import com.umc.product.schedule.application.port.in.query.dto.MyAttendanceHistoryInfo;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleWithStatsInfo;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.out.SaveAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.SaveAttendanceSheetPort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import com.umc.product.support.UseCaseTestSupport;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class AttendanceQueryUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private CreateScheduleUseCase createScheduleUseCase;

    @Autowired
    private CheckAttendanceUseCase checkAttendanceUseCase;

    @Autowired
    private GetScheduleListUseCase getScheduleListUseCase;

    @Autowired
    private GetAvailableAttendancesUseCase getAvailableAttendancesUseCase;

    @Autowired
    private GetMyAttendanceHistoryUseCase getMyAttendanceHistoryUseCase;

    @Autowired
    private LoadAttendanceSheetPort loadAttendanceSheetPort;

    @Autowired
    private SaveAttendanceSheetPort saveAttendanceSheetPort;

    @Autowired
    private SaveAttendanceRecordPort saveAttendanceRecordPort;

    @Autowired
    private LoadSchedulePort loadSchedulePort;

    @Autowired
    private ManageGisuPort manageGisuPort;

    @Autowired
    private SaveMemberPort saveMemberPort;

    @Autowired
    private SaveChallengerPort saveChallengerPort;

    private Gisu activeGisu;
    private Member authorMember;
    private Challenger authorChallenger;
    private Member participantMember;

    @BeforeEach
    void setUp() {
        // 기수 생성
        activeGisu = manageGisuPort.save(createActiveGisu(9L));

        // 작성자 생성
        authorMember = saveMemberPort.save(createMember("작성자", "작성자닉네임", "author@test.com", 1L, "1"));
        authorChallenger = saveChallengerPort.save(createChallenger(authorMember.getId(), activeGisu.getId()));

        // 참여자 생성
        participantMember = saveMemberPort.save(createMember("참여자", "참여자닉네임", "participant@test.com", 1L, "2"));
        saveChallengerPort.save(createChallenger(participantMember.getId(), activeGisu.getId()));

        // Mock 설정
        mockChallengerInfo(authorMember.getId(), activeGisu.getId(), authorChallenger.getId());
    }

    @Nested
    @DisplayName("일정 목록 조회 테스트")
    class GetScheduleListTest {

        @Test
        @DisplayName("모든 일정을 조회할 수 있다")
        void 모든_일정을_조회할_수_있다() {
            // given
            LocalDateTime now = LocalDateTime.now();
            createSchedule("일정1", now.plusHours(1), now.plusHours(2), List.of(participantMember.getId()));
            createSchedule("일정2", now.plusHours(3), now.plusHours(4), List.of(participantMember.getId()));

            // when
            List<ScheduleWithStatsInfo> result = getScheduleListUseCase.getAll();

            // then
            assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("일정 목록에 출석 통계가 포함된다")
        void 일정_목록에_출석_통계가_포함된다() {
            // given
            LocalDateTime now = LocalDateTime.now();
            Long scheduleId = createSchedule("통계 테스트 일정", now.plusHours(1), now.plusHours(2),
                List.of(participantMember.getId()));

            // when
            List<ScheduleWithStatsInfo> result = getScheduleListUseCase.getAll();

            // then
            ScheduleWithStatsInfo scheduleInfo = result.stream()
                .filter(s -> s.scheduleId().equals(scheduleId))
                .findFirst()
                .orElseThrow();

            assertThat(scheduleInfo.totalCount()).isNotNull();
            assertThat(scheduleInfo.totalCount()).isEqualTo(1); // 참여자 1명
        }

        @Test
        @DisplayName("일정이 없으면 빈 리스트를 반환한다")
        void 일정이_없으면_빈_리스트를_반환한다() {
            // when - 아무 일정도 생성하지 않음
            List<ScheduleWithStatsInfo> result = getScheduleListUseCase.getAll();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("출석 가능한 일정 조회 테스트")
    class GetAvailableAttendancesTest {

        @Test
        @DisplayName("활성화된 출석부가 있는 일정을 조회할 수 있다")
        void 활성화된_출석부가_있는_일정을_조회할_수_있다() {
            // given
            LocalDateTime now = LocalDateTime.now();
            Long scheduleId = createSchedule("출석 가능 일정", now.plusHours(1), now.plusHours(2),
                List.of(participantMember.getId()));

            // when
            List<AvailableAttendanceInfo> result = getAvailableAttendancesUseCase
                .getAvailableList(participantMember.getId(), activeGisu.getId());

            // then
            assertThat(result).isNotEmpty();
            assertThat(result).anyMatch(info -> info.scheduleId().equals(scheduleId));
        }

        @Test
        @DisplayName("출석 기록이 없는 일정의 상태는 PENDING이다")
        void 출석_기록이_없는_일정의_상태는_PENDING이다() {
            // given
            LocalDateTime now = LocalDateTime.now();
            createSchedule("미출석 일정", now.plusHours(1), now.plusHours(2),
                List.of(participantMember.getId()));

            // when
            List<AvailableAttendanceInfo> result = getAvailableAttendancesUseCase
                .getAvailableList(participantMember.getId(), activeGisu.getId());

            // then
            assertThat(result).isNotEmpty();
            assertThat(result.get(0).status()).isEqualTo(AttendanceStatus.PENDING);
        }

        @Test
        @DisplayName("비활성화된 출석부의 일정은 조회되지 않는다")
        void 비활성화된_출석부의_일정은_조회되지_않는다() {
            // given
            LocalDateTime now = LocalDateTime.now();
            Long scheduleId = createSchedule("비활성 일정", now.plusHours(1), now.plusHours(2),
                List.of(participantMember.getId()));

            // 출석부 비활성화
            AttendanceSheet sheet = loadAttendanceSheetPort.findByScheduleId(scheduleId).orElseThrow();
            sheet.deactivate();

            // when
            List<AvailableAttendanceInfo> result = getAvailableAttendancesUseCase
                .getAvailableList(participantMember.getId(), activeGisu.getId());

            // then
            assertThat(result).noneMatch(info -> info.scheduleId().equals(scheduleId));
        }
    }

    @Nested
    @DisplayName("나의 출석 현황 조회 테스트")
    class GetMyAttendanceHistoryTest {

        @Test
        @DisplayName("나의 출석 기록을 조회할 수 있다")
        void 나의_출석_기록을_조회할_수_있다() {
            // given
            LocalDateTime now = LocalDateTime.now();
            Long scheduleId = createSchedule("출석 기록 일정", now.plusHours(1), now.plusHours(2),
                List.of(participantMember.getId()));

            // 출석 체크
            AttendanceSheet sheet = loadAttendanceSheetPort.findByScheduleId(scheduleId).orElseThrow();
            CheckAttendanceCommand checkCommand = new CheckAttendanceCommand(
                sheet.getId(),
                participantMember.getId(),
                sheet.getWindow().getStartTime().plusMinutes(5)
            );
            checkAttendanceUseCase.check(checkCommand);

            // when
            List<MyAttendanceHistoryInfo> result = getMyAttendanceHistoryUseCase
                .getHistory(participantMember.getId(), activeGisu.getId());

            // then
            assertThat(result).isNotEmpty();
            assertThat(result.get(0).status()).isEqualTo(AttendanceStatus.PRESENT);
        }

        @Test
        @DisplayName("출석 기록이 없으면 빈 리스트를 반환한다")
        void 출석_기록이_없으면_빈_리스트를_반환한다() {
            // given - 일정 생성 없음

            // when
            List<MyAttendanceHistoryInfo> result = getMyAttendanceHistoryUseCase
                .getHistory(participantMember.getId(), activeGisu.getId());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("출석 현황은 최신순으로 정렬된다")
        void 출석_현황은_최신순으로_정렬된다() {
            // given
            LocalDateTime now = LocalDateTime.now();

            // 첫 번째 일정 (더 이른 시간)
            Long scheduleId1 = createSchedule("첫번째 일정", now.plusHours(1), now.plusHours(2),
                List.of(participantMember.getId()));
            AttendanceSheet sheet1 = loadAttendanceSheetPort.findByScheduleId(scheduleId1).orElseThrow();
            checkAttendanceUseCase.check(new CheckAttendanceCommand(
                sheet1.getId(),
                participantMember.getId(),
                sheet1.getWindow().getStartTime().plusMinutes(5)
            ));

            // 두 번째 일정 (더 늦은 시간)
            Long scheduleId2 = createSchedule("두번째 일정", now.plusHours(3), now.plusHours(4),
                List.of(participantMember.getId()));
            AttendanceSheet sheet2 = loadAttendanceSheetPort.findByScheduleId(scheduleId2).orElseThrow();
            checkAttendanceUseCase.check(new CheckAttendanceCommand(
                sheet2.getId(),
                participantMember.getId(),
                sheet2.getWindow().getStartTime().plusMinutes(5)
            ));

            // when
            List<MyAttendanceHistoryInfo> result = getMyAttendanceHistoryUseCase
                .getHistory(participantMember.getId(), activeGisu.getId());

            // then
            assertThat(result).hasSize(2);
            // 최신순 정렬 확인 (두 번째 일정이 먼저)
            assertThat(result.get(0).scheduleName()).isEqualTo("두번째 일정");
            assertThat(result.get(1).scheduleName()).isEqualTo("첫번째 일정");
        }
    }

    // ========== Helper 메서드 ==========

    private Long createSchedule(String name, LocalDateTime startsAt, LocalDateTime endsAt,
                                List<Long> participantIds) {
        CreateScheduleCommand command = CreateScheduleCommand.of(
            name,
            startsAt,
            endsAt,
            false,
            "테스트 장소",
            null,
            "테스트 설명",
            participantIds,
            Set.of(ScheduleTag.GENERAL),
            authorMember.getId()
        );
        Long scheduleId = createScheduleUseCase.create(command);

        // 출석부 수동 생성
        Schedule schedule = loadSchedulePort.findById(scheduleId).orElseThrow();
        AttendanceSheet sheet = saveAttendanceSheetPort.save(AttendanceSheet.builder()
            .scheduleId(scheduleId)
            .gisuId(activeGisu.getId())
            .window(AttendanceWindow.ofDefault(schedule.getStartsAt()))
            .requiresApproval(false)
            .build());

        // 참여자 출석 기록 생성
        for (Long participantId : participantIds) {
            saveAttendanceRecordPort.save(AttendanceRecord.builder()
                .attendanceSheetId(sheet.getId())
                .memberId(participantId)
                .status(AttendanceStatus.PENDING)
                .build());
        }

        return scheduleId;
    }

    // ========== Fixture 메서드 ==========

    private Gisu createActiveGisu(Long generation) {
        return Gisu.create(
            generation,
            Instant.parse("2024-03-01T00:00:00Z"),
            Instant.parse("2024-08-31T23:59:00Z"),
            true
        );
    }

    private Member createMember(String name, String nickname, String email, Long schoolId, String profileImageId) {
        return Member.builder()
            .email(email)
            .name(name)
            .nickname(nickname)
            .schoolId(schoolId)
            .profileImageId(profileImageId)
            .build();
    }

    private Challenger createChallenger(Long memberId, Long gisuId) {
        return Challenger.builder()
            .memberId(memberId)
            .gisuId(gisuId)
            .part(ChallengerPart.SPRINGBOOT)
            .build();
    }

    private void mockChallengerInfo(Long memberId, Long gisuId, Long challengerId) {
        ChallengerInfo mockInfo = ChallengerInfo.builder()
            .challengerId(challengerId)
            .memberId(memberId)
            .gisuId(gisuId)
            .part(ChallengerPart.SPRINGBOOT)
            .challengerPoints(List.of())
            .build();

        ChallengerInfoWithStatus mockInfoWithStatus = ChallengerInfoWithStatus.builder()
            .challengerId(challengerId)
            .memberId(memberId)
            .gisuId(gisuId)
            .part(ChallengerPart.SPRINGBOOT)
            .status(ChallengerStatus.ACTIVE)
            .build();

        given(getChallengerUseCase.getByMemberIdAndGisuId(memberId, gisuId))
            .willReturn(mockInfo);
        given(getChallengerUseCase.getLatestActiveChallengerByMemberId(memberId))
            .willReturn(mockInfoWithStatus);
    }
}
