package com.umc.product.schedule.application.port.in.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.out.SaveAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.SaveAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.SaveSchedulePort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import com.umc.product.support.UseCaseTestSupport;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DeleteScheduleUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private DeleteScheduleUseCase deleteScheduleUseCase;

    // 데이터 검증 및 조회를 위한 Ports
    @Autowired
    private LoadSchedulePort loadSchedulePort;
    @Autowired
    private LoadAttendanceSheetPort loadAttendanceSheetPort;
    @Autowired
    private LoadAttendanceRecordPort loadAttendanceRecordPort;

    // 테스트 데이터 셋업을 위한 Ports
    @Autowired
    private SaveSchedulePort saveSchedulePort;
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

    @BeforeEach
    void setUp() {
        // 기본 데이터 셋업 (기수, 멤버, 챌린저)
        activeGisu = manageGisuPort.save(createActiveGisu(9L));
        authorMember = saveMemberPort.save(createMember("지우", "두쫀쿠", "author@test.com"));
        authorChallenger = saveChallengerPort.save(createChallenger(authorMember.getId(), activeGisu.getId()));
    }

    @Test
    void 참여자가_있는_일정을_삭제하면_출석부와_출석기록도_함께_삭제된다() {
        // given
        // 1. 일정 생성
        Schedule schedule = saveSchedulePort.save(createSchedule(authorChallenger.getId()));

        // 2. 출석부 생성
        AttendanceSheet sheet = saveAttendanceSheetPort.save(createAttendanceSheet(schedule.getId()));

        // 3. 참여자 및 출석 기록 생성
        Member participant = saveMemberPort.save(createMember("참여자", "참여닉", "part@test.com"));
        saveAttendanceRecordPort.saveAllRecords(List.of(
            createAttendanceRecord(sheet.getId(), participant.getId())
        ));

        // when
        deleteScheduleUseCase.delete(schedule.getId());

        // then
        // 1. 일정이 삭제되었는지 확인
        assertThat(loadSchedulePort.existsById(schedule.getId())).isFalse();

        // 2. 출석부가 삭제되었는지 확인
        assertThat(loadAttendanceSheetPort.findByScheduleId(schedule.getId())).isEmpty();

        // 3. 출석 기록이 삭제되었는지 확인 (Sheet ID로 조회 시 비어있어야 함)
        assertThat(loadAttendanceRecordPort.findByAttendanceSheetId(sheet.getId())).isEmpty();
    }

    @Test
    void 참여자가_없는_즉_출석부가_없는_일정을_삭제한다() {
        // given
        Schedule schedule = saveSchedulePort.save(createSchedule(authorChallenger.getId()));

        // 출석부(Sheet)는 생성하지 않음

        // when
        deleteScheduleUseCase.delete(schedule.getId());

        // then
        assertThat(loadSchedulePort.existsById(schedule.getId())).isFalse();
    }

    @Test
    void 존재하지_않는_일정을_삭제하려_하면_예외가_발생한다() {
        // given
        Long nonExistentId = 99999L;

        // when & then
        assertThatThrownBy(() -> deleteScheduleUseCase.delete(nonExistentId))
            .isInstanceOf(BusinessException.class)
            .satisfies(exception -> {
                BusinessException be = (BusinessException) exception;
                assertThat(be.getCode()).isEqualTo(ScheduleErrorCode.SCHEDULE_NOT_FOUND);
            });
    }

    // ========== Fixture Methods ==========

    private Schedule createSchedule(Long authorChallengerId) {
        return Schedule.builder()
            .name("삭제 테스트 일정")
            .startsAt(LocalDateTime.now().plusDays(1))
            .endsAt(LocalDateTime.now().plusDays(1).plusHours(2))
            .isAllDay(false)
            .locationName("테스트 장소")
            .description("테스트 내용")
            .tags(Set.of(ScheduleTag.PROJECT))
            .authorChallengerId(authorChallengerId)
            .build();
    }

    private AttendanceSheet createAttendanceSheet(Long scheduleId) {
        return AttendanceSheet.builder()
            .scheduleId(scheduleId)
            .window(AttendanceWindow.ofDefault(LocalDateTime.now().plusDays(1)))
            .requiresApproval(false)
            .build();
    }

    private AttendanceRecord createAttendanceRecord(Long sheetId, Long memberId) {
        return AttendanceRecord.builder()
            .attendanceSheetId(sheetId)
            .memberId(memberId)
            .status(AttendanceStatus.PENDING)
            .build();
    }

    private Gisu createActiveGisu(Long generation) {
        return Gisu.builder()
            .generation(generation)
            .isActive(true)
            .startAt(Instant.parse("2026-03-01T00:00:00Z"))
            .endAt(Instant.parse("2026-08-31T00:00:00Z"))
            .build();
    }

    private Member createMember(String name, String nickname, String email) {
        return Member.builder()
            .email(email)
            .name(name)
            .nickname(nickname)
            .schoolId(1L)
            .profileImageId("1")
            .build();
    }

    private Challenger createChallenger(Long memberId, Long gisuId) {
        return Challenger.builder()
            .memberId(memberId)
            .gisuId(gisuId)
            .part(ChallengerPart.SPRINGBOOT)
            .build();
    }
}
