package com.umc.product.organization.application.port.in.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.common.domain.enums.ChallengerPart;
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
import com.umc.product.support.UseCaseTestSupport;
import java.time.LocalDateTime;
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

    @Test
    void 활성_기수의_스터디_그룹이_있는_학교_목록을_조회한다() {
        // given
        Gisu activeGisu = manageGisuPort.save(createActiveGisu(9L));
        School school1 = manageSchoolPort.save(School.create("서울대학교", "비고1"));
        School school2 = manageSchoolPort.save(School.create("연세대학교", "비고2"));
        manageSchoolPort.save(School.create("고려대학교", "비고3")); // 스터디 그룹 없음

        manageStudyGroupPort.save(createStudyGroup("React A팀", activeGisu, school1.getId(), ChallengerPart.WEB));
        manageStudyGroupPort.save(createStudyGroup("React B팀", activeGisu, school1.getId(), ChallengerPart.WEB));
        manageStudyGroupPort.save(createStudyGroup("Spring A팀", activeGisu, school2.getId(), ChallengerPart.SPRINGBOOT));

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

        // 비활성 기수 스터디 그룹
        manageStudyGroupPort.save(createStudyGroup("Old팀", inactiveGisu, school1.getId(), ChallengerPart.WEB));
        // 활성 기수 스터디 그룹
        manageStudyGroupPort.save(createStudyGroup("New팀", activeGisu, school2.getId(), ChallengerPart.WEB));

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

        manageStudyGroupPort.save(createStudyGroup("React A팀", activeGisu, school.getId(), ChallengerPart.WEB));
        manageStudyGroupPort.save(createStudyGroup("React B팀", activeGisu, school.getId(), ChallengerPart.WEB));
        manageStudyGroupPort.save(createStudyGroup("Spring A팀", activeGisu, school.getId(), ChallengerPart.SPRINGBOOT));
        manageStudyGroupPort.save(createStudyGroup("iOS A팀", activeGisu, school.getId(), ChallengerPart.IOS));

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

        // 비활성 기수의 스터디 그룹만 존재
        manageStudyGroupPort.save(createStudyGroup("Old팀", inactiveGisu, school.getId(), ChallengerPart.WEB));

        // when
        PartSummaryInfo result = getStudyGroupUseCase.getParts(school.getId());

        // then
        assertThat(result.schoolId()).isEqualTo(school.getId());
        assertThat(result.schoolName()).isNull(); // 활성 기수 스터디 그룹이 없으므로 학교 정보 없음
        assertThat(result.parts()).isEmpty();
    }

    @Test
    void 스터디_그룹_목록을_커서_기반으로_조회한다() {
        // given
        Gisu activeGisu = manageGisuPort.save(createActiveGisu(9L));
        School school = manageSchoolPort.save(School.create("서울대학교", "비고1"));

        // 5개의 스터디 그룹 생성
        for (int i = 1; i <= 5; i++) {
            manageStudyGroupPort.save(createStudyGroup("React " + i + "팀", activeGisu, school.getId(), ChallengerPart.WEB));
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

        manageStudyGroupPort.save(createStudyGroup("React A팀", activeGisu, school.getId(), ChallengerPart.WEB));
        manageStudyGroupPort.save(createStudyGroup("Spring A팀", activeGisu, school.getId(), ChallengerPart.SPRINGBOOT));

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

        manageStudyGroupPort.save(createStudyGroup("React A팀", activeGisu, school.getId(), ChallengerPart.WEB));

        // when - 다른 학교 조회
        StudyGroupListQuery query = new StudyGroupListQuery(999L, ChallengerPart.WEB, null, 10);
        List<StudyGroupListInfo.StudyGroupInfo> result = getStudyGroupUseCase.getStudyGroups(query);

        // then
        assertThat(result).isEmpty();
    }

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

    private StudyGroup createStudyGroup(String name, Gisu gisu, Long schoolId, ChallengerPart part) {
        return StudyGroup.builder()
                .name(name)
                .gisu(gisu)
                .schoolId(schoolId)
                .part(part)
                .build();
    }
}
