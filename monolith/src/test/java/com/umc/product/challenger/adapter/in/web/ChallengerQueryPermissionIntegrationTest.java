package com.umc.product.challenger.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.maintenance.application.port.in.command.RefreshMaintenanceStateUseCase;
import com.umc.product.member.adapter.out.persistence.MemberSystemRoleJpaRepository;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.MemberSystemRole;
import com.umc.product.member.domain.MemberSystemRoleType;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.ChallengerFixture;
import com.umc.product.support.fixture.ChallengerRoleFixture;
import com.umc.product.support.fixture.ChapterFixture;
import com.umc.product.support.fixture.GisuFixture;
import com.umc.product.support.fixture.SchoolFixture;

@DisplayName("챌린저 상세 조회 권한 통합 테스트")
@Execution(ExecutionMode.SAME_THREAD)
class ChallengerQueryPermissionIntegrationTest extends IntegrationTestSupport {

    @Autowired
    GisuFixture gisuFixture;
    @Autowired
    ChapterFixture chapterFixture;
    @Autowired
    SchoolFixture schoolFixture;
    @Autowired
    ChallengerFixture challengerFixture;
    @Autowired
    ChallengerRoleFixture challengerRoleFixture;
    @Autowired
    SaveMemberPort saveMemberPort;
    @Autowired
    MemberSystemRoleJpaRepository memberSystemRoleJpaRepository;
    @Autowired
    EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;
    @Autowired
    RefreshMaintenanceStateUseCase refreshMaintenanceStateUseCase;

    private Long targetChallengerId;
    private List<Long> memberIds = List.of();

    @BeforeEach
    void setUp() {
        refreshMaintenanceStateUseCase.refresh();
        Gisu targetGisu = gisuFixture.비활성_기수(11L);
        Gisu otherGisu = gisuFixture.비활성_기수(10L);
        Chapter chapter = chapterFixture.지부(targetGisu, "서울 지부");
        Chapter otherGisuChapter = chapterFixture.지부(otherGisu, "이전 기수 서울 지부");
        School targetSchool = schoolFixture.지부에_소속된_학교("대상 학교", chapter);
        School otherSchool = schoolFixture.지부에_소속된_학교("다른 학교", chapter);
        School otherGisuSchool = schoolFixture.지부에_소속된_학교("이전 기수 학교", otherGisuChapter);

        Long targetMemberId = saveMember("target", targetSchool.getId());
        Long superAdminMemberId = saveMember("super-admin", targetSchool.getId());
        Long centralMemberId = saveMember("central", targetSchool.getId());
        Long sameSchoolCoreMemberId = saveMember("same-school-core", targetSchool.getId());
        Long otherSchoolCoreMemberId = saveMember("other-school-core", otherSchool.getId());
        Long chapterPresidentMemberId = saveMember("chapter-president", targetSchool.getId());
        Long schoolPartLeaderMemberId = saveMember("school-part-leader", targetSchool.getId());
        Long schoolEtcAdminMemberId = saveMember("school-etc-admin", targetSchool.getId());
        Long otherGisuCentralMemberId = saveMember("other-gisu-central", otherGisuSchool.getId());

        targetChallengerId = challengerFixture.웹(targetMemberId, targetGisu.getId()).getId();
        Challenger central = challengerFixture.웹(centralMemberId, targetGisu.getId());
        Challenger sameSchoolCore = challengerFixture.웹(sameSchoolCoreMemberId, targetGisu.getId());
        Challenger otherSchoolCore = challengerFixture.웹(otherSchoolCoreMemberId, targetGisu.getId());
        Challenger chapterPresident = challengerFixture.웹(chapterPresidentMemberId, targetGisu.getId());
        Challenger schoolPartLeader = challengerFixture.웹(schoolPartLeaderMemberId, targetGisu.getId());
        Challenger schoolEtcAdmin = challengerFixture.웹(schoolEtcAdminMemberId, targetGisu.getId());
        Challenger otherGisuCentral = challengerFixture.웹(otherGisuCentralMemberId, otherGisu.getId());

        memberSystemRoleJpaRepository.save(
            MemberSystemRole.create(superAdminMemberId, MemberSystemRoleType.SUPER_ADMIN)
        );
        challengerRoleFixture.중앙운영사무국_일반_운영진(central.getId(), targetGisu.getId());
        challengerRoleFixture.학교_회장(sameSchoolCore.getId(), targetSchool.getId(), targetGisu.getId());
        challengerRoleFixture.학교_부회장(otherSchoolCore.getId(), otherSchool.getId(), targetGisu.getId());
        challengerRoleFixture.지부장(chapterPresident.getId(), chapter.getId(), targetGisu.getId());
        challengerRoleFixture.학교_파트장(
            schoolPartLeader.getId(), ChallengerPart.WEB, targetSchool.getId(), targetGisu.getId()
        );
        challengerRoleFixture.학교_기타_운영진(
            schoolEtcAdmin.getId(), ChallengerPart.WEB, targetSchool.getId(), targetGisu.getId()
        );
        challengerRoleFixture.중앙운영사무국_일반_운영진(otherGisuCentral.getId(), otherGisu.getId());

        memberIds = List.of(
            targetMemberId,
            superAdminMemberId,
            centralMemberId,
            sameSchoolCoreMemberId,
            otherSchoolCoreMemberId,
            chapterPresidentMemberId,
            schoolPartLeaderMemberId,
            schoolEtcAdminMemberId,
            otherGisuCentralMemberId
        );
        evictAuthoritySnapshotCacheUseCase.evictByMemberIds(memberIds);

        setUpToken("super-admin-token", superAdminMemberId);
        setUpToken("central-token", centralMemberId);
        setUpToken("same-school-core-token", sameSchoolCoreMemberId);
        setUpToken("other-school-core-token", otherSchoolCoreMemberId);
        setUpToken("chapter-president-token", chapterPresidentMemberId);
        setUpToken("school-part-leader-token", schoolPartLeaderMemberId);
        setUpToken("school-etc-admin-token", schoolEtcAdminMemberId);
        setUpToken("other-gisu-central-token", otherGisuCentralMemberId);
    }

    @AfterEach
    void clearAuthorityCache() {
        evictAuthoritySnapshotCacheUseCase.evictByMemberIds(memberIds);
    }

    @ParameterizedTest
    @ValueSource(strings = {"super-admin-token", "central-token", "same-school-core-token"})
    @DisplayName("SUPER_ADMIN과 대상 기수 중앙운영사무국 및 동일 학교 회장단은 상세를 조회한다")
    void 허용된_운영진은_챌린저_상세를_조회한다(String token) throws Exception {
        mockMvc.perform(get("/api/v1/challenger/{challengerId}", targetChallengerId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "other-school-core-token",
        "chapter-president-token",
        "school-part-leader-token",
        "school-etc-admin-token",
        "other-gisu-central-token"
    })
    @DisplayName("다른 학교 회장단과 권한 밖 운영진은 상세를 조회할 수 없다")
    void 권한이_없는_운영진은_챌린저_상세를_조회할_수_없다(String token) throws Exception {
        mockMvc.perform(get("/api/v1/challenger/{challengerId}", targetChallengerId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    private Long saveMember(String name, Long schoolId) {
        return saveMemberPort.save(Member.create(name, name, name + "@test.com", schoolId, null)).getId();
    }

    private void setUpToken(String token, Long memberId) {
        given(jwtTokenProvider.validateAccessToken(token)).willReturn(true);
        given(jwtTokenProvider.parseAccessToken(token)).willReturn(memberId);
        given(jwtTokenProvider.getRolesFromAccessToken(token)).willReturn(List.of());
    }
}
