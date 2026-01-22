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
import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleCommand;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import com.umc.product.support.UseCaseTestSupport;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class CreateScheduleUseCaseTest extends UseCaseTestSupport {
    @Autowired
    private CreateScheduleUseCase createScheduleUseCase;

    @Autowired
    private LoadSchedulePort loadSchedulePort;

    @Autowired
    private LoadAttendanceSheetPort loadAttendanceSheetPort;

    @Autowired
    private LoadAttendanceRecordPort loadAttendanceRecordPort;

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
        System.out.println("=== 저장된 Gisu ID: " + activeGisu.getId());

        authorMember = saveMemberPort.save(createMember("박세은", "세니", "test0@dd.com", 1L, 1L));
        System.out.println("=== 저장된 Member ID: " + authorMember.getId());

        authorChallenger = saveChallengerPort.save(
                createChallenger(authorMember.getId(), activeGisu.getId())
        );
        System.out.println("=== 저장된 Challenger ID: " + authorChallenger.getId());
        System.out.println("=== Challenger의 memberId: " + authorChallenger.getMemberId());
        System.out.println("=== Challenger의 gisuId: " + authorChallenger.getGisuId());
    }

    @Test
    void 참여자_없이_일정을_생성한다() {
        // given
        // Mock 객체가 올바른 DTO를 반환하도록 설정 (Stubbing)
        mockChallengerInfo(authorMember.getId(), activeGisu.getId(), authorChallenger.getId());

        CreateScheduleCommand command = CreateScheduleCommand.of(
                "9기 OT",
                LocalDateTime.of(2024, 3, 16, 10, 0),
                LocalDateTime.of(2024, 3, 16, 12, 0),
                false,
                "강남역 스터디룸",
                createPoint(37.4979, 127.0276),
                "OT입니다",
                List.of(),
                Set.of(ScheduleTag.ORIENTATION),
                authorMember.getId()
        );

        // when
        Long scheduleId = createScheduleUseCase.create(command);

        // then
        Schedule savedSchedule = loadSchedulePort.findById(scheduleId).orElseThrow();
        assertThat(savedSchedule.getName()).isEqualTo("9기 OT");
        assertThat(savedSchedule.getTags()).contains(ScheduleTag.ORIENTATION);
        assertThat(savedSchedule.getAuthorChallengerId()).isEqualTo(authorChallenger.getId());
        assertThat(savedSchedule.getLocation().getY()).isEqualTo(37.4979); // 위도 확인
    }

    @Test
    void 참여자와_함께_일정을_생성하면_출석부와_출석기록이_생성된다() {
        // given
        // Mock 객체가 올바른 DTO를 반환하도록 설정 (Stubbing)
        mockChallengerInfo(authorMember.getId(), activeGisu.getId(), authorChallenger.getId());

        Member participant1 = saveMemberPort.save(createMember("참여자1", "참여1", "test1@dd.com", 1L, 2L));
        Member participant2 = saveMemberPort.save(createMember("참여자2", "참여2", "test2@dd.com", 1L, 3L));

        CreateScheduleCommand command = CreateScheduleCommand.of(
                "스터디 모임",
                LocalDateTime.of(2024, 3, 20, 14, 0),
                LocalDateTime.of(2024, 3, 20, 16, 0),
                false,
                "홍대 카페",
                null,
                "스프링 스터디",
                List.of(participant1.getId(), participant2.getId()),
                Set.of(ScheduleTag.STUDY),
                authorMember.getId()
        );

        // when
        Long scheduleId = createScheduleUseCase.create(command);

        // then
        Schedule savedSchedule = loadSchedulePort.findById(scheduleId).orElseThrow();
        assertThat(savedSchedule.getName()).isEqualTo("스터디 모임");

        AttendanceSheet sheet = loadAttendanceSheetPort.findByScheduleId(scheduleId).orElseThrow();
        assertThat(sheet.getScheduleId()).isEqualTo(scheduleId);
        assertThat(sheet.isActive()).isTrue();
        assertThat(sheet.isRequiresApproval()).isFalse();

        List<AttendanceRecord> records = loadAttendanceRecordPort.findByAttendanceSheetId(sheet.getId());
        assertThat(records).hasSize(2);
        assertThat(records).allMatch(r -> r.getStatus() == AttendanceStatus.PENDING);
    }

    @Test
    void 종일_일정을_생성하면_시간이_00시부터_23시59분으로_조정된다() {
        // given
        mockChallengerInfo(authorMember.getId(), activeGisu.getId(), authorChallenger.getId());

        CreateScheduleCommand command = CreateScheduleCommand.of(
                "종일 행사",
                LocalDateTime.of(2024, 3, 16, 10, 0),
                LocalDateTime.of(2024, 3, 16, 12, 0),
                true,
                "컨퍼런스홀",
                createPoint(37.1234, 127.1234),
                "종일 진행",
                List.of(),
                Set.of(ScheduleTag.WORKSHOP),
                authorMember.getId()
        );

        // when
        Long scheduleId = createScheduleUseCase.create(command);

        // then
        Schedule savedSchedule = loadSchedulePort.findById(scheduleId).orElseThrow();
        assertThat(savedSchedule.getStartsAt()).isEqualTo(LocalDateTime.of(2024, 3, 16, 0, 0, 0));
        assertThat(savedSchedule.getEndsAt()).isEqualTo(LocalDateTime.of(2024, 3, 16, 23, 59, 59));
        assertThat(savedSchedule.isAllDay()).isTrue();
    }

    @Test
    void 활성_기수의_챌린저가_아닌_회원이_일정을_생성하면_예외가_발생한다() {
        // given
        Gisu pastGisu = manageGisuPort.save(createInactiveGisu(8L));

        // OB 멤버 생성
        Member pastMember = saveMemberPort.save(createMember("졸업생", "선배", "past@dd.com", 1L, 5L));

        // OB 멤버에 대한 8기 Challenger 저장
        saveChallengerPort.save(
                createChallenger(pastMember.getId(), pastGisu.getId())
        );

        given(getChallengerUseCase.getByMemberIdAndGisuId(pastMember.getId(), activeGisu.getId()))
                .willReturn(null);

        CreateScheduleCommand command = CreateScheduleCommand.of(
                "OB의 침입",
                LocalDateTime.of(2024, 3, 16, 10, 0),
                LocalDateTime.of(2024, 3, 16, 12, 0),
                false,
                "장소",
                null,
                "설명",
                List.of(),
                Set.of(ScheduleTag.GENERAL),
                pastMember.getId() // 과거 멤버 ID로 요청
        );

        // when & then
        assertThatThrownBy(() -> createScheduleUseCase.create(command))
                .isInstanceOf(BusinessException.class);
    }

    // ========== Fixture 메서드 ==========

    private Gisu createActiveGisu(Long generation) {
        return Gisu.builder()
                .generation(generation)
                .isActive(true)
                .startAt(LocalDateTime.of(2024, 3, 1, 0, 0))
                .endAt(LocalDateTime.of(2024, 8, 31, 23, 59))
                .build();
    }

    private Gisu createInactiveGisu(Long generation) {
        return Gisu.builder()
                .generation(generation)
                .isActive(false)
                .startAt(LocalDateTime.of(2023, 3, 1, 0, 0))
                .endAt(LocalDateTime.of(2023, 8, 31, 23, 59))
                .build();
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
    }

    private Point createPoint(double latitude, double longitude) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }
}
