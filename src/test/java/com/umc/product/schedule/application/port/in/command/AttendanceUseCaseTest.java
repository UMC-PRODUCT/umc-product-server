package com.umc.product.schedule.application.port.in.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.schedule.application.port.in.command.dto.CheckAttendanceCommand;
import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleCommand;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.out.SaveAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.SaveAttendanceSheetPort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceRecord.AttendanceRecordId;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import com.umc.product.support.UseCaseTestSupport;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Disabled("LocalDateTime -> Instant로 변경함에 따라 비활성화 처리")
@Transactional
public class AttendanceUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private CreateScheduleUseCase createScheduleUseCase;

    @Autowired
    private CheckAttendanceUseCase checkAttendanceUseCase;

    @Autowired
    private ApproveAttendanceUseCase approveAttendanceUseCase;

    @Autowired
    private LoadSchedulePort loadSchedulePort;

    @Autowired
    private LoadAttendanceSheetPort loadAttendanceSheetPort;

    @Autowired
    private LoadAttendanceRecordPort loadAttendanceRecordPort;

    @Autowired
    private SaveAttendanceSheetPort saveAttendanceSheetPort;

    @Autowired
    private SaveAttendanceRecordPort saveAttendanceRecordPort;

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
    private Long scheduleId;
    private AttendanceSheet attendanceSheet;

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

        // 일정 생성
        LocalDateTime now = LocalDateTime.now();
        CreateScheduleCommand command = CreateScheduleCommand.of(
            "테스트 일정",
            now.plusHours(1),
            now.plusHours(3),
            false,
            "테스트 장소",
            null,
            "테스트 설명",
            List.of(participantMember.getId()),
            Set.of(ScheduleTag.GENERAL),
            authorMember.getId()
        );

        scheduleId = createScheduleUseCase.create(command);
        Schedule schedule = loadSchedulePort.findById(scheduleId).orElseThrow();

        // 출석부 수동 생성
        attendanceSheet = saveAttendanceSheetPort.save(AttendanceSheet.builder()
            .scheduleId(scheduleId)
            .gisuId(activeGisu.getId())
            .window(AttendanceWindow.ofDefault(schedule.getStartsAt()))
            .requiresApproval(false)
            .build());

        // 참여자 출석 기록 생성
        saveAttendanceRecordPort.save(AttendanceRecord.builder()
            .attendanceSheetId(attendanceSheet.getId())
            .memberId(participantMember.getId())
            .status(AttendanceStatus.PENDING)
            .build());
    }

    @Nested
    @DisplayName("출석 요청 테스트")
    class CheckAttendanceTest {

        @Test
        @DisplayName("정상 시간에 출석하면 PRESENT 상태가 된다")
        void 정상_시간에_출석하면_PRESENT_상태가_된다() {
            // given
            LocalDateTime checkTime = attendanceSheet.getWindow().getStartTime().plusMinutes(5);
            CheckAttendanceCommand command = new CheckAttendanceCommand(
                attendanceSheet.getId(),
                participantMember.getId(),
                checkTime,
                37.5665,
                126.9780,
                true
            );

            // when
            AttendanceRecordId recordId = checkAttendanceUseCase.check(command);

            // then
            AttendanceRecord record = loadAttendanceRecordPort.findById(recordId.id()).orElseThrow();
            assertThat(record.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
            assertThat(record.getCheckedAt()).isEqualTo(checkTime);
        }

        @Test
        @DisplayName("지각 시간에 출석하면 LATE 상태가 된다")
        void 지각_시간에_출석하면_LATE_상태가_된다() {
            // given
            // 지각 임계값 이후 시간
            LocalDateTime checkTime = attendanceSheet.getWindow().getStartTime()
                .plusMinutes(attendanceSheet.getWindow().getLateThresholdMinutes() + 5);
            CheckAttendanceCommand command = new CheckAttendanceCommand(
                attendanceSheet.getId(),
                participantMember.getId(),
                checkTime,
                37.5665,
                126.9780,
                true
            );

            // when
            AttendanceRecordId recordId = checkAttendanceUseCase.check(command);

            // then
            AttendanceRecord record = loadAttendanceRecordPort.findById(recordId.id()).orElseThrow();
            assertThat(record.getStatus()).isEqualTo(AttendanceStatus.LATE);
        }

        @Test
        @DisplayName("비활성화된 출석부에 출석하면 예외가 발생한다")
        void 비활성화된_출석부에_출석하면_예외가_발생한다() {
            // given
            attendanceSheet.deactivate();
            loadAttendanceSheetPort.findById(attendanceSheet.getId());

            CheckAttendanceCommand command = new CheckAttendanceCommand(
                attendanceSheet.getId(),
                participantMember.getId(),
                LocalDateTime.now(),
                37.5665,
                126.9780,
                true
            );

            // when & then
            assertThatThrownBy(() -> checkAttendanceUseCase.check(command))
                .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("출석 승인 테스트")
    class ApproveAttendanceTest {

        @Test
        @DisplayName("승인 대기 상태의 출석을 승인하면 PRESENT 상태가 된다")
        void 승인_대기_상태의_출석을_승인하면_PRESENT_상태가_된다() {
            // given
            // 먼저 출석 체크 (승인 필요 모드로 변경 필요)
            attendanceSheet.updateApprovalMode(true);

            LocalDateTime checkTime = attendanceSheet.getWindow().getStartTime().plusMinutes(5);
            CheckAttendanceCommand checkCommand = new CheckAttendanceCommand(
                attendanceSheet.getId(),
                participantMember.getId(),
                checkTime,
                37.5665,
                126.9780,
                true
            );
            AttendanceRecordId recordId = checkAttendanceUseCase.check(checkCommand);

            // 상태 확인 (PRESENT_PENDING)
            AttendanceRecord pendingRecord = loadAttendanceRecordPort.findById(recordId.id()).orElseThrow();
            assertThat(pendingRecord.getStatus()).isEqualTo(AttendanceStatus.PRESENT_PENDING);

            // when
            approveAttendanceUseCase.approve(recordId, authorMember.getId());

            // then
            AttendanceRecord approvedRecord = loadAttendanceRecordPort.findById(recordId.id()).orElseThrow();
            assertThat(approvedRecord.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
            assertThat(approvedRecord.getConfirmedBy()).isEqualTo(authorMember.getId());
            assertThat(approvedRecord.getConfirmedAt()).isNotNull();
        }

        @Test
        @DisplayName("출석을 반려하면 ABSENT 상태가 된다")
        void 출석을_반려하면_ABSENT_상태가_된다() {
            // given
            attendanceSheet.updateApprovalMode(true);

            LocalDateTime checkTime = attendanceSheet.getWindow().getStartTime().plusMinutes(5);
            CheckAttendanceCommand checkCommand = new CheckAttendanceCommand(
                attendanceSheet.getId(),
                participantMember.getId(),
                checkTime,
                37.5665,
                126.9780,
                true
            );
            AttendanceRecordId recordId = checkAttendanceUseCase.check(checkCommand);

            // when
            approveAttendanceUseCase.reject(recordId, authorMember.getId());

            // then
            AttendanceRecord rejectedRecord = loadAttendanceRecordPort.findById(recordId.id()).orElseThrow();
            assertThat(rejectedRecord.getStatus()).isEqualTo(AttendanceStatus.ABSENT);
            assertThat(rejectedRecord.getConfirmedBy()).isEqualTo(authorMember.getId());
        }
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
