package com.umc.product.schedule.application.port.in.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.global.util.GeometryUtils;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.application.port.in.query.GetStudyGroupUseCase;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupDetailInfo;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import com.umc.product.schedule.application.port.in.command.dto.CreateStudyGroupScheduleCommand;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import com.umc.product.support.TestChallengerRepository;
import com.umc.product.support.TestMemberRepository;
import com.umc.product.support.UseCaseTestSupport;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

// TODO: UseCaseTestSupport에서 GetChallengerUseCase 제거하고 개선예정
class CreateStudyGroupScheduleUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private CreateStudyGroupScheduleUseCase createStudyGroupScheduleUseCase;

    @Autowired
    private LoadSchedulePort loadSchedulePort;

    @Autowired
    private LoadAttendanceSheetPort loadAttendanceSheetPort;

    @Autowired
    private LoadAttendanceRecordPort loadAttendanceRecordPort;

    @Autowired
    private ManageGisuPort manageGisuPort;

    @Autowired
    private TestMemberRepository memberRepository;

    @Autowired
    private TestChallengerRepository challengerRepository;

    @MockitoBean
    private GetStudyGroupUseCase getStudyGroupUseCase;

    private Gisu activeGisu;
    private Member authorMember;
    private Challenger authorChallenger;

    @BeforeEach
    void setUp() {
        activeGisu = manageGisuPort.save(createActiveGisu(9L));
        authorMember = memberRepository.save(createMember("작성자", "author@test.com"));
        authorChallenger = challengerRepository.save(
            new Challenger(authorMember.getId(), ChallengerPart.SPRINGBOOT, activeGisu.getId())
        );

        mockChallengerInfo(authorMember.getId(), activeGisu.getId(), authorChallenger.getId());
    }

    @Nested
    @DisplayName("스터디 그룹 일정 생성")
    class CreateStudyGroupScheduleTest {

        @Test
        void 스터디_그룹_일정을_생성하면_전체_멤버가_출석_대상으로_등록된다() {
            // given
            Member member1 = memberRepository.save(createMember("멤버1", "m1@test.com"));
            Member member2 = memberRepository.save(createMember("멤버2", "m2@test.com"));
            Challenger challenger1 = challengerRepository.save(
                new Challenger(member1.getId(), ChallengerPart.SPRINGBOOT, activeGisu.getId())
            );
            Challenger challenger2 = challengerRepository.save(
                new Challenger(member2.getId(), ChallengerPart.SPRINGBOOT, activeGisu.getId())
            );

            mockChallengerInfo(member1.getId(), activeGisu.getId(), challenger1.getId());
            mockChallengerInfo(member2.getId(), activeGisu.getId(), challenger2.getId());

            Long studyGroupId = 100L;
            given(getStudyGroupUseCase.getStudyGroupDetail(studyGroupId))
                .willReturn(new StudyGroupDetailInfo(
                    studyGroupId,
                    "Spring A팀",
                    ChallengerPart.SPRINGBOOT,
                    List.of(),
                    Instant.now(),
                    3,
                    new StudyGroupDetailInfo.MemberInfo(
                        authorChallenger.getId(), authorMember.getId(), "작성자", null),
                    List.of(
                        new StudyGroupDetailInfo.MemberInfo(
                            challenger1.getId(), member1.getId(), "멤버1", null),
                        new StudyGroupDetailInfo.MemberInfo(
                            challenger2.getId(), member2.getId(), "멤버2", null)
                    )
                ));

            CreateStudyGroupScheduleCommand command = new CreateStudyGroupScheduleCommand(
                "스터디 정기 모임",
                LocalDateTime.of(2026, 3, 20, 14, 0),
                LocalDateTime.of(2026, 3, 20, 16, 0),
                false,
                "강남역 스터디룸",
                GeometryUtils.createPoint(37.4979, 127.0276),
                "스프링 스터디",
                Set.of(ScheduleTag.STUDY),
                studyGroupId,
                activeGisu.getId(),
                false,
                authorMember.getId()
            );

            // when
            Long scheduleId = createStudyGroupScheduleUseCase.create(command);

            // then
            Schedule savedSchedule = loadSchedulePort.findById(scheduleId).orElseThrow();
            assertThat(savedSchedule.getName()).isEqualTo("스터디 정기 모임");
            assertThat(savedSchedule.getStudyGroupId()).isEqualTo(studyGroupId);
            assertThat(savedSchedule.getAuthorChallengerId()).isEqualTo(authorChallenger.getId());

            AttendanceSheet sheet = loadAttendanceSheetPort.findByScheduleId(scheduleId).orElseThrow();
            assertThat(sheet.getScheduleId()).isEqualTo(scheduleId);

            List<AttendanceRecord> records = loadAttendanceRecordPort.findByAttendanceSheetId(sheet.getId());
            assertThat(records).hasSize(3);
            assertThat(records).allMatch(r -> r.getStatus() == AttendanceStatus.PENDING);
        }

        @Test
        void 리더만_있는_스터디_그룹이면_리더_한명만_출석_대상으로_등록된다() {
            // given
            Long studyGroupId = 200L;
            given(getStudyGroupUseCase.getStudyGroupDetail(studyGroupId))
                .willReturn(new StudyGroupDetailInfo(
                    studyGroupId,
                    "1인 스터디",
                    ChallengerPart.SPRINGBOOT,
                    List.of(),
                    Instant.now(),
                    1,
                    new StudyGroupDetailInfo.MemberInfo(
                        authorChallenger.getId(), authorMember.getId(), "작성자", null),
                    List.of()
                ));

            CreateStudyGroupScheduleCommand command = new CreateStudyGroupScheduleCommand(
                "혼자 스터디",
                LocalDateTime.of(2026, 3, 25, 10, 0),
                LocalDateTime.of(2026, 3, 25, 12, 0),
                false,
                "집",
                null,
                "혼자 공부",
                Set.of(ScheduleTag.STUDY),
                studyGroupId,
                activeGisu.getId(),
                false,
                authorMember.getId()
            );

            // when
            Long scheduleId = createStudyGroupScheduleUseCase.create(command);

            // then
            Schedule savedSchedule = loadSchedulePort.findById(scheduleId).orElseThrow();
            assertThat(savedSchedule.getStudyGroupId()).isEqualTo(studyGroupId);

            AttendanceSheet sheet = loadAttendanceSheetPort.findByScheduleId(scheduleId).orElseThrow();
            List<AttendanceRecord> records = loadAttendanceRecordPort.findByAttendanceSheetId(sheet.getId());
            assertThat(records).hasSize(1);
        }

        @Test
        void 존재하지_않는_스터디_그룹이면_예외가_발생한다() {
            // given
            Long invalidStudyGroupId = 999L;
            given(getStudyGroupUseCase.getStudyGroupDetail(invalidStudyGroupId))
                .willThrow(new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_NOT_FOUND));

            CreateStudyGroupScheduleCommand command = new CreateStudyGroupScheduleCommand(
                "존재하지 않는 그룹",
                LocalDateTime.of(2026, 3, 20, 14, 0),
                LocalDateTime.of(2026, 3, 20, 16, 0),
                false,
                "장소",
                null,
                "설명",
                Set.of(ScheduleTag.STUDY),
                invalidStudyGroupId,
                activeGisu.getId(),
                false,
                authorMember.getId()
            );

            // when & then
            assertThatThrownBy(() -> createStudyGroupScheduleUseCase.create(command))
                .isInstanceOf(OrganizationDomainException.class);
        }
    }

    // ========== Fixture 메서드 ==========

    private Gisu createActiveGisu(Long generation) {
        return Gisu.create(
            generation,
            Instant.parse("2024-03-01T00:00:00Z"),
            Instant.parse("2026-08-31T00:00:00Z"),
            true
        );
    }

    private Member createMember(String name, String email) {
        return Member.builder()
            .name(name)
            .nickname(name)
            .email(email)
            .schoolId(1L)
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

        ChallengerInfoWithStatus mockInfoWithStatus = ChallengerInfoWithStatus.builder()
            .challengerId(challengerId)
            .memberId(memberId)
            .gisuId(gisuId)
            .part(ChallengerPart.SPRINGBOOT)
            .status(ChallengerStatus.ACTIVE)
            .build();

        given(getChallengerUseCase.getLatestActiveChallengerByMemberId(memberId))
            .willReturn(mockInfoWithStatus);
    }
}
