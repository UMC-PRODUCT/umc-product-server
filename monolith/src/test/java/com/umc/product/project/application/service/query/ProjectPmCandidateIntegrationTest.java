package com.umc.product.project.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.MemberFixture;
import com.umc.product.support.fixture.ProjectManagementPermissionFixture;
import com.umc.product.support.fixture.ProjectManualMatchingFixture;
import com.umc.product.support.fixture.ProjectManualMatchingFixture.Scenario;
import com.umc.product.support.fixture.SchoolFixture;

@AutoConfigureMockMvc(addFilters = false)
class ProjectPmCandidateIntegrationTest extends IntegrationTestSupport {
    private static final String PATH = "/api/v1/projects/admin/pm-candidates";

    @Autowired ProjectManualMatchingFixture projects;
    @Autowired ProjectManagementPermissionFixture permissions;
    @Autowired SchoolFixture schools;
    @Autowired MemberFixture members;
    @Autowired EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactionManager;

    private Scenario scenario;

    @BeforeEach
    void 준비한다() {
        scenario = projects.준비();
        authenticate(scenario.staffId());
    }

    @AfterEach
    void 인증을_정리한다() {
        evictAuthoritySnapshotCacheUseCase.evictByMemberIds(jdbc.queryForList("SELECT id FROM member", Long.class));
        SecurityContextHolder.clearContext();
    }

    @Test
    void 지부장은_본인학교뿐_아니라_본인지부_다른학교_PM도_조회한다() throws Exception {
        // given
        Scenario foreign = projects.다른_지부(scenario);
        Long anotherSchool = new TransactionTemplate(transactionManager).execute(ignored ->
            schools.지부에_소속된_학교("같은지부 다른학교", entityManager.find(Chapter.class, scenario.chapterId())).getId());
        Long pm = projects.회원(scenario, "다른학교 PM", ChallengerPart.PLAN);
        jdbc.update("UPDATE member SET school_id=? WHERE id=?", anotherSchool, pm);

        // when
        JsonNode result = query(scenario.gisuId(), null);

        // then
        assertThat(result.path("content").findValuesAsText("memberId")).contains(
            scenario.ownerId().toString(), scenario.coPmId().toString(), pm.toString())
            .doesNotContain(foreign.ownerId().toString(), scenario.candidateId().toString());
        assertThat(result.path("content").findValuesAsText("schoolId")).contains(anotherSchool.toString());
        for (JsonNode candidate : result.path("content")) {
            assertThat(candidate.size()).isEqualTo(6);
            assertThat(candidate.has("email")).isFalse();
            assertThat(candidate.has("roles")).isFalse();
            assertThat(candidate.has("profileImageUrl")).isFalse();
        }
    }

    @ParameterizedTest
    @EnumSource(value = ChallengerRoleType.class, names = {"CENTRAL_PRESIDENT", "CENTRAL_VICE_PRESIDENT"})
    void 총괄단은_소속기수_전체지부를_조회하고_다른기수는_거부한다(ChallengerRoleType role) throws Exception {
        // given
        Scenario foreign = projects.다른_지부(scenario);
        Scenario otherGisu = projects.다른_기수();
        Long actor = permissions.역할_회원(scenario, role, ChallengerPart.DESIGN);
        authenticate(actor);

        // when & then
        assertThat(query(scenario.gisuId(), null).path("content").findValuesAsText("memberId"))
            .contains(scenario.ownerId().toString(), foreign.ownerId().toString())
            .doesNotContain(otherGisu.ownerId().toString());
        mockMvc.perform(get(PATH).param("gisuId", otherGisu.gisuId().toString())).andExpect(status().isForbidden());
    }

    @Test
    void 최고관리자는_챌린저이력없이_선택한_기수의_PM을_조회하고_기수_혼합은_없다() throws Exception {
        // given
        Scenario other = projects.다른_기수();
        Long actor = members.일반("최고관리자").getId();
        permissions.최고관리자(actor);
        authenticate(actor);

        // when & then
        assertThat(query(other.gisuId(), null).path("content").findValuesAsText("memberId"))
            .contains(other.ownerId().toString()).doesNotContain(scenario.ownerId().toString());
    }

    @ParameterizedTest
    @EnumSource(value = ChallengerRoleType.class, names = {"CENTRAL_OPERATING_TEAM_MEMBER", "CENTRAL_EDUCATION_TEAM_MEMBER",
        "SCHOOL_PRESIDENT", "SCHOOL_VICE_PRESIDENT", "SCHOOL_PART_LEADER", "SCHOOL_ETC_ADMIN"})
    void 일반운영진은_PM을_겸해도_대리등록_후보를_조회하지_못한다(ChallengerRoleType role) throws Exception {
        // given
        authenticate(permissions.역할_회원(scenario, role, ChallengerPart.PLAN));

        // when & then
        mockMvc.perform(get(PATH).param("gisuId", scenario.gisuId().toString())).andExpect(status().isForbidden());
    }

    @Test
    void PM본인과_다른기수_지부장과_존재하지않는회원은_접근하지_못한다() throws Exception {
        // given
        Scenario other = projects.다른_기수();

        // when & then
        for (Long actor : List.of(scenario.ownerId(), other.staffId(), Long.MAX_VALUE)) {
            authenticate(actor);
            mockMvc.perform(get(PATH).param("gisuId", scenario.gisuId().toString())).andExpect(status().isForbidden());
        }
        authenticate(scenario.staffId());
        mockMvc.perform(get(PATH)).andExpect(status().isBadRequest());
    }

    @Test
    void 이름과_닉네임을_검색하고_비활성_PM과_다른파트는_제외한다() throws Exception {
        // given
        Long pm = projects.회원(scenario, "표시이름", ChallengerPart.PLAN);
        jdbc.update("UPDATE member SET nickname='SubPm' WHERE id=?", pm);
        Long withdrawn = projects.회원(scenario, "탈퇴PM", ChallengerPart.PLAN);
        jdbc.update("UPDATE member SET status='WITHDRAWN' WHERE id=?", withdrawn);
        Long expelled = projects.회원(scenario, "제명PM", ChallengerPart.PLAN);
        jdbc.update("UPDATE challenger SET status='EXPELLED' WHERE member_id=?", expelled);
        projects.회원(scenario, "표시이름 디자인", ChallengerPart.DESIGN);

        // when & then
        assertThat(query(scenario.gisuId(), "  subpm  ").path("content").findValuesAsText("memberId"))
            .containsExactly(pm.toString());
        assertThat(query(scenario.gisuId(), "표시이름").path("content").findValuesAsText("memberId"))
            .containsExactly(pm.toString());
        assertThat(query(scenario.gisuId(), null).path("content").findValuesAsText("memberId"))
            .doesNotContain(withdrawn.toString(), expelled.toString());
        assertThat(query(scenario.gisuId(), "검색결과 없음").path("totalElements").asLong()).isZero();
    }

    @Test
    void 이름이_같아도_페이지_경계에서_중복과_누락없이_조회한다() throws Exception {
        // given
        Long first = projects.회원(scenario, "동명PM1", ChallengerPart.PLAN);
        Long second = projects.회원(scenario, "동명PM2", ChallengerPart.PLAN);
        jdbc.update("UPDATE member SET name='동명PM' WHERE id IN (?, ?)", first, second);

        // when & then
        for (int page = 0; page < 2; page++) {
            String body = mockMvc.perform(get(PATH).param("gisuId", scenario.gisuId().toString())
                    .param("keyword", "동명PM").param("size", "1").param("page", String.valueOf(page)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            JsonNode result = objectMapper.readTree(body).path("result");
            assertThat(result.path("totalElements").asLong()).isEqualTo(2);
            assertThat(result.path("content").get(0).path("memberId").asLong()).isEqualTo(page == 0 ? first : second);
        }
    }

    private JsonNode query(Long gisuId, String keyword) throws Exception {
        var request = get(PATH).param("gisuId", gisuId.toString());
        if (keyword != null) {
            request.param("keyword", keyword);
        }
        String body = mockMvc.perform(request).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("result");
    }

    private void authenticate(Long memberId) {
        evictAuthoritySnapshotCacheUseCase.evictByMemberId(memberId);
        var principal = MemberPrincipal.builder().memberId(memberId).build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
