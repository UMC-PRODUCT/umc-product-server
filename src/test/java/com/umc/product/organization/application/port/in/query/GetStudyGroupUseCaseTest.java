package com.umc.product.organization.application.port.in.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListQuery;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupNameInfo;
import com.umc.product.organization.application.port.out.command.ManageSchoolPort;
import com.umc.product.organization.application.port.out.query.LoadStudyGroupPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.support.UseCaseTestSupport;
import com.umc.product.support.fixture.ChallengerFixture;
import com.umc.product.support.fixture.GisuFixture;
import com.umc.product.support.fixture.MemberFixture;
import com.umc.product.support.fixture.StudyGroupFixture;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GetStudyGroupUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private GetStudyGroupUseCase getStudyGroupUseCase;

    @Autowired
    private ManageSchoolPort manageSchoolPort;

    @Autowired
    private GisuFixture gisuFixture;

    @Autowired
    private MemberFixture memberFixture;

    @Autowired
    private ChallengerFixture challengerFixture;

    @Autowired
    private StudyGroupFixture studyGroupFixture;

    @Autowired
    private LoadStudyGroupPort loadStudyGroupPort;

    @Test
    void 스터디_그룹_목록을_커서_기반으로_조회한다() {
        // given
        Gisu activeGisu = gisuFixture.활성_기수(9L);
        School school = manageSchoolPort.save(School.create("서울대학교", "비고1"));

        for (int i = 1; i <= 5; i++) {
            Challenger challenger = challengerFixture.챌린저(
                memberFixture.학교_소속_멤버("멤버" + i, school.getId()).getId(), ChallengerPart.WEB, activeGisu.getId());
            studyGroupFixture.스터디그룹("React " + i + "팀", activeGisu, ChallengerPart.WEB, challenger.getId());
        }

        // when - 첫 페이지 (size=2, fetchSize=3)
        StudyGroupListQuery firstQuery = new StudyGroupListQuery(school.getId(), ChallengerPart.WEB, null, 2);
        List<StudyGroupListInfo.StudyGroupInfo> firstPage = getStudyGroupUseCase.getStudyGroups(firstQuery);

        // then - fetchSize(3)만큼 조회되어 hasNext 판단 가능
        assertThat(firstPage).hasSize(3);
        Long firstNextCursor = firstPage.get(1).groupId();

        // when - 두 번째 페이지
        StudyGroupListQuery secondQuery = new StudyGroupListQuery(school.getId(), ChallengerPart.WEB, firstNextCursor, 2);
        List<StudyGroupListInfo.StudyGroupInfo> secondPage = getStudyGroupUseCase.getStudyGroups(secondQuery);

        // then
        assertThat(secondPage).hasSize(3);
        Long secondNextCursor = secondPage.get(1).groupId();

        // when - 마지막 페이지
        StudyGroupListQuery thirdQuery = new StudyGroupListQuery(school.getId(), ChallengerPart.WEB, secondNextCursor, 2);
        List<StudyGroupListInfo.StudyGroupInfo> thirdPage = getStudyGroupUseCase.getStudyGroups(thirdQuery);

        // then - 1개만 남아서 hasNext=false
        assertThat(thirdPage).hasSize(1);
    }

    @Test
    void 다른_파트의_스터디_그룹은_조회되지_않는다() {
        // given
        Gisu activeGisu = gisuFixture.활성_기수(9L);
        School school = manageSchoolPort.save(School.create("서울대학교", "비고1"));

        Challenger webChallenger = challengerFixture.챌린저(
            memberFixture.학교_소속_멤버("웹1", school.getId()).getId(), ChallengerPart.WEB, activeGisu.getId());
        Challenger springChallenger = challengerFixture.챌린저(
            memberFixture.학교_소속_멤버("스프링1", school.getId()).getId(), ChallengerPart.SPRINGBOOT, activeGisu.getId());

        studyGroupFixture.스터디그룹("React A팀", activeGisu, ChallengerPart.WEB, webChallenger.getId());
        studyGroupFixture.스터디그룹("Spring A팀", activeGisu, ChallengerPart.SPRINGBOOT, springChallenger.getId());

        // when
        StudyGroupListQuery query = new StudyGroupListQuery(school.getId(), ChallengerPart.WEB, null, 10);
        List<StudyGroupListInfo.StudyGroupInfo> result = getStudyGroupUseCase.getStudyGroups(query);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("React A팀");
    }

    @Test
    void 조건에_맞는_스터디_그룹이_없으면_빈_목록을_반환한다() {
        // given
        Gisu activeGisu = gisuFixture.활성_기수(9L);
        School school = manageSchoolPort.save(School.create("서울대학교", "비고1"));

        Challenger challenger = challengerFixture.챌린저(
            memberFixture.학교_소속_멤버("웹1", school.getId()).getId(), ChallengerPart.WEB, activeGisu.getId());

        studyGroupFixture.스터디그룹("React A팀", activeGisu, ChallengerPart.WEB, challenger.getId());

        // when - 다른 학교 조회
        StudyGroupListQuery query = new StudyGroupListQuery(999L, ChallengerPart.WEB, null, 10);
        List<StudyGroupListInfo.StudyGroupInfo> result = getStudyGroupUseCase.getStudyGroups(query);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 스터디_그룹_이름_목록을_파트_필터로_조회한다() {
        // given
        Gisu activeGisu = gisuFixture.활성_기수(9L);
        School school = manageSchoolPort.save(School.create("서울대학교", "비고1"));

        Challenger webChallenger1 = challengerFixture.챌린저(
            memberFixture.학교_소속_멤버("웹1", school.getId()).getId(), ChallengerPart.WEB, activeGisu.getId());
        Challenger webChallenger2 = challengerFixture.챌린저(
            memberFixture.학교_소속_멤버("웹2", school.getId()).getId(), ChallengerPart.WEB, activeGisu.getId());
        Challenger springChallenger = challengerFixture.챌린저(
            memberFixture.학교_소속_멤버("스프링1", school.getId()).getId(), ChallengerPart.SPRINGBOOT, activeGisu.getId());

        studyGroupFixture.스터디그룹("React A팀", activeGisu, ChallengerPart.WEB, webChallenger1.getId());
        studyGroupFixture.스터디그룹("React B팀", activeGisu, ChallengerPart.WEB, webChallenger2.getId());
        studyGroupFixture.스터디그룹("Spring A팀", activeGisu, ChallengerPart.SPRINGBOOT, springChallenger.getId());

        // when
        List<StudyGroupNameInfo> result = loadStudyGroupPort.findStudyGroupNames(school.getId(), ChallengerPart.WEB);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(StudyGroupNameInfo::name)
            .containsExactly("React A팀", "React B팀");
    }

    @Test
    void 파트가_null이면_모든_파트의_스터디_그룹_이름을_조회한다() {
        // given
        Gisu activeGisu = gisuFixture.활성_기수(9L);
        School school = manageSchoolPort.save(School.create("서울대학교", "비고1"));

        Challenger webChallenger = challengerFixture.챌린저(
            memberFixture.학교_소속_멤버("웹1", school.getId()).getId(), ChallengerPart.WEB, activeGisu.getId());
        Challenger springChallenger = challengerFixture.챌린저(
            memberFixture.학교_소속_멤버("스프링1", school.getId()).getId(), ChallengerPart.SPRINGBOOT, activeGisu.getId());

        studyGroupFixture.스터디그룹("React A팀", activeGisu, ChallengerPart.WEB, webChallenger.getId());
        studyGroupFixture.스터디그룹("Spring A팀", activeGisu, ChallengerPart.SPRINGBOOT, springChallenger.getId());

        // when
        List<StudyGroupNameInfo> result = loadStudyGroupPort.findStudyGroupNames(school.getId(), null);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(StudyGroupNameInfo::name)
            .containsExactly("React A팀", "Spring A팀");
    }

    @Test
    void 비활성_기수의_스터디_그룹_이름은_조회되지_않는다() {
        // given
        Gisu inactiveGisu = gisuFixture.비활성_기수(8L);
        Gisu activeGisu = gisuFixture.활성_기수(9L);
        School school = manageSchoolPort.save(School.create("서울대학교", "비고1"));

        Challenger oldChallenger = challengerFixture.챌린저(
            memberFixture.학교_소속_멤버("옛날1", school.getId()).getId(), ChallengerPart.WEB, inactiveGisu.getId());
        Challenger newChallenger = challengerFixture.챌린저(
            memberFixture.학교_소속_멤버("현재1", school.getId()).getId(), ChallengerPart.WEB, activeGisu.getId());

        studyGroupFixture.스터디그룹("Old팀", inactiveGisu, ChallengerPart.WEB, oldChallenger.getId());
        studyGroupFixture.스터디그룹("New팀", activeGisu, ChallengerPart.WEB, newChallenger.getId());

        // when
        List<StudyGroupNameInfo> result = loadStudyGroupPort.findStudyGroupNames(school.getId(), ChallengerPart.WEB);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("New팀");
    }

    @Test
    void 스터디_그룹이_없으면_빈_이름_목록을_반환한다() {
        // given
        gisuFixture.활성_기수(9L);
        School school = manageSchoolPort.save(School.create("서울대학교", "비고1"));

        // when
        List<StudyGroupNameInfo> result = loadStudyGroupPort.findStudyGroupNames(school.getId(), ChallengerPart.WEB);

        // then
        assertThat(result).isEmpty();
    }
}
