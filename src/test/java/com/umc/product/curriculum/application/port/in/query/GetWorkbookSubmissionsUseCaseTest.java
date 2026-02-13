package com.umc.product.curriculum.application.port.in.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.authorization.adapter.out.persistence.ChallengerRoleJpaRepository;
import com.umc.product.authorization.domain.ChallengerRole;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.curriculum.adapter.out.persistence.ChallengerWorkbookJpaRepository;
import com.umc.product.curriculum.adapter.out.persistence.CurriculumJpaRepository;
import com.umc.product.curriculum.adapter.out.persistence.OriginalWorkbookJpaRepository;
import com.umc.product.curriculum.application.port.in.query.dto.GetWorkbookSubmissionsQuery;
import com.umc.product.curriculum.application.port.in.query.dto.StudyGroupFilterInfo;
import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionDetailInfo;
import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionInfo;
import com.umc.product.curriculum.domain.ChallengerWorkbook;
import com.umc.product.curriculum.domain.Curriculum;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.enums.MissionType;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.application.port.in.query.GetSchoolAccessContextUseCase;
import com.umc.product.organization.application.port.in.query.dto.SchoolAccessContext;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.application.port.out.command.ManageSchoolPort;
import com.umc.product.organization.application.port.out.command.ManageStudyGroupPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.organization.domain.StudyGroup;
import com.umc.product.support.TestChallengerRepository;
import com.umc.product.support.TestMemberRepository;
import com.umc.product.support.UseCaseTestSupport;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GetWorkbookSubmissionsUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private GetWorkbookSubmissionsUseCase getWorkbookSubmissionsUseCase;

    @Autowired
    private GetSchoolAccessContextUseCase getSchoolAccessContextUseCase;

    @Autowired
    private GetStudyGroupsForFilterUseCase getStudyGroupsForFilterUseCase;

    @Autowired
    private ManageGisuPort manageGisuPort;

    @Autowired
    private ManageSchoolPort manageSchoolPort;

    @Autowired
    private ManageStudyGroupPort manageStudyGroupPort;

    @Autowired
    private TestMemberRepository memberRepository;

    @Autowired
    private TestChallengerRepository challengerRepository;

    @Autowired
    private ChallengerRoleJpaRepository challengerRoleJpaRepository;

    @Autowired
    private CurriculumJpaRepository curriculumJpaRepository;

    @Autowired
    private OriginalWorkbookJpaRepository originalWorkbookJpaRepository;

    @Autowired
    private ChallengerWorkbookJpaRepository challengerWorkbookJpaRepository;

    @Test
    void 주차별_워크북_제출_현황을_조회한다() {
        // given
        Gisu gisu = manageGisuPort.save(createActiveGisu(9L));
        School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

        Member member = memberRepository.save(createMember("홍길동", school.getId()));
        Challenger challenger = challengerRepository.save(
            new Challenger(member.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));

        Curriculum curriculum = curriculumJpaRepository.save(createCurriculum(gisu.getId(), ChallengerPart.SPRINGBOOT));
        OriginalWorkbook workbook = originalWorkbookJpaRepository.save(createWorkbook(curriculum, 1, "1주차 워크북"));

        challengerWorkbookJpaRepository.save(
            createChallengerWorkbook(challenger.getId(), workbook.getId(), WorkbookStatus.SUBMITTED));

        // when
        GetWorkbookSubmissionsQuery query = new GetWorkbookSubmissionsQuery(null, 1, null, null, null, 20);
        List<WorkbookSubmissionInfo> result = getWorkbookSubmissionsUseCase.getSubmissions(query);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).challengerName()).isEqualTo("홍길동");
        assertThat(result.get(0).schoolName()).isEqualTo("서울대학교");
        assertThat(result.get(0).status()).isEqualTo(WorkbookStatus.SUBMITTED);
    }

    @Test
    void 학교_필터링으로_조회한다() {
        // given
        Gisu gisu = manageGisuPort.save(createActiveGisu(9L));
        School school1 = manageSchoolPort.save(School.create("서울대학교", "비고"));
        School school2 = manageSchoolPort.save(School.create("연세대학교", "비고"));

        Member member1 = memberRepository.save(createMember("서울대생", school1.getId()));
        Member member2 = memberRepository.save(createMember("연세대생", school2.getId()));

        Challenger challenger1 = challengerRepository.save(
            new Challenger(member1.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));
        Challenger challenger2 = challengerRepository.save(
            new Challenger(member2.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));

        Curriculum curriculum = curriculumJpaRepository.save(createCurriculum(gisu.getId(), ChallengerPart.SPRINGBOOT));
        OriginalWorkbook workbook = originalWorkbookJpaRepository.save(createWorkbook(curriculum, 1, "1주차 워크북"));

        challengerWorkbookJpaRepository.save(
            createChallengerWorkbook(challenger1.getId(), workbook.getId(), WorkbookStatus.SUBMITTED));
        challengerWorkbookJpaRepository.save(
            createChallengerWorkbook(challenger2.getId(), workbook.getId(), WorkbookStatus.SUBMITTED));

        // when
        GetWorkbookSubmissionsQuery query = new GetWorkbookSubmissionsQuery(school1.getId(), 1, null, null, null, 20);
        List<WorkbookSubmissionInfo> result = getWorkbookSubmissionsUseCase.getSubmissions(query);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).schoolName()).isEqualTo("서울대학교");
    }

    @Test
    void 스터디_그룹_필터링으로_조회한다() {
        // given
        Gisu gisu = manageGisuPort.save(createActiveGisu(9L));
        School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

        Member member1 = memberRepository.save(createMember("그룹원1", school.getId()));
        Member member2 = memberRepository.save(createMember("그룹원2", school.getId()));
        Member member3 = memberRepository.save(createMember("비그룹원", school.getId()));

        Challenger challenger1 = challengerRepository.save(
            new Challenger(member1.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));
        Challenger challenger2 = challengerRepository.save(
            new Challenger(member2.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));
        Challenger challenger3 = challengerRepository.save(
            new Challenger(member3.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));

        StudyGroup studyGroup = manageStudyGroupPort.save(
            createStudyGroup("스프링 1조", gisu, ChallengerPart.SPRINGBOOT));
        studyGroup.addMember(challenger1.getId(), true);
        studyGroup.addMember(challenger2.getId(), false);
        manageStudyGroupPort.save(studyGroup);

        Curriculum curriculum = curriculumJpaRepository.save(createCurriculum(gisu.getId(), ChallengerPart.SPRINGBOOT));
        OriginalWorkbook workbook = originalWorkbookJpaRepository.save(createWorkbook(curriculum, 1, "1주차 워크북"));

        challengerWorkbookJpaRepository.save(
            createChallengerWorkbook(challenger1.getId(), workbook.getId(), WorkbookStatus.SUBMITTED));
        challengerWorkbookJpaRepository.save(
            createChallengerWorkbook(challenger2.getId(), workbook.getId(), WorkbookStatus.SUBMITTED));
        challengerWorkbookJpaRepository.save(
            createChallengerWorkbook(challenger3.getId(), workbook.getId(), WorkbookStatus.SUBMITTED));

        // when
        GetWorkbookSubmissionsQuery query = new GetWorkbookSubmissionsQuery(null, 1, studyGroup.getId(), null, null,
            20);
        List<WorkbookSubmissionInfo> result = getWorkbookSubmissionsUseCase.getSubmissions(query);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void 커서_기반_페이지네이션으로_조회한다() {
        // given
        Gisu gisu = manageGisuPort.save(createActiveGisu(9L));
        School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

        Curriculum curriculum = curriculumJpaRepository.save(createCurriculum(gisu.getId(), ChallengerPart.SPRINGBOOT));
        OriginalWorkbook workbook = originalWorkbookJpaRepository.save(createWorkbook(curriculum, 1, "1주차 워크북"));

        // 5명의 챌린저 생성
        for (int i = 1; i <= 5; i++) {
            Member member = memberRepository.save(createMember("챌린저" + i, school.getId()));
            Challenger challenger = challengerRepository.save(
                new Challenger(member.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));
            challengerWorkbookJpaRepository.save(
                createChallengerWorkbook(challenger.getId(), workbook.getId(), WorkbookStatus.SUBMITTED));
        }

        // when - 첫 페이지 (size=2)
        GetWorkbookSubmissionsQuery firstQuery = new GetWorkbookSubmissionsQuery(null, 1, null, null, null, 2);
        List<WorkbookSubmissionInfo> firstPage = getWorkbookSubmissionsUseCase.getSubmissions(firstQuery);

        // then - fetchSize(3)만큼 조회
        assertThat(firstPage).hasSize(3);

        // when - 두 번째 페이지
        Long cursor = firstPage.get(1).challengerWorkbookId();
        GetWorkbookSubmissionsQuery secondQuery = new GetWorkbookSubmissionsQuery(null, 1, null, null, cursor, 2);
        List<WorkbookSubmissionInfo> secondPage = getWorkbookSubmissionsUseCase.getSubmissions(secondQuery);

        // then
        assertThat(secondPage).hasSize(3);
    }

    @Test
    void 필터용_스터디_그룹_목록을_조회한다() {
        // given
        Gisu gisu = manageGisuPort.save(createActiveGisu(9L));
        School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

        // 멤버와 챌린저 생성 (schoolId 기반 필터링을 위해 필요)
        Member member1 = memberRepository.save(createMember("멤버1", school.getId()));
        Member member2 = memberRepository.save(createMember("멤버2", school.getId()));
        Member member3 = memberRepository.save(createMember("멤버3", school.getId()));

        Challenger challenger1 = challengerRepository.save(
            new Challenger(member1.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));
        Challenger challenger2 = challengerRepository.save(
            new Challenger(member2.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));
        Challenger challenger3 = challengerRepository.save(
            new Challenger(member3.getId(), ChallengerPart.WEB, gisu.getId()));

        // 스터디 그룹 생성 및 멤버 추가
        StudyGroup springGroup1 = createStudyGroup("스프링 1조", gisu, ChallengerPart.SPRINGBOOT);
        springGroup1.addMember(challenger1.getId());
        manageStudyGroupPort.save(springGroup1);

        StudyGroup springGroup2 = createStudyGroup("스프링 2조", gisu, ChallengerPart.SPRINGBOOT);
        springGroup2.addMember(challenger2.getId());
        manageStudyGroupPort.save(springGroup2);

        StudyGroup webGroup = createStudyGroup("웹 1조", gisu, ChallengerPart.WEB);
        webGroup.addMember(challenger3.getId());
        manageStudyGroupPort.save(webGroup);

        // when
        List<StudyGroupFilterInfo> result = getStudyGroupsForFilterUseCase.getStudyGroupsForFilter(
            school.getId(), ChallengerPart.SPRINGBOOT);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(StudyGroupFilterInfo::name)
            .containsExactlyInAnyOrder("스프링 1조", "스프링 2조");
    }

    @Test
    void 스터디_그룹이_없으면_빈_목록을_반환한다() {
        // when
        List<StudyGroupFilterInfo> result = getStudyGroupsForFilterUseCase.getStudyGroupsForFilter(
            999L, ChallengerPart.SPRINGBOOT);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 챌린저_워크북의_제출_URL을_조회한다() {
        // given
        Gisu gisu = manageGisuPort.save(createActiveGisu(9L));
        School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

        Member member = memberRepository.save(createMember("홍길동", school.getId()));
        Challenger challenger = challengerRepository.save(
            new Challenger(member.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));

        Curriculum curriculum = curriculumJpaRepository.save(createCurriculum(gisu.getId(), ChallengerPart.SPRINGBOOT));
        OriginalWorkbook workbook = originalWorkbookJpaRepository.save(createWorkbook(curriculum, 1, "1주차 워크북"));

        ChallengerWorkbook challengerWorkbook = challengerWorkbookJpaRepository.save(
            createChallengerWorkbook(challenger.getId(), workbook.getId(), WorkbookStatus.PENDING));
        challengerWorkbook.submit("https://github.com/user/repo");
        challengerWorkbookJpaRepository.save(challengerWorkbook);

        // when
        WorkbookSubmissionDetailInfo result = getWorkbookSubmissionsUseCase.getSubmissionDetail(
            challengerWorkbook.getId());

        // then
        assertThat(result.challengerWorkbookId()).isEqualTo(challengerWorkbook.getId());
        assertThat(result.submission()).isEqualTo("https://github.com/user/repo");
    }

    @Test
    void 미제출_워크북의_제출_URL은_null이다() {
        // given
        Gisu gisu = manageGisuPort.save(createActiveGisu(9L));
        School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

        Member member = memberRepository.save(createMember("홍길동", school.getId()));
        Challenger challenger = challengerRepository.save(
            new Challenger(member.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));

        Curriculum curriculum = curriculumJpaRepository.save(createCurriculum(gisu.getId(), ChallengerPart.SPRINGBOOT));
        OriginalWorkbook workbook = originalWorkbookJpaRepository.save(createWorkbook(curriculum, 1, "1주차 워크북"));

        ChallengerWorkbook challengerWorkbook = challengerWorkbookJpaRepository.save(
            createChallengerWorkbook(challenger.getId(), workbook.getId(), WorkbookStatus.PENDING));

        // when
        WorkbookSubmissionDetailInfo result = getWorkbookSubmissionsUseCase.getSubmissionDetail(
            challengerWorkbook.getId());

        // then
        assertThat(result.challengerWorkbookId()).isEqualTo(challengerWorkbook.getId());
        assertThat(result.submission()).isNull();
    }

    private Gisu createActiveGisu(Long generation) {
        return Gisu.create(
            generation,
            Instant.parse("2024-03-01T00:00:00Z"),
            Instant.parse("2024-08-31T23:59:59Z"),
            true
        );
    }

    private Member createMember(String nickname, Long schoolId) {
        return Member.builder()
            .name(nickname)
            .nickname(nickname)
            .email(nickname + "@test.com")
            .schoolId(schoolId)
            .build();
    }

    private OriginalWorkbook createWorkbook(Curriculum curriculum, int weekNo, String title) {
        return OriginalWorkbook.create(
            curriculum,
            weekNo,
            title,
            null,
            null,
            Instant.parse("2024-03-01T00:00:00Z"),
            Instant.parse("2024-03-07T23:59:59Z"),
            MissionType.LINK
        );
    }

    private Curriculum createCurriculum(Long gisuId, ChallengerPart part) {
        return Curriculum.create(gisuId, part, "9기 " + part.name());
    }

    private ChallengerWorkbook createChallengerWorkbook(Long challengerId, Long workbookId, WorkbookStatus status) {
        return ChallengerWorkbook.builder()
            .challengerId(challengerId)
            .originalWorkbookId(workbookId)
            .scheduleId(1L)
            .status(status)
            .build();
    }

    @Nested
    class 권한별_컨텍스트_조회 {

        @Test
        void 회장은_모든_파트를_조회할_수_있다() {
            // given
            Gisu gisu = manageGisuPort.save(createActiveGisu(9L));
            School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

            Member member = memberRepository.save(createMember("회장", school.getId()));
            Challenger challenger = challengerRepository.save(
                new Challenger(member.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));

            challengerRoleJpaRepository.save(ChallengerRole.create(
                challenger.getId(), ChallengerRoleType.SCHOOL_PRESIDENT,
                school.getId(), null, gisu.getId()));

            // when
            SchoolAccessContext context = getSchoolAccessContextUseCase.getContext(member.getId());

            // then
            assertThat(context.schoolId()).isEqualTo(school.getId());
            assertThat(context.part()).isNull();
        }

        @Test
        void 부회장은_모든_파트를_조회할_수_있다() {
            // given
            Gisu gisu = manageGisuPort.save(createActiveGisu(9L));
            School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

            Member member = memberRepository.save(createMember("부회장", school.getId()));
            Challenger challenger = challengerRepository.save(
                new Challenger(member.getId(), ChallengerPart.WEB, gisu.getId()));

            challengerRoleJpaRepository.save(ChallengerRole.create(
                challenger.getId(), ChallengerRoleType.SCHOOL_VICE_PRESIDENT,
                school.getId(), null, gisu.getId()));

            // when
            SchoolAccessContext context = getSchoolAccessContextUseCase.getContext(member.getId());

            // then
            assertThat(context.schoolId()).isEqualTo(school.getId());
            assertThat(context.part()).isNull();
        }

        @Test
        void 파트장은_담당_파트만_조회할_수_있다() {
            // given
            Gisu gisu = manageGisuPort.save(createActiveGisu(9L));
            School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

            Member member = memberRepository.save(createMember("스프링파트장", school.getId()));
            Challenger challenger = challengerRepository.save(
                new Challenger(member.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));

            challengerRoleJpaRepository.save(ChallengerRole.create(
                challenger.getId(), ChallengerRoleType.SCHOOL_PART_LEADER,
                school.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));

            // when
            SchoolAccessContext context = getSchoolAccessContextUseCase.getContext(member.getId());

            // then
            assertThat(context.schoolId()).isEqualTo(school.getId());
            assertThat(context.part()).isEqualTo(ChallengerPart.SPRINGBOOT);
        }

        @Test
        void 기타_교내_운영진은_담당_파트만_조회할_수_있다() {
            // given
            Gisu gisu = manageGisuPort.save(createActiveGisu(9L));
            School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

            Member member = memberRepository.save(createMember("운영진", school.getId()));
            Challenger challenger = challengerRepository.save(
                new Challenger(member.getId(), ChallengerPart.WEB, gisu.getId()));

            challengerRoleJpaRepository.save(ChallengerRole.create(
                challenger.getId(), ChallengerRoleType.SCHOOL_ETC_ADMIN,
                school.getId(), ChallengerPart.WEB, gisu.getId()));

            // when
            SchoolAccessContext context = getSchoolAccessContextUseCase.getContext(member.getId());

            // then
            assertThat(context.schoolId()).isEqualTo(school.getId());
            assertThat(context.part()).isEqualTo(ChallengerPart.WEB);
        }

        @Test
        void 학교_운영진_역할이_없으면_예외가_발생한다() {
            // given
            Gisu gisu = manageGisuPort.save(createActiveGisu(9L));
            School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

            Member member = memberRepository.save(createMember("일반챌린저", school.getId()));
            challengerRepository.save(
                new Challenger(member.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));

            // when & then
            assertThatThrownBy(() -> getSchoolAccessContextUseCase.getContext(member.getId()))
                .isInstanceOf(IllegalStateException.class);
        }
    }

    private StudyGroup createStudyGroup(String name, Gisu gisu, ChallengerPart part) {
        return StudyGroup.create(name, gisu, part);
    }
}
