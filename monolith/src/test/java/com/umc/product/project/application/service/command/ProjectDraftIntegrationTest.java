package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.project.application.port.in.command.CreateDraftProjectUseCase;
import com.umc.product.project.application.port.in.command.dto.CreateDraftProjectCommand;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.concurrency.PostgreSqlTransactionRace;
import com.umc.product.support.fixture.ProjectDraftFixture;
import com.umc.product.support.fixture.ProjectDraftFixture.Context;

@AutoConfigureMockMvc(addFilters = false)
class ProjectDraftIntegrationTest extends IntegrationTestSupport {

    @Autowired ProjectDraftFixture fixture;
    @Autowired CreateDraftProjectUseCase createDraftProjectUseCase;
    @Autowired LoadProjectPort loadProjectPort;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired JdbcTemplate jdbcTemplate;

    @AfterEach
    void 인증_정리() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 같은_기수의_여러_초안을_생성하고_정렬_후_페이지를_나눈다() throws Exception {
        // given
        Context owner = fixture.기획자("작성자", 91L);
        authenticate(owner.memberId());
        Long first = create(owner);
        Long second = create(owner);
        Long third = create(owner);
        fixture.생성시각_통일(List.of(first, second, third));

        // when & then — 생성 시각이 같아도 ID 내림차순으로 페이지 경계를 유지한다.
        mockMvc.perform(get("/api/v1/projects/me/drafts").param("gisuId", owner.gisuId().toString())
                .param("size", "2").param("page", "0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.totalElements").value(3))
            .andExpect(jsonPath("$.result.content[0].id").value(third.toString()))
            .andExpect(jsonPath("$.result.content[1].id").value(second.toString()));
        mockMvc.perform(get("/api/v1/projects/me/drafts").param("gisuId", owner.gisuId().toString())
                .param("size", "2").param("page", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content[0].id").value(first.toString()));
        mockMvc.perform(get("/api/v1/projects/me/draft").param("gisuId", owner.gisuId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.id").value(third.toString()));
        mockMvc.perform(get("/api/v1/projects/me/managed").param("gisuId", owner.gisuId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.totalElements").value(3))
            .andExpect(jsonPath("$.result.content[0].id").value(first.toString()));
    }

    @Test
    void 다른_작성자나_다른_기수의_초안은_목록에_섞이지_않는다() throws Exception {
        // given
        Context owner = fixture.기획자("작성자", 91L);
        Context other = fixture.같은_기수의_다른_기획자(owner, "다른회원");
        Context nextGisu = fixture.기획자("다른기수", 92L);
        authenticate(owner.memberId());
        Long mine = create(owner);
        authenticate(other.memberId());
        Long theirs = create(other);
        authenticate(nextGisu.memberId());
        create(nextGisu);
        authenticate(owner.memberId());

        // when & then
        mockMvc.perform(get("/api/v1/projects/me/drafts").param("gisuId", owner.gisuId().toString())
                .param("creatorMemberId", other.memberId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.totalElements").value(1))
            .andExpect(jsonPath("$.result.content[0].id").value(mine.toString()));
        mockMvc.perform(patch("/api/v1/projects/{projectId}", theirs).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"수정 불가\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void 개별_초안의_수정_제출_삭제가_다른_초안을_변경하지_않는다() throws Exception {
        // given
        Context owner = fixture.기획자("작성자", 91L);
        authenticate(owner.memberId());
        Long first = create(owner);
        Long second = create(owner);
        Long third = create(owner);
        fixture.지원폼(first, owner.memberId());

        // when
        mockMvc.perform(patch("/api/v1/projects/{projectId}", first).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"제출할 프로젝트\"}"))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/projects/{projectId}/submit", first)).andExpect(status().isOk());
        mockMvc.perform(delete("/api/v1/projects/{projectId}", second)).andExpect(status().isOk());

        // then
        assertThat(loadProjectPort.getById(first).getStatus()).isEqualTo(ProjectStatus.PENDING_REVIEW);
        assertThat(loadProjectPort.findById(second)).isEmpty();
        assertThat(loadProjectPort.getById(third).getStatus()).isEqualTo(ProjectStatus.DRAFT);
        assertThat(loadProjectPort.getById(third).getName()).isNull();
        mockMvc.perform(get("/api/v1/projects/me/drafts").param("gisuId", owner.gisuId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.totalElements").value(1))
            .andExpect(jsonPath("$.result.content[0].id").value(third.toString()));
    }

    @Test
    void 같은_작성자와_PM의_초안을_동시에_생성할_수_있다() throws Exception {
        // given
        Context owner = fixture.기획자("작성자", 91L);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        // when
        try (var race = new PostgreSqlTransactionRace(transactionManager, jdbcTemplate)) {
            Supplier<Long> action = () -> {
                ready.countDown();
                race.await(start, "초안 동시 생성 시작");
                authenticate(owner.memberId());
                try {
                    return createDraftProjectUseCase.create(CreateDraftProjectCommand.builder()
                        .gisuId(owner.gisuId()).productOwnerMemberId(owner.memberId())
                        .requesterMemberId(owner.memberId()).build());
                } finally {
                    SecurityContextHolder.clearContext();
                }
            };
            var first = race.submit(action);
            var second = race.submit(action);
            race.await(ready, "두 생성 트랜잭션 준비");
            start.countDown();
            Long firstId = first.future().get(10, TimeUnit.SECONDS);
            Long secondId = second.future().get(10, TimeUnit.SECONDS);

            // then
            assertThat(firstId).isNotEqualTo(secondId);
            assertThat(loadProjectPort.getById(firstId).getCreatorMemberId()).isEqualTo(owner.memberId());
            assertThat(loadProjectPort.getById(secondId).getProductOwnerMemberId()).isEqualTo(owner.memberId());
        }
    }

    private Long create(Context owner) throws Exception {
        String json = mockMvc.perform(post("/api/v1/projects").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(Map.of("gisuId", owner.gisuId()))))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).path("result").path("projectId").asLong();
    }

    private void authenticate(Long memberId) {
        MemberPrincipal principal = MemberPrincipal.builder().memberId(memberId).build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
