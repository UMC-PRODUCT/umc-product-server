//package com.umc.product.curriculum.application.port.in.query;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import com.umc.product.challenger.domain.Challenger;
//import com.umc.product.common.domain.enums.ChallengerPart;
//import com.umc.product.curriculum.adapter.out.persistence.CurriculumJpaRepository;
//import com.umc.product.curriculum.adapter.out.persistence.OriginalWorkbookJpaRepository;
//import com.umc.product.curriculum.application.port.in.query.dto.GetWorkbookSubmissionsQuery;
//import com.umc.product.curriculum.application.port.in.query.dto.StudyGroupFilterInfo;
//import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionInfo;
//import com.umc.product.curriculum.domain.ChallengerWorkbook;
//import com.umc.product.curriculum.domain.Curriculum;
//import com.umc.product.curriculum.domain.OriginalWorkbook;
//import com.umc.product.curriculum.domain.enums.MissionType;
//import com.umc.product.curriculum.domain.enums.WorkbookStatus;
//import com.umc.product.member.adapter.out.persistence.MemberJpaRepository;
//import com.umc.product.member.domain.Member;
//import com.umc.product.organization.application.port.out.command.ManageGisuPort;
//import com.umc.product.organization.application.port.out.command.ManageSchoolPort;
//import com.umc.product.organization.application.port.out.command.ManageStudyGroupMemberPort;
//import com.umc.product.organization.application.port.out.command.ManageStudyGroupPort;
//import com.umc.product.organization.domain.Gisu;
//import com.umc.product.organization.domain.School;
//import com.umc.product.organization.domain.StudyGroup;
//import com.umc.product.support.TestChallengerRepository;
//import com.umc.product.support.UseCaseTestSupport;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//class GetWorkbookSubmissionsUseCaseTest extends UseCaseTestSupport {
//
//    @Autowired
//    private GetWorkbookSubmissionsUseCase getWorkbookSubmissionsUseCase;
//
//    @Autowired
//    private GetStudyGroupsForFilterUseCase getStudyGroupsForFilterUseCase;
//
//    @Autowired
//    private ManageGisuPort manageGisuPort;
//
//    @Autowired
//    private ManageSchoolPort manageSchoolPort;
//
//    @Autowired
//    private ManageStudyGroupPort manageStudyGroupPort;
//
//    @Autowired
//    private ManageStudyGroupMemberPort manageStudyGroupMemberPort;
//
//    @Autowired
//    private MemberJpaRepository memberJpaRepository;
//
//    @Autowired
//    private TestChallengerRepository challengerRepository;
//
//    @Autowired
//    private CurriculumJpaRepository curriculumJpaRepository;
//
//    @Autowired
//    private OriginalWorkbookJpaRepository originalWorkbookJpaRepository;
//
//    @Autowired
//    private ChallengerWorkbookJpaRepository challengerWorkbookJpaRepository;
//
//    @Test
//    void 주차별_워크북_제출_현황을_조회한다() {
//        // given
//        Gisu gisu = manageGisuPort.save(createActiveGisu(9L));
//        School school = manageSchoolPort.save(School.create("서울대학교", "비고"));
//
//        Member member = memberJpaRepository.save(createMember("홍길동", school.getId()));
//        Challenger challenger = challengerRepository.save(
//                new Challenger(member.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));
//
//        Curriculum curriculum = curriculumJpaRepository.save(createCurriculum(gisu.getId(), ChallengerPart.SPRINGBOOT));
//        OriginalWorkbook workbook = originalWorkbookJpaRepository.save(createWorkbook(curriculum, 1, "1주차 워크북"));
//
//        challengerWorkbookJpaRepository.save(
//                createChallengerWorkbook(challenger.getId(), workbook.getId(), WorkbookStatus.SUBMITTED));
//
//        // when
//        GetWorkbookSubmissionsQuery query = new GetWorkbookSubmissionsQuery(null, 1, null, null, 20);
//        List<WorkbookSubmissionInfo> result = getWorkbookSubmissionsUseCase.getSubmissions(query);
//
//        // then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).challengerName()).isEqualTo("홍길동");
//        assertThat(result.get(0).schoolName()).isEqualTo("서울대학교");
//        assertThat(result.get(0).status()).isEqualTo(WorkbookStatus.SUBMITTED);
//    }
//
//    @Test
//    void 학교_필터링으로_조회한다() {
//        // given
//        Gisu gisu = manageGisuPort.save(createActiveGisu(9L));
//        School school1 = manageSchoolPort.save(School.create("서울대학교", "비고"));
//        School school2 = manageSchoolPort.save(School.create("연세대학교", "비고"));
//
//        Member member1 = memberJpaRepository.save(createMember("서울대생", school1.getId()));
//        Member member2 = memberJpaRepository.save(createMember("연세대생", school2.getId()));
//
//        Challenger challenger1 = challengerRepository.save(
//                new Challenger(member1.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));
//        Challenger challenger2 = challengerRepository.save(
//                new Challenger(member2.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));
//
//        Curriculum curriculum = curriculumJpaRepository.save(createCurriculum(gisu.getId(), ChallengerPart.SPRINGBOOT));
//        OriginalWorkbook workbook = originalWorkbookJpaRepository.save(createWorkbook(curriculum, 1, "1주차 워크북"));
//
//        challengerWorkbookJpaRepository.save(
//                createChallengerWorkbook(challenger1.getId(), workbook.getId(), WorkbookStatus.SUBMITTED));
//        challengerWorkbookJpaRepository.save(
//                createChallengerWorkbook(challenger2.getId(), workbook.getId(), WorkbookStatus.SUBMITTED));
//
//        // when
//        GetWorkbookSubmissionsQuery query = new GetWorkbookSubmissionsQuery(school1.getId(), 1, null, null, 20);
//        List<WorkbookSubmissionInfo> result = getWorkbookSubmissionsUseCase.getSubmissions(query);
//
//        // then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).schoolName()).isEqualTo("서울대학교");
//    }
//
//    @Test
//    void 스터디_그룹_필터링으로_조회한다() {
//        // given
//        Gisu gisu = manageGisuPort.save(createActiveGisu(9L));
//        School school = manageSchoolPort.save(School.create("서울대학교", "비고"));
//
//        Member member1 = memberJpaRepository.save(createMember("그룹원1", school.getId()));
//        Member member2 = memberJpaRepository.save(createMember("그룹원2", school.getId()));
//        Member member3 = memberJpaRepository.save(createMember("비그룹원", school.getId()));
//
//        Challenger challenger1 = challengerRepository.save(
//                new Challenger(member1.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));
//        Challenger challenger2 = challengerRepository.save(
//                new Challenger(member2.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));
//        Challenger challenger3 = challengerRepository.save(
//                new Challenger(member3.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));
//
//        StudyGroup studyGroup = manageStudyGroupPort.save(
//                createStudyGroup("스프링 1조", gisu, school.getId(), ChallengerPart.SPRINGBOOT));
//        studyGroup.addMember(challenger1.getId(), true);
//        studyGroup.addMember(challenger2.getId(), false);
//        manageStudyGroupPort.save(studyGroup);
//
//        Curriculum curriculum = curriculumJpaRepository.save(createCurriculum(gisu.getId(), ChallengerPart.SPRINGBOOT));
//        OriginalWorkbook workbook = originalWorkbookJpaRepository.save(createWorkbook(curriculum, 1, "1주차 워크북"));
//
//        challengerWorkbookJpaRepository.save(
//                createChallengerWorkbook(challenger1.getId(), workbook.getId(), WorkbookStatus.SUBMITTED));
//        challengerWorkbookJpaRepository.save(
//                createChallengerWorkbook(challenger2.getId(), workbook.getId(), WorkbookStatus.SUBMITTED));
//        challengerWorkbookJpaRepository.save(
//                createChallengerWorkbook(challenger3.getId(), workbook.getId(), WorkbookStatus.SUBMITTED));
//
//        // when
//        GetWorkbookSubmissionsQuery query = new GetWorkbookSubmissionsQuery(null, 1, studyGroup.getId(), null, 20);
//        List<WorkbookSubmissionInfo> result = getWorkbookSubmissionsUseCase.getSubmissions(query);
//
//        // then
//        assertThat(result).hasSize(2);
//    }
//
//    @Test
//    void 커서_기반_페이지네이션으로_조회한다() {
//        // given
//        Gisu gisu = manageGisuPort.save(createActiveGisu(9L));
//        School school = manageSchoolPort.save(School.create("서울대학교", "비고"));
//
//        Curriculum curriculum = curriculumJpaRepository.save(createCurriculum(gisu.getId(), ChallengerPart.SPRINGBOOT));
//        OriginalWorkbook workbook = originalWorkbookJpaRepository.save(createWorkbook(curriculum, 1, "1주차 워크북"));
//
//        // 5명의 챌린저 생성
//        for (int i = 1; i <= 5; i++) {
//            Member member = memberJpaRepository.save(createMember("챌린저" + i, school.getId()));
//            Challenger challenger = challengerRepository.save(
//                    new Challenger(member.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));
//            challengerWorkbookJpaRepository.save(
//                    createChallengerWorkbook(challenger.getId(), workbook.getId(), WorkbookStatus.SUBMITTED));
//        }
//
//        // when - 첫 페이지 (size=2)
//        GetWorkbookSubmissionsQuery firstQuery = new GetWorkbookSubmissionsQuery(null, 1, null, null, 2);
//        List<WorkbookSubmissionInfo> firstPage = getWorkbookSubmissionsUseCase.getSubmissions(firstQuery);
//
//        // then - fetchSize(3)만큼 조회
//        assertThat(firstPage).hasSize(3);
//
//        // when - 두 번째 페이지
//        Long cursor = firstPage.get(1).challengerWorkbookId();
//        GetWorkbookSubmissionsQuery secondQuery = new GetWorkbookSubmissionsQuery(null, 1, null, cursor, 2);
//        List<WorkbookSubmissionInfo> secondPage = getWorkbookSubmissionsUseCase.getSubmissions(secondQuery);
//
//        // then
//        assertThat(secondPage).hasSize(3);
//    }
//
//    @Test
//    void 필터용_스터디_그룹_목록을_조회한다() {
//        // given
//        Gisu gisu = manageGisuPort.save(createActiveGisu(9L));
//        School school = manageSchoolPort.save(School.create("서울대학교", "비고"));
//
//        manageStudyGroupPort.save(createStudyGroup("스프링 1조", gisu, school.getId(), ChallengerPart.SPRINGBOOT));
//        manageStudyGroupPort.save(createStudyGroup("스프링 2조", gisu, school.getId(), ChallengerPart.SPRINGBOOT));
//        manageStudyGroupPort.save(createStudyGroup("웹 1조", gisu, school.getId(), ChallengerPart.WEB));
//
//        // when
//        List<StudyGroupFilterInfo> result = getStudyGroupsForFilterUseCase.getStudyGroupsForFilter(
//                school.getId(), ChallengerPart.SPRINGBOOT);
//
//        // then
//        assertThat(result).hasSize(2);
//        assertThat(result).extracting(StudyGroupFilterInfo::name)
//                .containsExactlyInAnyOrder("스프링 1조", "스프링 2조");
//    }
//
//    @Test
//    void 스터디_그룹이_없으면_빈_목록을_반환한다() {
//        // when
//        List<StudyGroupFilterInfo> result = getStudyGroupsForFilterUseCase.getStudyGroupsForFilter(
//                999L, ChallengerPart.SPRINGBOOT);
//
//        // then
//        assertThat(result).isEmpty();
//    }
//
//    private Gisu createActiveGisu(Long generation) {
//        return Gisu.builder()
//                .generation(generation)
//                .isActive(true)
//                .startAt(LocalDateTime.of(2024, 3, 1, 0, 0))
//                .endAt(LocalDateTime.of(2024, 8, 31, 23, 59))
//                .build();
//    }
//
//    private Member createMember(String nickname, Long schoolId) {
//        return Member.builder()
//                .name(nickname)
//                .nickname(nickname)
//                .email(nickname + "@test.com")
//                .schoolId(schoolId)
//                .build();
//    }
//
//    private Curriculum createCurriculum(Long gisuId, ChallengerPart part) {
//        return Curriculum.builder()
//                .gisuId(gisuId)
//                .part(part)
//                .title("9기 " + part.name())
//                .build();
//    }
//
//    private OriginalWorkbook createWorkbook(Curriculum curriculum, int weekNo, String title) {
//        return OriginalWorkbook.builder()
//                .curriculum(curriculum)
//                .weekNo(weekNo)
//                .title(title)
//                .startDate(LocalDate.of(2024, 3, 1))
//                .endDate(LocalDate.of(2024, 3, 7))
//                .missionType(MissionType.LINK)
//                .build();
//    }
//
//    private ChallengerWorkbook createChallengerWorkbook(Long challengerId, Long workbookId, WorkbookStatus status) {
//        return ChallengerWorkbook.builder()
//                .challengerId(challengerId)
//                .originalWorkbookId(workbookId)
//                .scheduleId(1L)
//                .status(status)
//                .build();
//    }
//
//    private StudyGroup createStudyGroup(String name, Gisu gisu, Long schoolId, ChallengerPart part) {
//        return StudyGroup.builder()
//                .name(name)
//                .gisu(gisu)
//                .schoolId(schoolId)
//                .part(part)
//                .build();
//    }
//}
