package com.umc.product.schedule.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.schedule.application.port.in.command.dto.SubmitReasonCommand;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.SaveAttendanceRecordPort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceRecord.AttendanceRecordId;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.ScheduleConstants;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@Disabled
@ExtendWith(MockitoExtension.class)
@DisplayName("사유 제출 출석 UseCase 테스트")
class SubmitReasonUseCaseTest {

    @Mock
    LoadAttendanceSheetPort loadAttendanceSheetPort;
    @Mock
    LoadAttendanceRecordPort loadAttendanceRecordPort;
    @Mock
    SaveAttendanceRecordPort saveAttendanceRecordPort;

    @InjectMocks
    AttendanceCommandService sut;

    @Test
    @DisplayName("사유 제출 출석 성공 - EXCUSED_PENDING 상태로 저장")
    void submitReason_Success() {
        // given
        Long sheetId = 1L;
        Long memberId = 100L;
        String reason = "개인 사유로 위치 인증이 어렵습니다";
        Instant now = Instant.now(); // Instant로 변경

        // DTO나 Command 클래스도 Instant를 받도록 수정되어 있어야 합니다.
        SubmitReasonCommand command = new SubmitReasonCommand(sheetId, memberId, reason, now);

        AttendanceSheet sheet = createActiveSheet(sheetId);
        AttendanceRecord record = createPendingRecord(sheetId, memberId);

        given(loadAttendanceSheetPort.findById(sheetId)).willReturn(Optional.of(sheet));
        given(loadAttendanceRecordPort.findBySheetIdAndMemberId(sheetId, memberId))
            .willReturn(Optional.of(record));
        given(saveAttendanceRecordPort.save(any(AttendanceRecord.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        // when
        AttendanceRecordId result = sut.submitReason(command);

        // then
        assertThat(result).isNotNull();
        assertThat(record.getStatus()).isEqualTo(AttendanceStatus.EXCUSED_PENDING);
        assertThat(record.getMemo()).isEqualTo(reason);
        // checkedAt은 LocalDateTime으로 저장되므로 변환해서 비교
        assertThat(record.getCheckedAt())
            .isEqualTo(java.time.LocalDateTime.ofInstant(now, ScheduleConstants.KST));
        then(saveAttendanceRecordPort).should().save(record);
    }

    @Test
    @DisplayName("출석부를 찾을 수 없으면 예외 발생")
    void submitReason_SheetNotFound_ThrowsException() {
        // given
        Long sheetId = 999L;
        Long memberId = 100L;
        SubmitReasonCommand command = new SubmitReasonCommand(
            sheetId, memberId, "사유", Instant.now()
        );

        given(loadAttendanceSheetPort.findById(sheetId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.submitReason(command))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("비활성화된 출석부면 예외 발생")
    void submitReason_InactiveSheet_ThrowsException() {
        // given
        Long sheetId = 1L;
        Long memberId = 100L;
        SubmitReasonCommand command = new SubmitReasonCommand(
            sheetId, memberId, "사유", Instant.now()
        );

        AttendanceSheet sheet = createInactiveSheet(sheetId);
        given(loadAttendanceSheetPort.findById(sheetId)).willReturn(Optional.of(sheet));

        // when & then
        assertThatThrownBy(() -> sut.submitReason(command))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("출석 기록을 찾을 수 없으면 예외 발생")
    void submitReason_RecordNotFound_ThrowsException() {
        // given
        Long sheetId = 1L;
        Long memberId = 999L;
        SubmitReasonCommand command = new SubmitReasonCommand(
            sheetId, memberId, "사유", Instant.now()
        );

        AttendanceSheet sheet = createActiveSheet(sheetId);
        given(loadAttendanceSheetPort.findById(sheetId)).willReturn(Optional.of(sheet));
        given(loadAttendanceRecordPort.findBySheetIdAndMemberId(sheetId, memberId))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.submitReason(command))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("이미 출석 체크한 기록은 사유 제출 불가")
    void submitReason_AlreadyChecked_ThrowsException() {
        // given
        Long sheetId = 1L;
        Long memberId = 100L;
        SubmitReasonCommand command = new SubmitReasonCommand(
            sheetId, memberId, "사유", Instant.now()
        );

        AttendanceSheet sheet = createActiveSheet(sheetId);
        AttendanceRecord record = createCheckedRecord(sheetId, memberId);

        given(loadAttendanceSheetPort.findById(sheetId)).willReturn(Optional.of(sheet));
        given(loadAttendanceRecordPort.findBySheetIdAndMemberId(sheetId, memberId))
            .willReturn(Optional.of(record));

        // when & then
        assertThatThrownBy(() -> sut.submitReason(command))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("출석 체크 전 상태에서만 사유를 제출할 수 있습니다");
    }

    private AttendanceSheet createActiveSheet(Long sheetId) {
        Instant now = Instant.now();
        // Instant를 LocalDateTime으로 변환
        java.time.LocalDateTime startTime = java.time.LocalDateTime.ofInstant(now, ScheduleConstants.KST);
        java.time.LocalDateTime endTime = startTime.plusHours(2);
        AttendanceWindow window = AttendanceWindow.from(startTime, endTime, 10);

        AttendanceSheet sheet = AttendanceSheet.builder()
            .scheduleId(1L)
            .gisuId(1L)
            .window(window)
            .requiresApproval(true)
            .build();

        ReflectionTestUtils.setField(sheet, "id", sheetId);
        ReflectionTestUtils.setField(sheet, "active", true);
        return sheet;
    }

    private AttendanceSheet createInactiveSheet(Long sheetId) {
        AttendanceSheet sheet = createActiveSheet(sheetId);
        sheet.deactivate();
        return sheet;
    }

    private AttendanceRecord createPendingRecord(Long sheetId, Long memberId) {
        AttendanceRecord record = AttendanceRecord.builder()
            .attendanceSheetId(sheetId)
            .memberId(memberId)
            .status(AttendanceStatus.PENDING)
            .build();

        ReflectionTestUtils.setField(record, "id", 1L);
        return record;
    }

    private AttendanceRecord createCheckedRecord(Long sheetId, Long memberId) {
        AttendanceRecord record = AttendanceRecord.builder()
            .attendanceSheetId(sheetId)
            .memberId(memberId)
            .status(AttendanceStatus.PRESENT)
            .checkedAt(java.time.LocalDateTime.now())
            .build();

        ReflectionTestUtils.setField(record, "id", 1L);
        return record;
    }
}
