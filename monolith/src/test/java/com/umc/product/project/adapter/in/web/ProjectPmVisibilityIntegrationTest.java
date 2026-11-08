package com.umc.product.project.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.JsonNode;
import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.ProjectManualMatchingFixture;
import com.umc.product.support.fixture.ProjectManualMatchingFixture.Scenario;
import com.umc.product.support.fixture.ProjectQuotaManagementFixture;

@AutoConfigureMockMvc(addFilters = false)
class ProjectPmVisibilityIntegrationTest extends IntegrationTestSupport {
    @Autowired ProjectManualMatchingFixture projects;
    @Autowired ProjectQuotaManagementFixture fixture;
    @Autowired EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;

    private Scenario scenario;

    @BeforeEach
    void 준비한다() {
        SecurityContextHolder.clearContext();
        scenario = projects.준비();
        fixture.배정(scenario, scenario.coPmId(), ChallengerPart.PLAN, false);
    }

    @AfterEach
    void 인증을_정리한다() {
        evictAuthoritySnapshotCacheUseCase.evictByMemberId(scenario.candidateId());
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void 목록과_상세의_PM정보는_로그인_여부에_따라_제공한다(boolean authenticated) throws Exception {
        // given
        if (authenticated) {
            var principal = MemberPrincipal.builder().memberId(scenario.candidateId()).build();
            SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        } else {
            SecurityContextHolder.clearContext();
        }

        // when
        String listBody = mockMvc.perform(get("/api/v1/projects").param("gisuId", scenario.gisuId().toString()))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String detailBody = mockMvc.perform(get("/api/v1/projects/{id}", scenario.projectId()))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode listOwner = objectMapper.readTree(listBody).path("result").path("content").get(0).path("productOwner");
        JsonNode detail = objectMapper.readTree(detailBody).path("result");

        // then
        assertIdentity(listOwner, scenario.ownerId(), "PM", authenticated);
        assertIdentity(detail.path("productOwner"), scenario.ownerId(), "PM", authenticated);
        assertIdentity(detail.path("coProductOwners").get(0), scenario.coPmId(), "보조PM", authenticated);
    }

    @Test
    void 지부를_바꾸고_이전학교_필터를_유지하면_빈목록이며_필터를_지우면_조회된다() throws Exception {
        // given
        Scenario foreign = projects.다른_지부(scenario);

        // when
        String stale = mockMvc.perform(get("/api/v1/projects").param("gisuId", scenario.gisuId().toString())
                .param("chapterId", foreign.chapterId().toString()).param("schoolIds", scenario.schoolId().toString()))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String reset = mockMvc.perform(get("/api/v1/projects").param("gisuId", scenario.gisuId().toString())
                .param("chapterId", foreign.chapterId().toString()))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        // then
        assertThat(objectMapper.readTree(stale).path("result").path("totalElements").asLong()).isZero();
        assertThat(objectMapper.readTree(reset).path("result").path("content").get(0).path("id").asLong())
            .isEqualTo(foreign.projectId());
    }

    private void assertIdentity(JsonNode member, Long memberId, String nickname, boolean authenticated) {
        assertThat(member.path("memberId").asLong()).isEqualTo(memberId);
        assertThat(member.path("nickname").asText()).isEqualTo(nickname);
        if (authenticated) {
            assertThat(member.path("name").asText()).isEqualTo(nickname);
            assertThat(member.path("schoolName").asText()).isNotBlank();
        } else {
            assertThat(member.path("name").isNull()).isTrue();
            assertThat(member.path("schoolName").isNull()).isTrue();
        }
    }
}
