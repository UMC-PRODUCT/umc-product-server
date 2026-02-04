package com.umc.product.schedule.application.port.in.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.schedule.application.port.in.command.dto.CreateAttendanceSheetCommand;
import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleCommand;
import com.umc.product.schedule.application.port.in.command.dto.UpdateAttendanceSheetCommand;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;
import com.umc.product.schedule.domain.Schedule;
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
public class AttendanceSheetUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private CreateScheduleUseCase createScheduleUseCase;

    @Autowired
    private CreateAttendanceSheetUseCase createAttendanceSheetUseCase;

    @Autowired
    private UpdateAttendanceSheetUseCase updateAttendanceSheetUseCase;

    @Autowired
    private LoadSchedulePort loadSchedulePort;

    @Autowired
    private LoadAttendanceSheetPort loadAttendanceSheetPort;

    @Autowired
    private ManageGisuPort manageGisuPort;

    @Autowired
    private SaveMemberPort saveMemberPort;

    @Autowired
    private SaveChallengerPort saveChallengerPort;

    private Gisu activeGisu;
    private Member authorMember;
    private Challenger authorChallenger;
    private Long scheduleIdWithoutSheet;

    @BeforeEach
    void setUp() {
        // 기수 생성
        activeGisu = manageGisuPort.save(createActiveGisu(9L));

        // 작성자 생성
        authorMember = saveMemberPort.save(createMember("작성자", "작성자닉네임", "author@test.com", 1L, "1"));
        authorChallenger = saveChallengerPort.save(createChallenger(authorMember.getId(), activeGisu.getId()));

        // Mock 설정
        mockChallengerInfo(authorMember.getId(), activeGisu.getId(), authorChallenger.getId());

        // 참여자 없는 일정 생성 (출석부 자동 생성 안됨)
        LocalDateTime now = LocalDateTime.now();
        CreateScheduleCommand command = CreateScheduleCommand.of(
                "출석부 없는 일정",
                now.plusHours(1),
                now.plusHours(3),
                false,
                "테스트 장소",
                null,
                "테스트 설명",
                List.of(), // 참여자 없음
                Set.of(ScheduleTag.GENERAL),
                authorMember.getId()
        );
        scheduleIdWithoutSheet = createScheduleUseCase.create(command);
    }

    @Nested
    @DisplayName("출석부 생성 테스트")
    class CreateAttendanceSheetTest {

        @Test
        @DisplayName("일정에 출석부를 생성할 수 있다")
        void 일정에_출석부를_생성할_수_있다() {
            // given
            Schedule schedule = loadSchedulePort.findById(scheduleIdWithoutSheet).orElseThrow();
            AttendanceWindow window = AttendanceWindow.of(
                    schedule.getStartsAt(),
                    30, // 30분 전부터
                    30, // 30분 후까지
                    10  // 10분 지각 인정
            );

            CreateAttendanceSheetCommand command = new CreateAttendanceSheetCommand(
                    scheduleIdWithoutSheet,
                    activeGisu.getId(),
                    window,
                    false,
                    List.of()
            );

            // when
            AttendanceSheetId sheetId = createAttendanceSheetUseCase.create(command);

            // then
            AttendanceSheet sheet = loadAttendanceSheetPort.findById(sheetId.id()).orElseThrow();
            assertThat(sheet.getScheduleId()).isEqualTo(scheduleIdWithoutSheet);
            assertThat(sheet.isActive()).isTrue();
            assertThat(sheet.isRequiresApproval()).isFalse();
            assertThat(sheet.getWindow().getLateThresholdMinutes()).isEqualTo(10);
        }

        @Test
        @DisplayName("승인 필요 모드로 출석부를 생성할 수 있다")
        void 승인_필요_모드로_출석부를_생성할_수_있다() {
            // given
            Schedule schedule = loadSchedulePort.findById(scheduleIdWithoutSheet).orElseThrow();
            AttendanceWindow window = AttendanceWindow.ofDefault(schedule.getStartsAt());

            CreateAttendanceSheetCommand command = new CreateAttendanceSheetCommand(
                    scheduleIdWithoutSheet,
                    activeGisu.getId(),
                    window,
                    true, // 승인 필요
                    List.of()
            );

            // when
            AttendanceSheetId sheetId = createAttendanceSheetUseCase.create(command);

            // then
            AttendanceSheet sheet = loadAttendanceSheetPort.findById(sheetId.id()).orElseThrow();
            assertThat(sheet.isRequiresApproval()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 일정에 출석부를 생성하면 예외가 발생한다")
        void 존재하지_않는_일정에_출석부를_생성하면_예외가_발생한다() {
            // given
            AttendanceWindow window = AttendanceWindow.ofDefault(LocalDateTime.now());
            CreateAttendanceSheetCommand command = new CreateAttendanceSheetCommand(
                    999999L, // 존재하지 않는 일정 ID
                    activeGisu.getId(),
                    window,
                    false,
                    List.of()
            );

            // when & then
            assertThatThrownBy(() -> createAttendanceSheetUseCase.create(command))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("출석부 수정 테스트")
    class UpdateAttendanceSheetTest {

        private AttendanceSheet existingSheet;

        @BeforeEach
        void setUpSheet() {
            Schedule schedule = loadSchedulePort.findById(scheduleIdWithoutSheet).orElseThrow();
            AttendanceWindow window = AttendanceWindow.ofDefault(schedule.getStartsAt());
            CreateAttendanceSheetCommand createCommand = new CreateAttendanceSheetCommand(
                    scheduleIdWithoutSheet,
                    activeGisu.getId(),
                    window,
                    false,
                    List.of()
            );
            AttendanceSheetId sheetId = createAttendanceSheetUseCase.create(createCommand);
            existingSheet = loadAttendanceSheetPort.findById(sheetId.id()).orElseThrow();
        }

        @Test
        @DisplayName("출석부의 시간대를 수정할 수 있다")
        void 출석부의_시간대를_수정할_수_있다() {
            // given
            AttendanceWindow newWindow = AttendanceWindow.of(
                    existingSheet.getWindow().getStartTime(),
                    60, // 60분 전부터
                    60, // 60분 후까지
                    15  // 15분 지각 인정
            );

            UpdateAttendanceSheetCommand command = new UpdateAttendanceSheetCommand(
                    existingSheet.getAttendanceSheetId(),
                    newWindow,
                    false
            );

            // when
            updateAttendanceSheetUseCase.update(command);

            // then
            AttendanceSheet updated = loadAttendanceSheetPort.findById(existingSheet.getId()).orElseThrow();
            assertThat(updated.getWindow().getLateThresholdMinutes()).isEqualTo(15);
        }

        @Test
        @DisplayName("출석부의 승인 모드를 변경할 수 있다")
        void 출석부의_승인_모드를_변경할_수_있다() {
            // given
            assertThat(existingSheet.isRequiresApproval()).isFalse();

            UpdateAttendanceSheetCommand command = new UpdateAttendanceSheetCommand(
                    existingSheet.getAttendanceSheetId(),
                    existingSheet.getWindow(),
                    true // 승인 필요로 변경
            );

            // when
            updateAttendanceSheetUseCase.update(command);

            // then
            AttendanceSheet updated = loadAttendanceSheetPort.findById(existingSheet.getId()).orElseThrow();
            assertThat(updated.isRequiresApproval()).isTrue();
        }
    }

    @Nested
    @DisplayName("출석부 활성화/비활성화 테스트")
    class ActivateDeactivateTest {

        private AttendanceSheet existingSheet;

        @BeforeEach
        void setUpSheet() {
            Schedule schedule = loadSchedulePort.findById(scheduleIdWithoutSheet).orElseThrow();
            AttendanceWindow window = AttendanceWindow.ofDefault(schedule.getStartsAt());
            CreateAttendanceSheetCommand createCommand = new CreateAttendanceSheetCommand(
                    scheduleIdWithoutSheet,
                    activeGisu.getId(),
                    window,
                    false,
                    List.of()
            );
            AttendanceSheetId sheetId = createAttendanceSheetUseCase.create(createCommand);
            existingSheet = loadAttendanceSheetPort.findById(sheetId.id()).orElseThrow();
        }

        @Test
        @DisplayName("출석부를 비활성화할 수 있다")
        void 출석부를_비활성화할_수_있다() {
            // given
            assertThat(existingSheet.isActive()).isTrue();

            // when
            updateAttendanceSheetUseCase.deactivate(existingSheet.getAttendanceSheetId());

            // then
            AttendanceSheet deactivated = loadAttendanceSheetPort.findById(existingSheet.getId()).orElseThrow();
            assertThat(deactivated.isActive()).isFalse();
        }

        @Test
        @DisplayName("비활성화된 출석부를 다시 활성화할 수 있다")
        void 비활성화된_출석부를_다시_활성화할_수_있다() {
            // given
            updateAttendanceSheetUseCase.deactivate(existingSheet.getAttendanceSheetId());
            AttendanceSheet deactivated = loadAttendanceSheetPort.findById(existingSheet.getId()).orElseThrow();
            assertThat(deactivated.isActive()).isFalse();

            // when
            updateAttendanceSheetUseCase.activate(existingSheet.getAttendanceSheetId());

            // then
            AttendanceSheet activated = loadAttendanceSheetPort.findById(existingSheet.getId()).orElseThrow();
            assertThat(activated.isActive()).isTrue();
        }

        @Test
        @DisplayName("이미 비활성화된 출석부를 비활성화하면 예외가 발생한다")
        void 이미_비활성화된_출석부를_비활성화하면_예외가_발생한다() {
            // given
            updateAttendanceSheetUseCase.deactivate(existingSheet.getAttendanceSheetId());

            // when & then
            assertThatThrownBy(() ->
                    updateAttendanceSheetUseCase.deactivate(existingSheet.getAttendanceSheetId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("이미 활성화된 출석부를 활성화하면 예외가 발생한다")
        void 이미_활성화된_출석부를_활성화하면_예외가_발생한다() {
            // given - 이미 활성화 상태

            // when & then
            assertThatThrownBy(() ->
                    updateAttendanceSheetUseCase.activate(existingSheet.getAttendanceSheetId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // ========== Fixture 메서드 ==========

    private Gisu createActiveGisu(Long generation) {
        return Gisu.builder()
                .generation(generation)
                .isActive(true)
                .startAt(LocalDateTime.of(2024, 3, 1, 0, 0).atZone(ZoneId.systemDefault()).toInstant())
                .endAt(LocalDateTime.of(2024, 8, 31, 23, 59).atZone(ZoneId.systemDefault()).toInstant())
                .build();
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

        given(getChallengerUseCase.getByMemberIdAndGisuId(memberId, gisuId))
                .willReturn(mockInfo);
        given(getChallengerUseCase.getLatestActiveChallengerByMemberId(memberId))
                .willReturn(mockInfo);
    }
}
