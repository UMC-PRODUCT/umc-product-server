package com.umc.product.challenger.adapter.in.web;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.umc.product.challenger.application.port.in.command.RegisterChallengerDeactivationUseCase;
import com.umc.product.challenger.application.port.in.command.dto.RegisterChallengerDeactivationCommand;
import com.umc.product.challenger.application.port.out.SaveChallengerPointPort;
import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.ChallengerPoint;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;
import com.umc.product.challenger.domain.enums.PointType;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.maintenance.application.port.in.command.RefreshMaintenanceStateUseCase;
import com.umc.product.member.adapter.out.persistence.MemberSystemRoleJpaRepository;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.MemberSystemRole;
import com.umc.product.member.domain.MemberSystemRoleType;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.ChallengerFixture;
import com.umc.product.support.fixture.ChallengerRoleFixture;
import com.umc.product.support.fixture.ChapterFixture;
import com.umc.product.support.fixture.GisuFixture;
import com.umc.product.support.fixture.SchoolFixture;

@DisplayName("관리자 탈부 목록 조회")
class ChallengerDeactivationListIntegrationTest extends IntegrationTestSupport {

    private static final String PATH = "/api/v1/challenger/admin/deactivations";
    private static final String SUPER_ADMIN_TOKEN = "super-admin-token";
    private static final ChallengerDeactivationReason VOLUNTARY_REASON =
        ChallengerDeactivationReason.VOLUNTARY_PERSONAL_CIRCUMSTANCES;

    @Autowired private GisuFixture gisuFixture;
    @Autowired private LoadGisuPort loadGisuPort;
    @Autowired private ChapterFixture chapterFixture;
    @Autowired private SchoolFixture schoolFixture;
    @Autowired private ChallengerFixture challengerFixture;
    @Autowired private ChallengerRoleFixture challengerRoleFixture;
    @Autowired private SaveMemberPort saveMemberPort;
    @Autowired private SaveChallengerPort saveChallengerPort;
    @Autowired private SaveChallengerPointPort saveChallengerPointPort;
    @Autowired private MemberSystemRoleJpaRepository memberSystemRoleJpaRepository;
    @Autowired private RegisterChallengerDeactivationUseCase registerDeactivation;
    @Autowired private RefreshMaintenanceStateUseCase refreshMaintenanceStateUseCase;

    private Gisu activeGisu;
    private Gisu pastGisu;
    private Chapter ownChapter;
    private Chapter otherChapter;
    private School ownSchool;
    private School otherSchool;
    private School pastSchool;
    private Long superAdminMemberId;

    @BeforeEach
    void 준비() {
        refreshMaintenanceStateUseCase.refresh();
        activeGisu = loadGisuPort.findActiveGisu().orElseGet(() -> gisuFixture.활성_기수(9011L));
        pastGisu = gisuFixture.비활성_기수(9010L);
        ownChapter = chapterFixture.지부(activeGisu, "본교 지부");
        otherChapter = chapterFixture.지부(activeGisu, "다른 지부");
        Chapter pastChapter = chapterFixture.지부(pastGisu, "과거 지부");
        ownSchool = schoolFixture.지부에_소속된_학교("본교", ownChapter);
        otherSchool = schoolFixture.지부에_소속된_학교("다른 학교", otherChapter);
        pastSchool = schoolFixture.지부에_소속된_학교("과거 학교", pastChapter);
        superAdminMemberId = 회원_저장("admin", ownSchool);
        memberSystemRoleJpaRepository.save(
            MemberSystemRole.create(superAdminMemberId, MemberSystemRoleType.SUPER_ADMIN)
        );
        인증_설정(SUPER_ADMIN_TOKEN, superAdminMemberId);
    }

    @Test
    @DisplayName("활성 기수 중앙운영진과 시스템 관리자는 과거 기수의 다른 학교 탈부 기록을 조회한다")
    void 중앙운영진과_최고관리자는_과거_기수를_조회한다() throws Exception {
        // Given
        Challenger central = 챌린저_저장("central", ownSchool, activeGisu, ChallengerPart.WEB_PRODUCT_ENGINEER);
        challengerRoleFixture.중앙운영사무국_일반_운영진(central.getId(), activeGisu.getId());
        인증_설정("central-token", central.getMemberId());
        Challenger past = 챌린저_저장("past", pastSchool, pastGisu, ChallengerPart.WEB_PRODUCT_ENGINEER);
        Long deactivationId = 탈부_등록(past);

        // When / Then
        for (String token : List.of("central-token", SUPER_ADMIN_TOKEN)) {
            mockMvc.perform(목록_요청(token).param("gisuId", pastGisu.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalElements").value(1))
                .andExpect(jsonPath("$.result.content[0].deactivationId").value(deactivationId))
                .andExpect(jsonPath("$.result.content[0].gisuId").value(pastGisu.getId()));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("교내 회장단에 본교와 활성 기수 범위를 강제한다")
    void 교내_회장단은_본교_활성_기수만_조회한다(boolean vicePresident) throws Exception {
        // Given
        Challenger president = 챌린저_저장("president", ownSchool, activeGisu, ChallengerPart.WEB_PRODUCT_ENGINEER);
        if (vicePresident) {
            challengerRoleFixture.학교_부회장(president.getId(), ownSchool.getId(), activeGisu.getId());
        } else {
            challengerRoleFixture.학교_회장(president.getId(), ownSchool.getId(), activeGisu.getId());
        }
        인증_설정("school-token", president.getMemberId());
        Challenger target = 챌린저_저장("target", ownSchool, activeGisu, ChallengerPart.WEB_PRODUCT_ENGINEER);
        Challenger outside = 챌린저_저장("outside", otherSchool, activeGisu, ChallengerPart.WEB_PRODUCT_ENGINEER);
        탈부_등록(target);
        탈부_등록(outside);

        // When / Then
        mockMvc.perform(목록_요청("school-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.totalElements").value(1))
            .andExpect(jsonPath("$.result.content[0].challengerId").value(target.getId()))
            .andExpect(jsonPath("$.result.content[0].schoolId").value(ownSchool.getId()));
        접근_거부(mockMvc.perform(목록_요청("school-token").param("schoolId", otherSchool.getId().toString())));
        접근_거부(mockMvc.perform(목록_요청("school-token").param("chapterId", otherChapter.getId().toString())));
        접근_거부(mockMvc.perform(목록_요청("school-token").param("gisuId", pastGisu.getId().toString())));
    }

    @Test
    @DisplayName("지부장과 교내 파트장·기타 운영진, 과거 중앙운영진은 목록을 볼 수 없다")
    void 허용되지_않은_운영진은_목록을_조회하지_못한다() throws Exception {
        // Given
        Challenger chapterPresident = 챌린저_저장("chapter", ownSchool, activeGisu, ChallengerPart.WEB_PRODUCT_ENGINEER);
        Challenger schoolStaff = 챌린저_저장("staff", ownSchool, activeGisu, ChallengerPart.WEB_PRODUCT_ENGINEER);
        Challenger partLeader = 챌린저_저장("part", ownSchool, activeGisu, ChallengerPart.WEB_PRODUCT_ENGINEER);
        Challenger formerCentral = 챌린저_저장("former", pastSchool, pastGisu, ChallengerPart.WEB_PRODUCT_ENGINEER);
        challengerRoleFixture.지부장(chapterPresident.getId(), ownChapter.getId(), activeGisu.getId());
        challengerRoleFixture.학교_기타_운영진(schoolStaff.getId(), ChallengerPart.WEB_PRODUCT_ENGINEER,
            ownSchool.getId(), activeGisu.getId());
        challengerRoleFixture.학교_파트장(partLeader.getId(), ChallengerPart.WEB_PRODUCT_ENGINEER,
            ownSchool.getId(), activeGisu.getId());
        challengerRoleFixture.중앙운영사무국_일반_운영진(formerCentral.getId(), pastGisu.getId());
        인증_설정("chapter-token", chapterPresident.getMemberId());
        인증_설정("staff-token", schoolStaff.getMemberId());
        인증_설정("part-token", partLeader.getMemberId());
        인증_설정("former-token", formerCentral.getMemberId());

        // When / Then
        for (String token : List.of("chapter-token", "staff-token", "part-token")) {
            접근_거부(mockMvc.perform(목록_요청(token)));
        }
        접근_거부(mockMvc.perform(목록_요청("former-token").param("gisuId", pastGisu.getId().toString())));
    }

    @Test
    @DisplayName("총점 -20점은 등록 전 목록에 포함하며 점수 회복 후에는 확정 기록만 남는다")
    void 등록_전_후보는_총점에_따라_제외하고_등록_기록은_유지한다() throws Exception {
        // Given
        Challenger pending = 챌린저_저장("candidate", ownSchool, activeGisu, ChallengerPart.WEB_PRODUCT_ENGINEER);
        saveChallengerPointPort.saveAll(List.of(
            ChallengerPoint.create(pending, PointType.EVENT_NO_SHOW, null),
            ChallengerPoint.create(pending, PointType.CUSTOM, -10, null)
        ));
        Challenger registered = 챌린저_저장("registered", ownSchool, activeGisu, ChallengerPart.WEB_PRODUCT_ENGINEER);
        Long deactivationId = 탈부_등록(registered);

        // When / Then
        mockMvc.perform(목록_요청(SUPER_ADMIN_TOKEN).param("status", "PENDING"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.totalElements").value(1))
            .andExpect(jsonPath("$.result.content[0].challengerId").value(pending.getId()))
            .andExpect(jsonPath("$.result.content[0].status").value("PENDING"))
            .andExpect(jsonPath("$.result.content[0].deactivationId").doesNotExist())
            .andExpect(jsonPath("$.result.content[0].reason").doesNotExist());

        saveChallengerPointPort.save(ChallengerPoint.create(pending, PointType.BLOG_CHALLENGE, null));

        mockMvc.perform(목록_요청(SUPER_ADMIN_TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.totalElements").value(1))
            .andExpect(jsonPath("$.result.content[0].deactivationId").value(deactivationId))
            .andExpect(jsonPath("$.result.content[0].status").value("REGISTERED"))
            .andExpect(jsonPath("$.result.content[0].reason").value(VOLUNTARY_REASON.name()))
            .andExpect(jsonPath("$.result.content[0].registeredAt").exists());

        mockMvc.perform(목록_요청(SUPER_ADMIN_TOKEN).param("keyword", "REGISTERED@TEST.COM"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.totalElements").value(1))
            .andExpect(jsonPath("$.result.content[0].challengerId").value(registered.getId()))
            .andExpect(jsonPath("$.result.content[0].email").doesNotExist());
    }

    @Test
    @DisplayName("상세 기록이 없는 기존 탈부·제명자도 상태를 유지한 채 등록 완료 목록에서 조회한다")
    void 상세_없는_기존_탈부와_제명_인원도_조회한다() throws Exception {
        // Given
        List<ChallengerStatus> legacyStatuses = List.of(ChallengerStatus.EXPELLED, ChallengerStatus.WITHDRAWN);
        for (ChallengerStatus legacyStatus : legacyStatuses) {
            Challenger legacy = 챌린저_저장(
                "legacy-" + legacyStatus.name(), ownSchool, activeGisu, ChallengerPart.WEB_PRODUCT_ENGINEER
            );
            saveChallengerPointPort.save(ChallengerPoint.create(legacy, PointType.CUSTOM, -20, null));
            legacy.changeStatus(legacyStatus, superAdminMemberId, "과거 처리 사유");
            saveChallengerPort.save(legacy);
        }

        // When / Then
        ResultActions result = mockMvc.perform(목록_요청(SUPER_ADMIN_TOKEN).param("sort", "name,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.totalElements").value(2))
            .andExpect(jsonPath("$.result.content", hasSize(2)));
        for (int i = 0; i < legacyStatuses.size(); i++) {
            String row = "$.result.content[" + i + "]";
            result.andExpect(jsonPath(row + ".status").value("REGISTERED"))
                .andExpect(jsonPath(row + ".challengerStatus").value(legacyStatuses.get(i).name()))
                .andExpect(jsonPath(row + ".deactivationId").doesNotExist())
                .andExpect(jsonPath(row + ".reason").doesNotExist())
                .andExpect(jsonPath(row + ".registeredAt").doesNotExist());
        }
        mockMvc.perform(목록_요청(SUPER_ADMIN_TOKEN).param("status", "REGISTERED").param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.totalElements").value(2))
            .andExpect(jsonPath("$.result.content", hasSize(1)));
        mockMvc.perform(목록_요청(SUPER_ADMIN_TOKEN).param("status", "PENDING"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.totalElements").value(0));
        mockMvc.perform(목록_요청(SUPER_ADMIN_TOKEN).param("reason", VOLUNTARY_REASON.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.totalElements").value(0));
    }

    @Test
    @DisplayName("목록 필터와 이름 정렬을 페이지 내용과 전체 건수에 적용한다")
    void 검색과_필터링_이후_페이지를_나눈다() throws Exception {
        // Given
        Challenger second = 챌린저_저장("target-b", ownSchool, activeGisu, ChallengerPart.WEB_PRODUCT_ENGINEER);
        Challenger first = 챌린저_저장("target-a", ownSchool, activeGisu, ChallengerPart.WEB_PRODUCT_ENGINEER);
        Challenger otherPart = 챌린저_저장("target-c", ownSchool, activeGisu, ChallengerPart.MOBILE_PRODUCT_ENGINEER);
        Challenger otherName = 챌린저_저장("another", ownSchool, activeGisu, ChallengerPart.WEB_PRODUCT_ENGINEER);
        Challenger otherOrganization = 챌린저_저장("target-d", otherSchool, activeGisu, ChallengerPart.WEB_PRODUCT_ENGINEER);
        Challenger otherReason = 챌린저_저장("target-e", ownSchool, activeGisu, ChallengerPart.WEB_PRODUCT_ENGINEER);
        for (Challenger challenger : List.of(first, second, otherPart, otherName, otherOrganization)) {
            탈부_등록(challenger);
        }
        registerDeactivation.register(RegisterChallengerDeactivationCommand.of(
            otherReason.getId(), ChallengerDeactivationReason.INVOLUNTARY_EVENT_NO_SHOW,
            null, null, List.of(), List.of(), superAdminMemberId
        ));

        // When / Then
        mockMvc.perform(목록_요청(SUPER_ADMIN_TOKEN)
                .param("chapterId", ownChapter.getId().toString())
                .param("part", ChallengerPart.WEB_PRODUCT_ENGINEER.name())
                .param("keyword", "TARGET")
                .param("status", "REGISTERED")
                .param("reason", VOLUNTARY_REASON.name())
                .param("page", "1")
                .param("size", "1")
                .param("sort", "name,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.totalElements").value(2))
            .andExpect(jsonPath("$.result.content", hasSize(1)))
            .andExpect(jsonPath("$.result.content[0].name").value("target-b"));
    }

    private void 접근_거부(ResultActions result) throws Exception {
        result.andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("AUTHORIZATION-0001"));
    }

    private Challenger 챌린저_저장(String name, School school, Gisu gisu, ChallengerPart part) {
        return challengerFixture.챌린저(회원_저장(name, school), part, gisu.getId());
    }

    private Long 회원_저장(String name, School school) {
        return saveMemberPort.save(Member.create(name, name, name + "@test.com", school.getId(), null)).getId();
    }

    private Long 탈부_등록(Challenger challenger) {
        return registerDeactivation.register(RegisterChallengerDeactivationCommand.of(
            challenger.getId(), VOLUNTARY_REASON, null, null, List.of(), List.of(), superAdminMemberId
        ));
    }

    private MockHttpServletRequestBuilder 목록_요청(String token) {
        return get(PATH).header("Authorization", "Bearer " + token);
    }

    private void 인증_설정(String token, Long memberId) {
        given(jwtTokenProvider.validateAccessToken(token)).willReturn(true);
        given(jwtTokenProvider.parseAccessToken(token)).willReturn(memberId);
        given(jwtTokenProvider.getRolesFromAccessToken(token)).willReturn(List.of());
    }
}
