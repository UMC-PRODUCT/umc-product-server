package com.umc.product.organization.application.port.in.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.application.port.in.query.dto.PartSummaryInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolStudyGroupInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListQuery;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GetStudyGroupUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private GetStudyGroupUseCase getStudyGroupUseCase;

    @Autowired
    private ManageStudyGroupPort manageStudyGroupPort;

    @Autowired
    private ManageSchoolPort manageSchoolPort;

    @Autowired
    private ManageGisuPort manageGisuPort;

    @Autowired
    private TestMemberRepository memberRepository;

    @Autowired
    private TestChallengerRepository challengerRepository;

    @Test
    void 활성_기수의_스터디_그룹이_있는_학교_목록을_조회한다() {
        // given
        Gisu activeGisu = manageGisuPort.save(createActiveGisu(9L));
        School school1 = manageSchoolPort.save(School.create("서울대학교", "비고1"));
        School school2 = manageSchoolPort.save(School.create("연세대학교", "비고2"));
        manageSchoolPort.save(School.create("고려대학교", "비고3")); // 스터디 그룹 없음

        // 서울대 멤버/챌린저 생성
        Challenger seoulChallenger1 = createAndSaveChallenger("서울대생1", school1.getId(), ChallengerPart.WEB, activeGisu.getId());
        Challenger seoulChallenger2 = createAndSaveChallenger("서울대생2", school1.getId(), ChallengerPart.WEB, activeGisu.getId());

        // 연세대 멤버/챌린저 생성
        Challenger yonseiChallenger1 = createAndSaveChallenger("연세대생1", school2.getId(), ChallengerPart.SPRINGBOOT, activeGisu.getId());

        // 스터디 그룹 생성 및 멤버 추가
        StudyGroup reactA = createStudyGroup("React A팀", activeGisu, ChallengerPart.WEB);
        reactA.addMember(seoulChallenger1.getId());
        manageStudyGroupPort.save(reactA);

        StudyGroup reactB = createStudyGroup("React B팀", activeGisu, ChallengerPart.WEB);
        reactB.addMember(seoulChallenger2.getId());
        manageStudyGroupPort.save(reactB);

        StudyGroup springA = createStudyGroup("Spring A팀", activeGisu, ChallengerPart.SPRINGBOOT);
        springA.addMember(yonseiChallenger1.getId());
        manageStudyGroupPort.save(springA);

        // when
        List<SchoolStudyGroupInfo> result = getStudyGroupUseCase.getSchools();

        // then
        assertThat(result).hasSize(2);

        SchoolStudyGroupInfo seoul = result.stream()
                .filter(s -> s.schoolName().equals("서울대학교"))
                .findFirst()
                .orElseThrow();
        assertThat(seoul.totalStudyGroupCount()).isEqualTo(2);

        SchoolStudyGroupInfo yonsei = result.stream()
                .filter(s -> s.schoolName().equals("연세대학교"))
                .findFirst()
                .orElseThrow();
        assertThat(yonsei.totalStudyGroupCount()).isEqualTo(1);
    }

    @Test
    void 비활성_기수의_스터디_그룹은_학교_목록에_포함되지_않는다() {
        // given
        Gisu inactiveGisu = manageGisuPort.save(createInactiveGisu(8L));
        Gisu activeGisu = manageGisuPort.save(createActiveGisu(9L));
        School school1 = manageSchoolPort.save(School.create("서울대학교", "비고1"));
        School school2 = manageSchoolPort.save(School.create("연세대학교", "비고2"));

        // 비활성 기수 챌린저
        Challenger oldChallenger = createAndSaveChallenger("서울대생1", school1.getId(), ChallengerPart.WEB, inactiveGisu.getId());
        // 활성 기수 챌린저
        Challenger newChallenger = createAndSaveChallenger("연세대생1", school2.getId(), ChallengerPart.WEB, activeGisu.getId());

        // 비활성 기수 스터디 그룹
        StudyGroup oldGroup = createStudyGroup("Old팀", inactiveGisu, ChallengerPart.WEB);
        oldGroup.addMember(oldChallenger.getId());
        manageStudyGroupPort.save(oldGroup);

        // 활성 기수 스터디 그룹
        StudyGroup newGroup = createStudyGroup("New팀", activeGisu, ChallengerPart.WEB);
        newGroup.addMember(newChallenger.getId());
        manageStudyGroupPort.save(newGroup);

        // when
        List<SchoolStudyGroupInfo> result = getStudyGroupUseCase.getSchools();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).schoolName()).isEqualTo("연세대학교");
    }

    @Test
    void 특정_학교의_파트별_스터디_그룹_요약을_조회한다() {
        // given
        Gisu activeGisu = manageGisuPort.save(createActiveGisu(9L));
        School school = manageSchoolPort.save(School.create("서울대학교", "비고1"));

        Challenger webChallenger1 = createAndSaveChallenger("웹1", school.getId(), ChallengerPart.WEB, activeGisu.getId());
        Challenger webChallenger2 = createAndSaveChallenger("웹2", school.getId(), ChallengerPart.WEB, activeGisu.getId());
        Challenger springChallenger = createAndSaveChallenger("스프링1", school.getId(), ChallengerPart.SPRINGBOOT, activeGisu.getId());
        Challenger iosChallenger = createAndSaveChallenger("iOS1", school.getId(), ChallengerPart.IOS, activeGisu.getId());

        StudyGroup reactA = createStudyGroup("React A팀", activeGisu, ChallengerPart.WEB);
        reactA.addMember(webChallenger1.getId());
        manageStudyGroupPort.save(reactA);

        StudyGroup reactB = createStudyGroup("React B팀", activeGisu, ChallengerPart.WEB);
        reactB.addMember(webChallenger2.getId());
        manageStudyGroupPort.save(reactB);

        StudyGroup springA = createStudyGroup("Spring A팀", activeGisu, ChallengerPart.SPRINGBOOT);
        springA.addMember(springChallenger.getId());
        manageStudyGroupPort.save(springA);

        StudyGroup iosA = createStudyGroup("iOS A팀", activeGisu, ChallengerPart.IOS);
        iosA.addMember(iosChallenger.getId());
        manageStudyGroupPort.save(iosA);

        // when
        PartSummaryInfo result = getStudyGroupUseCase.getParts(school.getId());

        // then
        assertThat(result.schoolId()).isEqualTo(school.getId());
        assertThat(result.schoolName()).isEqualTo("서울대학교");
        assertThat(result.parts()).hasSize(3);

        PartSummaryInfo.PartInfo webPart = result.parts().stream()
                .filter(p -> p.part() == ChallengerPart.WEB)
                .findFirst()
                .orElseThrow();
        assertThat(webPart.studyGroupCount()).isEqualTo(2);

        PartSummaryInfo.PartInfo springPart = result.parts().stream()
                .filter(p -> p.part() == ChallengerPart.SPRINGBOOT)
                .findFirst()
                .orElseThrow();
        assertThat(springPart.studyGroupCount()).isEqualTo(1);
    }

    @Test
    void 활성_기수_스터디_그룹이_없는_학교는_빈_파트_목록을_반환한다() {
        // given
        Gisu inactiveGisu = manageGisuPort.save(createInactiveGisu(8L));
        manageGisuPort.save(createActiveGisu(9L));
        School school = manageSchoolPort.save(School.create("서울대학교", "비고1"));

        Challenger oldChallenger = createAndSaveChallenger("서울대생1", school.getId(), ChallengerPart.WEB, inactiveGisu.getId());

        // 비활성 기수의 스터디 그룹만 존재
        StudyGroup oldGroup = createStudyGroup("Old팀", inactiveGisu, ChallengerPart.WEB);
        oldGroup.addMember(oldChallenger.getId());
        manageStudyGroupPort.save(oldGroup);

        // when
        PartSummaryInfo result = getStudyGroupUseCase.getParts(school.getId());

        // then
        assertThat(result.schoolId()).isEqualTo(school.getId());
        assertThat(result.schoolName()).isEqualTo("서울대학교"); // 학교 정보는 별도 조회하므로 있음
        assertThat(result.parts()).isEmpty();
    }

    @Test
    void 스터디_그룹_목록을_커서_기반으로_조회한다() {
        // given
        Gisu activeGisu = manageGisuPort.save(createActiveGisu(9L));
        School school = manageSchoolPort.save(School.create("서울대학교", "비고1"));

        // 5개의 스터디 그룹 생성 (각각 멤버 포함)
        for (int i = 1; i <= 5; i++) {
            Challenger challenger = createAndSaveChallenger("멤버" + i, school.getId(), ChallengerPart.WEB, activeGisu.getId());
            StudyGroup group = createStudyGroup("React " + i + "팀", activeGisu, ChallengerPart.WEB);
            group.addMember(challenger.getId());
            manageStudyGroupPort.save(group);
        }

        // when - 첫 페이지 (size=2, fetchSize=3)
        StudyGroupListQuery firstQuery = new StudyGroupListQuery(school.getId(), ChallengerPart.WEB, null, 2);
        List<StudyGroupListInfo.StudyGroupInfo> firstPage = getStudyGroupUseCase.getStudyGroups(firstQuery);

        // then - fetchSize(3)만큼 조회되어 hasNext 판단 가능
        assertThat(firstPage).hasSize(3); // size+1 = 3개 조회 (다음 페이지 있음)
        Long firstNextCursor = firstPage.get(1).groupId(); // size 위치의 커서

        // when - 두 번째 페이지
        StudyGroupListQuery secondQuery = new StudyGroupListQuery(school.getId(), ChallengerPart.WEB, firstNextCursor, 2);
        List<StudyGroupListInfo.StudyGroupInfo> secondPage = getStudyGroupUseCase.getStudyGroups(secondQuery);

        // then
        assertThat(secondPage).hasSize(3); // 다음 페이지 있음
        Long secondNextCursor = secondPage.get(1).groupId();

        // when - 마지막 페이지
        StudyGroupListQuery thirdQuery = new StudyGroupListQuery(school.getId(), ChallengerPart.WEB, secondNextCursor, 2);
        List<StudyGroupListInfo.StudyGroupInfo> thirdPage = getStudyGroupUseCase.getStudyGroups(thirdQuery);

        // then - 1개만 남아서 hasNext=false
        assertThat(thirdPage).hasSize(1); // size(2)보다 작으므로 다음 페이지 없음
    }

    @Test
    void 다른_파트의_스터디_그룹은_조회되지_않는다() {
        // given
        Gisu activeGisu = manageGisuPort.save(createActiveGisu(9L));
        School school = manageSchoolPort.save(School.create("서울대학교", "비고1"));

        Challenger webChallenger = createAndSaveChallenger("웹1", school.getId(), ChallengerPart.WEB, activeGisu.getId());
        Challenger springChallenger = createAndSaveChallenger("스프링1", school.getId(), ChallengerPart.SPRINGBOOT, activeGisu.getId());

        StudyGroup reactA = createStudyGroup("React A팀", activeGisu, ChallengerPart.WEB);
        reactA.addMember(webChallenger.getId());
        manageStudyGroupPort.save(reactA);

        StudyGroup springA = createStudyGroup("Spring A팀", activeGisu, ChallengerPart.SPRINGBOOT);
        springA.addMember(springChallenger.getId());
        manageStudyGroupPort.save(springA);

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
        Gisu activeGisu = manageGisuPort.save(createActiveGisu(9L));
        School school = manageSchoolPort.save(School.create("서울대학교", "비고1"));

        Challenger challenger = createAndSaveChallenger("웹1", school.getId(), ChallengerPart.WEB, activeGisu.getId());

        StudyGroup reactA = createStudyGroup("React A팀", activeGisu, ChallengerPart.WEB);
        reactA.addMember(challenger.getId());
        manageStudyGroupPort.save(reactA);

        // when - 다른 학교 조회
        StudyGroupListQuery query = new StudyGroupListQuery(999L, ChallengerPart.WEB, null, 10);
        List<StudyGroupListInfo.StudyGroupInfo> result = getStudyGroupUseCase.getStudyGroups(query);

        // then
        assertThat(result).isEmpty();
    }

    private Gisu createActiveGisu(Long generation) {
        return Gisu.create(
                generation,
                Instant.parse("2024-03-01T00:00:00Z"),
                Instant.parse("2024-08-31T23:59:59Z"),
                true
        );
    }

    private Gisu createInactiveGisu(Long generation) {
        return Gisu.create(
                generation,
                Instant.parse("2023-03-01T00:00:00Z"),
                Instant.parse("2023-08-31T23:59:59Z"),
                false
        );
    }

    private StudyGroup createStudyGroup(String name, Gisu gisu, ChallengerPart part) {
        return StudyGroup.create(name, gisu, part);
    }

    private Member createMember(String name, Long schoolId) {
        return Member.builder()
                .name(name)
                .nickname(name)
                .email(name + "@test.com")
                .schoolId(schoolId)
                .build();
    }

    private Challenger createAndSaveChallenger(String name, Long schoolId, ChallengerPart part, Long gisuId) {
        Member member = memberRepository.save(createMember(name, schoolId));
        return challengerRepository.save(new Challenger(member.getId(), part, gisuId));
    }
}
