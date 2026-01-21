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
import com.umc.product.schedule.application.port.in.command.dto.UpdateScheduleCommand;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.out.SaveSchedulePort;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.ScheduleType;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import com.umc.product.support.UseCaseTestSupport;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UpdateScheduleUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private UpdateScheduleUseCase updateScheduleUseCase;

    @Autowired
    private LoadSchedulePort loadSchedulePort;

    @Autowired
    private SaveSchedulePort saveSchedulePort;

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
        activeGisu = manageGisuPort.save(createActiveGisu(9L));
        authorMember = saveMemberPort.save(createMember("지우", "두쫀쿠", "author@test.com"));
        authorChallenger = saveChallengerPort.save(createChallenger(authorMember.getId(), activeGisu.getId()));
    }

    @Test
    void 일정_정보를_모두_수정한다() {
        // given
        Schedule schedule = saveSchedulePort.save(createSchedule(authorChallenger.getId()));

        LocalDateTime newStartsAt = LocalDateTime.now().plusDays(3);
        LocalDateTime newEndsAt = newStartsAt.plusHours(3);

        UpdateScheduleCommand command = UpdateScheduleCommand.of(
                schedule.getId(),
                "수정된 일정 제목",
                newStartsAt,
                newEndsAt,
                false,
                "수정된 장소",
                "수정된 설명",
                ScheduleType.EVENT
        );

        // when
        updateScheduleUseCase.update(command);

        // then
        Schedule updatedSchedule = loadSchedulePort.findById(schedule.getId()).orElseThrow();
        assertThat(updatedSchedule.getName()).isEqualTo("수정된 일정 제목");
        assertThat(updatedSchedule.getLocationName()).isEqualTo("수정된 장소");
        assertThat(updatedSchedule.getDescription()).isEqualTo("수정된 설명");
        assertThat(updatedSchedule.getType()).isEqualTo(ScheduleType.EVENT);
        assertThat(updatedSchedule.getStartsAt()).isEqualTo(newStartsAt);
        assertThat(updatedSchedule.getEndsAt()).isEqualTo(newEndsAt);
    }

    @Test
    void 일부_필드만_수정하면_해당_필드만_변경되고_나머지는_유지된다() {
        // given
        Schedule schedule = saveSchedulePort.save(createSchedule(authorChallenger.getId()));

        // 기존 값 백업 (검증용)
        LocalDateTime originalStart = schedule.getStartsAt();
        LocalDateTime originalEnd = schedule.getEndsAt();
        String originalLocation = schedule.getLocationName();

        // 제목과 설명만 변경하고, 나머지는 null로 요청
        UpdateScheduleCommand command = UpdateScheduleCommand.of(
                schedule.getId(),
                "부분 수정된 제목", // 변경할 값
                null,             // 변경 안 함 (기존 시간 유지)
                null,             // 변경 안 함
                null,             // 변경 안 함
                null,             // 변경 안 함 (기존 장소 유지)
                "부분 수정된 설명", // 변경할 값
                null              // 변경 안 함 (기존 타입 유지)
        );

        // when
        updateScheduleUseCase.update(command);

        // then
        Schedule updatedSchedule = loadSchedulePort.findById(schedule.getId()).orElseThrow();

        // 1. 변경 요청한 필드는 바뀌어야 함
        assertThat(updatedSchedule.getName()).isEqualTo("부분 수정된 제목");
        assertThat(updatedSchedule.getDescription()).isEqualTo("부분 수정된 설명");

        // 2. null로 보낸 필드는 기존 값이 유지되어야 함
        assertThat(updatedSchedule.getStartsAt()).isEqualTo(originalStart);
        assertThat(updatedSchedule.getEndsAt()).isEqualTo(originalEnd);
        assertThat(updatedSchedule.getLocationName()).isEqualTo(originalLocation);
        assertThat(updatedSchedule.getType()).isEqualTo(ScheduleType.TEAM_ACTIVITY); // 기존 타입
    }

    @Test
    void 시간_정보만_부분_수정해도_기존_데이터와_병합되어_업데이트된다() {
        // given
        Schedule schedule = saveSchedulePort.save(createSchedule(authorChallenger.getId()));
        String originalName = schedule.getName();

        // 시간만 변경 (1시간 뒤로 미룸)
        LocalDateTime newStart = schedule.getStartsAt().plusHours(1);
        LocalDateTime newEnd = schedule.getEndsAt().plusHours(1);

        UpdateScheduleCommand command = UpdateScheduleCommand.of(
                schedule.getId(),
                null,       // 이름 유지
                newStart,   // 시작 시간 변경
                newEnd,     // 종료 시간 변경
                null,       // 종일 여부 유지
                null,
                null,
                null
        );

        // when
        updateScheduleUseCase.update(command);

        // then
        Schedule updatedSchedule = loadSchedulePort.findById(schedule.getId()).orElseThrow();

        assertThat(updatedSchedule.getStartsAt()).isEqualTo(newStart);
        assertThat(updatedSchedule.getEndsAt()).isEqualTo(newEnd);
        assertThat(updatedSchedule.getName()).isEqualTo(originalName); // 이름은 그대로여야 함
    }

    @Test
    void 변경할_값을_아무것도_보내지_않으면_데이터가_유지된다() {
        // given
        Schedule schedule = saveSchedulePort.save(createSchedule(authorChallenger.getId()));

        // 모든 필드를 null로 전송
        UpdateScheduleCommand command = UpdateScheduleCommand.of(
                schedule.getId(),
                null, null, null, null, null, null, null
        );

        // when
        updateScheduleUseCase.update(command);

        // then
        Schedule notUpdatedSchedule = loadSchedulePort.findById(schedule.getId()).orElseThrow();

        // 모든 필드가 초기 생성 상태와 같아야 함
        assertThat(notUpdatedSchedule.getName()).isEqualTo("수정 테스트 일정");
        assertThat(notUpdatedSchedule.getLocationName()).isEqualTo("테스트 장소");
        assertThat(notUpdatedSchedule.getStartsAt()).isEqualTo(schedule.getStartsAt());
    }

    @Test
    void 종일_일정으로_수정하면_시간이_조정된다() {
        // given
        Schedule schedule = saveSchedulePort.save(createSchedule(authorChallenger.getId()));

        LocalDateTime startsAt = LocalDateTime.of(2026, 5, 10, 10, 0);
        LocalDateTime endsAt = LocalDateTime.of(2026, 5, 11, 18, 0);

        UpdateScheduleCommand command = UpdateScheduleCommand.of(
                schedule.getId(),
                "종일 일정",
                startsAt,
                endsAt,
                true,  // 종일
                null,
                null,
                ScheduleType.TEAM_ACTIVITY
        );

        // when
        updateScheduleUseCase.update(command);

        // then
        Schedule updatedSchedule = loadSchedulePort.findById(schedule.getId()).orElseThrow();
        assertThat(updatedSchedule.isAllDay()).isTrue();
        assertThat(updatedSchedule.getStartsAt()).isEqualTo(LocalDateTime.of(2026, 5, 10, 0, 0));
        assertThat(updatedSchedule.getEndsAt()).isEqualTo(LocalDateTime.of(2026, 5, 11, 23, 59, 59));
    }

    @Test
    void 존재하지_않는_일정을_수정하려_하면_예외가_발생한다() {
        // given
        Long nonExistentId = 99999L;

        UpdateScheduleCommand command = UpdateScheduleCommand.of(
                nonExistentId,
                "수정된 제목",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                false,
                "장소",
                "설명",
                ScheduleType.TEAM_ACTIVITY
        );

        // when & then
        assertThatThrownBy(() -> updateScheduleUseCase.update(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException be = (BusinessException) exception;
                    assertThat(be.getCode()).isEqualTo(ScheduleErrorCode.SCHEDULE_NOT_FOUND);
                });
    }

    // ========== Fixture Methods ==========

    private Schedule createSchedule(Long authorChallengerId) {
        return Schedule.builder()
                .name("수정 테스트 일정")
                .startsAt(LocalDateTime.now().plusDays(1))
                .endsAt(LocalDateTime.now().plusDays(1).plusHours(2))
                .isAllDay(false)
                .locationName("테스트 장소")
                .description("테스트 내용")
                .type(ScheduleType.TEAM_ACTIVITY)
                .authorChallengerId(authorChallengerId)
                .build();
    }

    private Gisu createActiveGisu(Long generation) {
        return Gisu.builder()
                .generation(generation)
                .isActive(true)
                .startAt(LocalDateTime.of(2024, 3, 1, 0, 0))
                .endAt(LocalDateTime.of(2024, 8, 31, 23, 59))
                .build();
    }

    private Member createMember(String name, String nickname, String email) {
        return Member.builder()
                .email(email)
                .name(name)
                .nickname(nickname)
                .schoolId(1L)
                .profileImageId(1L)
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
