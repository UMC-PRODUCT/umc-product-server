package com.umc.product.curriculum.application.port.in.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.dto.GetWorkbookSubmissionsQuery;
import com.umc.product.curriculum.application.port.in.query.dto.StudyGroupFilterInfo;
import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionDetailInfo;
import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionInfo;
import com.umc.product.curriculum.domain.ChallengerWorkbook;
import com.umc.product.curriculum.domain.Curriculum;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.application.port.in.query.GetSchoolAccessContextUseCase;
import com.umc.product.organization.application.port.in.query.dto.SchoolAccessContext;
import com.umc.product.organization.application.port.out.command.ManageSchoolPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.organization.domain.StudyGroup;
import com.umc.product.support.UseCaseTestSupport;
import com.umc.product.support.fixture.ChallengerFixture;
import com.umc.product.support.fixture.ChallengerRoleFixture;
import com.umc.product.support.fixture.CurriculumFixture;
import com.umc.product.support.fixture.GisuFixture;
import com.umc.product.support.fixture.MemberFixture;
import com.umc.product.support.fixture.StudyGroupFixture;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GetChallengerWorkbookUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private GetChallengerWorkbookUseCase getChallengerWorkbookUseCase;

    @Autowired
    private GetSchoolAccessContextUseCase getSchoolAccessContextUseCase;

    @Autowired
    private GisuFixture gisuFixture;

    @Autowired
    private MemberFixture memberFixture;

    @Autowired
    private ChallengerFixture challengerFixture;

    @Autowired
    private CurriculumFixture curriculumFixture;

    @Autowired
    private ChallengerRoleFixture challengerRoleFixture;

    @Autowired
    private StudyGroupFixture studyGroupFixture;

    @Autowired
    private ManageSchoolPort manageSchoolPort;

    @Test
    void 주차별_워크북_제출_현황을_조회한다() {
        // given
        Gisu gisu = gisuFixture.활성_기수(9L);
        School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

        Member member = memberFixture.학교_소속_멤버("홍길동", school.getId());
        Challenger challenger = challengerFixture.챌린저(member.getId(), ChallengerPart.SPRINGBOOT, gisu.getId());

        Curriculum curriculum = curriculumFixture.커리큘럼(gisu.getId(), ChallengerPart.SPRINGBOOT);
        OriginalWorkbook workbook = curriculumFixture.워크북(curriculum, 1, "1주차 워크북");

        curriculumFixture.챌린저워크북(challenger.getId(), workbook.getId(), WorkbookStatus.SUBMITTED);

        // when
        GetWorkbookSubmissionsQuery query = new GetWorkbookSubmissionsQuery(null, 1, null, null, null, 20);
        List<WorkbookSubmissionInfo> result = getChallengerWorkbookUseCase.getSubmissions(query);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).challengerName()).isEqualTo("홍길동");
        assertThat(result.get(0).schoolName()).isEqualTo("서울대학교");
        assertThat(result.get(0).status()).isEqualTo(WorkbookStatus.SUBMITTED);
    }

    @Test
    void 학교_필터링으로_조회한다() {
        // given
        Gisu gisu = gisuFixture.활성_기수(9L);
        School school1 = manageSchoolPort.save(School.create("서울대학교", "비고"));
        School school2 = manageSchoolPort.save(School.create("연세대학교", "비고"));

        Member member1 = memberFixture.학교_소속_멤버("서울대생", school1.getId());
        Member member2 = memberFixture.학교_소속_멤버("연세대생", school2.getId());

        Challenger challenger1 = challengerFixture.챌린저(member1.getId(), ChallengerPart.SPRINGBOOT, gisu.getId());
        Challenger challenger2 = challengerFixture.챌린저(member2.getId(), ChallengerPart.SPRINGBOOT, gisu.getId());

        Curriculum curriculum = curriculumFixture.커리큘럼(gisu.getId(), ChallengerPart.SPRINGBOOT);
        OriginalWorkbook workbook = curriculumFixture.워크북(curriculum, 1, "1주차 워크북");

        curriculumFixture.챌린저워크북(challenger1.getId(), workbook.getId(), WorkbookStatus.SUBMITTED);
        curriculumFixture.챌린저워크북(challenger2.getId(), workbook.getId(), WorkbookStatus.SUBMITTED);

        // when
        GetWorkbookSubmissionsQuery query = new GetWorkbookSubmissionsQuery(school1.getId(), 1, null, null, null, 20);
        List<WorkbookSubmissionInfo> result = getChallengerWorkbookUseCase.getSubmissions(query);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).schoolName()).isEqualTo("서울대학교");
    }

    @Test
    void 스터디_그룹_필터링으로_조회한다() {
        // given
        Gisu gisu = gisuFixture.활성_기수(9L);
        School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

        Member member1 = memberFixture.학교_소속_멤버("그룹원1", school.getId());
        Member member2 = memberFixture.학교_소속_멤버("그룹원2", school.getId());
        Member member3 = memberFixture.학교_소속_멤버("비그룹원", school.getId());

        Challenger challenger1 = challengerFixture.챌린저(member1.getId(), ChallengerPart.SPRINGBOOT, gisu.getId());
        Challenger challenger2 = challengerFixture.챌린저(member2.getId(), ChallengerPart.SPRINGBOOT, gisu.getId());
        Challenger challenger3 = challengerFixture.챌린저(member3.getId(), ChallengerPart.SPRINGBOOT, gisu.getId());

        StudyGroup studyGroup = studyGroupFixture.스터디그룹("스프링 1조", gisu, ChallengerPart.SPRINGBOOT, challenger1.getId(), challenger2.getId());

        Curriculum curriculum = curriculumFixture.커리큘럼(gisu.getId(), ChallengerPart.SPRINGBOOT);
        OriginalWorkbook workbook = curriculumFixture.워크북(curriculum, 1, "1주차 워크북");

        curriculumFixture.챌린저워크북(challenger1.getId(), workbook.getId(), WorkbookStatus.SUBMITTED);
        curriculumFixture.챌린저워크북(challenger2.getId(), workbook.getId(), WorkbookStatus.SUBMITTED);
        curriculumFixture.챌린저워크북(challenger3.getId(), workbook.getId(), WorkbookStatus.SUBMITTED);

        // when
        GetWorkbookSubmissionsQuery query = new GetWorkbookSubmissionsQuery(null, 1, studyGroup.getId(), null, null,
            20);
        List<WorkbookSubmissionInfo> result = getChallengerWorkbookUseCase.getSubmissions(query);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void 커서_기반_페이지네이션으로_조회한다() {
        // given
        Gisu gisu = gisuFixture.활성_기수(9L);
        School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

        Curriculum curriculum = curriculumFixture.커리큘럼(gisu.getId(), ChallengerPart.SPRINGBOOT);
        OriginalWorkbook workbook = curriculumFixture.워크북(curriculum, 1, "1주차 워크북");

        // 5명의 챌린저 생성
        for (int i = 1; i <= 5; i++) {
            Member member = memberFixture.학교_소속_멤버("챌린저" + i, school.getId());
            Challenger challenger = challengerFixture.챌린저(member.getId(), ChallengerPart.SPRINGBOOT, gisu.getId());
            curriculumFixture.챌린저워크북(challenger.getId(), workbook.getId(), WorkbookStatus.SUBMITTED);
        }

        // when - 첫 페이지 (size=2)
        GetWorkbookSubmissionsQuery firstQuery = new GetWorkbookSubmissionsQuery(null, 1, null, null, null, 2);
        List<WorkbookSubmissionInfo> firstPage = getChallengerWorkbookUseCase.getSubmissions(firstQuery);

        // then - fetchSize(3)만큼 조회
        assertThat(firstPage).hasSize(3);

        // when - 두 번째 페이지
        Long cursor = firstPage.get(1).challengerWorkbookId();
        GetWorkbookSubmissionsQuery secondQuery = new GetWorkbookSubmissionsQuery(null, 1, null, null, cursor, 2);
        List<WorkbookSubmissionInfo> secondPage = getChallengerWorkbookUseCase.getSubmissions(secondQuery);

        // then
        assertThat(secondPage).hasSize(3);
    }

    @Test
    void 필터용_스터디_그룹_목록을_조회한다() {
        // given
        Gisu gisu = gisuFixture.활성_기수(9L);
        School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

        Member member1 = memberFixture.학교_소속_멤버("멤버1", school.getId());
        Member member2 = memberFixture.학교_소속_멤버("멤버2", school.getId());
        Member member3 = memberFixture.학교_소속_멤버("멤버3", school.getId());

        Challenger challenger1 = challengerFixture.챌린저(member1.getId(), ChallengerPart.SPRINGBOOT, gisu.getId());
        Challenger challenger2 = challengerFixture.챌린저(member2.getId(), ChallengerPart.SPRINGBOOT, gisu.getId());
        Challenger challenger3 = challengerFixture.챌린저(member3.getId(), ChallengerPart.WEB, gisu.getId());

        studyGroupFixture.스터디그룹("스프링 1조", gisu, ChallengerPart.SPRINGBOOT, challenger1.getId());
        studyGroupFixture.스터디그룹("스프링 2조", gisu, ChallengerPart.SPRINGBOOT, challenger2.getId());
        studyGroupFixture.스터디그룹("웹 1조", gisu, ChallengerPart.WEB, challenger3.getId());

        // when
        List<StudyGroupFilterInfo> result = getChallengerWorkbookUseCase.getStudyGroupsForFilter(
            school.getId(), ChallengerPart.SPRINGBOOT);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(StudyGroupFilterInfo::name)
            .containsExactlyInAnyOrder("스프링 1조", "스프링 2조");
    }

    @Test
    void 스터디_그룹이_없으면_빈_목록을_반환한다() {
        // when
        List<StudyGroupFilterInfo> result = getChallengerWorkbookUseCase.getStudyGroupsForFilter(
            999L, ChallengerPart.SPRINGBOOT);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 챌린저_워크북의_제출_URL을_조회한다() {
        // given
        Gisu gisu = gisuFixture.활성_기수(9L);
        School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

        Member member = memberFixture.학교_소속_멤버("홍길동", school.getId());
        Challenger challenger = challengerFixture.챌린저(member.getId(), ChallengerPart.SPRINGBOOT, gisu.getId());

        Curriculum curriculum = curriculumFixture.커리큘럼(gisu.getId(), ChallengerPart.SPRINGBOOT);
        OriginalWorkbook workbook = curriculumFixture.워크북(curriculum, 1, "1주차 워크북");

        ChallengerWorkbook challengerWorkbook = curriculumFixture.제출된_챌린저워크북(
            challenger.getId(), workbook.getId(), "https://github.com/user/repo");

        // when
        WorkbookSubmissionDetailInfo result = getChallengerWorkbookUseCase.getSubmissionDetail(
            challengerWorkbook.getId());

        // then
        assertThat(result.challengerWorkbookId()).isEqualTo(challengerWorkbook.getId());
        assertThat(result.submission()).isEqualTo("https://github.com/user/repo");
    }

    @Test
    void 미제출_워크북의_제출_URL은_null이다() {
        // given
        Gisu gisu = gisuFixture.활성_기수(9L);
        School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

        Member member = memberFixture.학교_소속_멤버("홍길동", school.getId());
        Challenger challenger = challengerFixture.챌린저(member.getId(), ChallengerPart.SPRINGBOOT, gisu.getId());

        Curriculum curriculum = curriculumFixture.커리큘럼(gisu.getId(), ChallengerPart.SPRINGBOOT);
        OriginalWorkbook workbook = curriculumFixture.워크북(curriculum, 1, "1주차 워크북");

        ChallengerWorkbook challengerWorkbook = curriculumFixture.챌린저워크북(
            challenger.getId(), workbook.getId(), WorkbookStatus.PENDING);

        // when
        WorkbookSubmissionDetailInfo result = getChallengerWorkbookUseCase.getSubmissionDetail(
            challengerWorkbook.getId());

        // then
        assertThat(result.challengerWorkbookId()).isEqualTo(challengerWorkbook.getId());
        assertThat(result.submission()).isNull();
    }

    @Nested
    class 권한별_컨텍스트_조회 {

        @Test
        void 회장은_모든_파트를_조회할_수_있다() {
            // given
            Gisu gisu = gisuFixture.활성_기수(9L);
            School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

            Member member = memberFixture.학교_소속_멤버("회장", school.getId());
            Challenger challenger = challengerFixture.챌린저(member.getId(), ChallengerPart.SPRINGBOOT, gisu.getId());

            challengerRoleFixture.학교_회장(challenger.getId(), school.getId(), gisu.getId());

            // when
            SchoolAccessContext context = getSchoolAccessContextUseCase.getContext(member.getId());

            // then
            assertThat(context.schoolId()).isEqualTo(school.getId());
            assertThat(context.part()).isNull();
        }

        @Test
        void 부회장은_모든_파트를_조회할_수_있다() {
            // given
            Gisu gisu = gisuFixture.활성_기수(9L);
            School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

            Member member = memberFixture.학교_소속_멤버("부회장", school.getId());
            Challenger challenger = challengerFixture.챌린저(member.getId(), ChallengerPart.WEB, gisu.getId());

            challengerRoleFixture.학교_부회장(challenger.getId(), school.getId(), gisu.getId());

            // when
            SchoolAccessContext context = getSchoolAccessContextUseCase.getContext(member.getId());

            // then
            assertThat(context.schoolId()).isEqualTo(school.getId());
            assertThat(context.part()).isNull();
        }

        @Test
        void 파트장은_담당_파트만_조회할_수_있다() {
            // given
            Gisu gisu = gisuFixture.활성_기수(9L);
            School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

            Member member = memberFixture.학교_소속_멤버("스프링파트장", school.getId());
            Challenger challenger = challengerFixture.챌린저(member.getId(), ChallengerPart.SPRINGBOOT, gisu.getId());

            challengerRoleFixture.학교_파트장(challenger.getId(), ChallengerPart.SPRINGBOOT, school.getId(), gisu.getId());

            // when
            SchoolAccessContext context = getSchoolAccessContextUseCase.getContext(member.getId());

            // then
            assertThat(context.schoolId()).isEqualTo(school.getId());
            assertThat(context.part()).isEqualTo(ChallengerPart.SPRINGBOOT);
        }

        @Test
        void 기타_교내_운영진은_담당_파트만_조회할_수_있다() {
            // given
            Gisu gisu = gisuFixture.활성_기수(9L);
            School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

            Member member = memberFixture.학교_소속_멤버("운영진", school.getId());
            Challenger challenger = challengerFixture.챌린저(member.getId(), ChallengerPart.WEB, gisu.getId());

            challengerRoleFixture.학교_운영진(challenger.getId(), ChallengerPart.WEB, school.getId(), gisu.getId());

            // when
            SchoolAccessContext context = getSchoolAccessContextUseCase.getContext(member.getId());

            // then
            assertThat(context.schoolId()).isEqualTo(school.getId());
            assertThat(context.part()).isEqualTo(ChallengerPart.WEB);
        }

        @Test
        void 학교_운영진_역할이_없으면_예외가_발생한다() {
            // given
            Gisu gisu = gisuFixture.활성_기수(9L);
            School school = manageSchoolPort.save(School.create("서울대학교", "비고"));

            Member member = memberFixture.학교_소속_멤버("일반챌린저", school.getId());
            challengerFixture.챌린저(member.getId(), ChallengerPart.SPRINGBOOT, gisu.getId());

            // when & then
            assertThatThrownBy(() -> getSchoolAccessContextUseCase.getContext(member.getId()))
                .isInstanceOf(BusinessException.class);
        }
    }
}
