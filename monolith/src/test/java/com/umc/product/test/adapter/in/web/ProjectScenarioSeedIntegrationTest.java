package com.umc.product.test.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.databind.JsonNode;
import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.project.application.port.in.query.GetProjectQuotaManagementUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo.PartSummary;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.ChallengerFixture;
import com.umc.product.support.fixture.ChallengerRoleFixture;
import com.umc.product.support.fixture.ChapterFixture;
import com.umc.product.support.fixture.GisuFixture;
import com.umc.product.support.fixture.SchoolFixture;

@TestPropertySource(properties = "app.seed.enabled=true")
class ProjectScenarioSeedIntegrationTest extends IntegrationTestSupport {

    @Autowired private GisuFixture gisuFixture;
    @Autowired private LoadGisuPort loadGisuPort;
    @Autowired private ChapterFixture chapterFixture;
    @Autowired private SchoolFixture schoolFixture;
    @Autowired private ChallengerFixture challengerFixture;
    @Autowired private ChallengerRoleFixture challengerRoleFixture;
    @Autowired private SaveMemberPort saveMemberPort;
    @Autowired private EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;
    @Autowired private GetProjectQuotaManagementUseCase getProjectQuotaManagementUseCase;
    @Autowired private LoadProjectPort loadProjectPort;
    @Autowired private LoadProjectPartQuotaPort loadProjectPartQuotaPort;

    private Gisu gisu;
    private Chapter chapter;
    private Long schoolId;
    private int memberSequence;

    @BeforeEach
    void setUp() {
        gisu = loadGisuPort.findActiveGisu().orElseGet(() -> gisuFixture.활성_기수(90L));
        chapter = chapterFixture.지부(gisu, "시딩 지부");
        schoolId = schoolFixture.지부에_소속된_학교("시딩 학교", chapter).getId();
    }

    @Test
    @DisplayName("총괄단이 공개 시나리오를 시딩하면 지부 TO 상한 안에서 TO를 걸고 프로젝트를 공개한다")
    void seedsInProgressProjectsWithinChapterCapacity() throws Exception {
        // given
        챌린저들(schoolId, ChallengerPart.PLAN, 4);
        챌린저들(schoolId, ChallengerPart.DESIGN, 3);
        챌린저들(schoolId, ChallengerPart.WEB_PRODUCT_ENGINEER, 5);
        챌린저들(schoolId, ChallengerPart.MOBILE_PRODUCT_ENGINEER, 4);
        Long adminId = 총괄단();

        // when
        JsonNode result = 시딩("""
            {"targetStatus":"IN_PROGRESS","projectCount":3,"adminMemberId":%d}
            """.formatted(adminId));

        // then
        assertThat(result.path("failedProjects")).isEmpty();
        assertThat(result.path("createdProjects")).hasSize(3);
        for (JsonNode created : result.path("createdProjects")) {
            Long projectId = created.path("projectId").asLong();
            assertThat(loadProjectPort.getById(projectId).getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);

            Map<ChallengerPart, Long> saved = loadProjectPartQuotaPort.listByProjectId(projectId).stream()
                .collect(Collectors.toMap(ProjectPartQuota::getPart, ProjectPartQuota::getQuota));
            assertThat(saved).isNotEmpty();
            assertThat(saved.containsKey(ChallengerPart.WEB_PRODUCT_ENGINEER)
                && saved.containsKey(ChallengerPart.MOBILE_PRODUCT_ENGINEER)).isFalse();
            assertThat(응답_TO(created)).isEqualTo(saved);
        }
        for (PartSummary summary : getProjectQuotaManagementUseCase
            .get(adminId, gisu.getId(), chapter.getId(), null, null).summary()) {
            assertThat(summary.allocatedQuota())
                .as("%s TO 합계", summary.part())
                .isLessThanOrEqualTo(summary.availableCount());
        }
    }

    @Test
    @DisplayName("지부에 남은 TO가 없으면 프로젝트를 만들지 않고 QUOTA 실패로 보고한다")
    void reportsQuotaFailureWhenChapterCapacityRunsOut() throws Exception {
        // given
        챌린저들(schoolId, ChallengerPart.PLAN, 3);
        챌린저들(schoolId, ChallengerPart.MOBILE_PRODUCT_ENGINEER, 3);
        Long presidentId = 지부장(chapter);

        // when
        JsonNode result = 시딩("""
            {"targetStatus":"IN_PROGRESS","projectCount":3,"adminMemberId":%d}
            """.formatted(presidentId));

        // then
        assertThat(result.path("createdProjects")).singleElement().satisfies(created ->
            assertThat(응답_TO(created)).isEqualTo(Map.of(ChallengerPart.MOBILE_PRODUCT_ENGINEER, 3L)));
        assertThat(result.path("failedProjects")).hasSize(2).allSatisfy(failed -> {
            assertThat(failed.path("failedStep").asText()).isEqualTo("QUOTA");
            assertThat(failed.path("projectId").isNull()).isTrue();
        });
        assertThat(프로젝트_수()).isEqualTo(1L);
    }

    @Test
    @DisplayName("PO를 지정하지 않으면 운영진이 관리하는 지부의 PLAN 챌린저만 PO로 고른다")
    void picksProductOwnersOnlyFromAdminChapters() throws Exception {
        // given
        Chapter otherChapter = chapterFixture.지부(gisu, "다른 지부");
        Long otherSchoolId = schoolFixture.지부에_소속된_학교("다른 학교", otherChapter).getId();
        List<Long> ownChapterPlans = 챌린저들(schoolId, ChallengerPart.PLAN, 2);
        챌린저들(otherSchoolId, ChallengerPart.PLAN, 3);
        챌린저들(schoolId, ChallengerPart.WEB_PRODUCT_ENGINEER, 10);
        Long presidentId = 지부장(chapter);

        // when
        JsonNode result = 시딩("""
            {"targetStatus":"IN_PROGRESS","projectCount":2,"adminMemberId":%d}
            """.formatted(presidentId));

        // then
        assertThat(result.path("failedProjects")).isEmpty();
        assertThat(result.path("createdProjects")).hasSize(2).allSatisfy(created -> {
            assertThat(created.path("chapterId").asLong()).isEqualTo(chapter.getId());
            assertThat(ownChapterPlans).contains(created.path("productOwnerMemberId").asLong());
        });
    }

    @Test
    @DisplayName("운영진이 관리하지 않는 지부의 PO를 지정하면 프로젝트를 만들지 않고 QUOTA 실패로 보고한다")
    void rejectsProductOwnerOutsideAdminChapters() throws Exception {
        // given
        Chapter otherChapter = chapterFixture.지부(gisu, "다른 지부");
        Long otherSchoolId = schoolFixture.지부에_소속된_학교("다른 학교", otherChapter).getId();
        Long outsider = 챌린저들(otherSchoolId, ChallengerPart.PLAN, 1).getFirst();
        챌린저들(otherSchoolId, ChallengerPart.DESIGN, 2);
        Long presidentId = 지부장(chapter);

        // when
        JsonNode result = 시딩("""
            {"targetStatus":"IN_PROGRESS","projectCount":1,"adminMemberId":%d,"productOwnerMemberIds":[%d]}
            """.formatted(presidentId, outsider));

        // then
        assertThat(result.path("createdProjects")).isEmpty();
        assertThat(result.path("failedProjects")).singleElement().satisfies(failed -> {
            assertThat(failed.path("failedStep").asText()).isEqualTo("QUOTA");
            assertThat(failed.path("projectId").isNull()).isTrue();
        });
        assertThat(프로젝트_수()).isZero();
    }

    @Test
    @DisplayName("adminMemberId 없이 공개 시나리오를 요청하면 400으로 거부하고 프로젝트를 만들지 않는다")
    void rejectsInProgressWithoutAdmin() throws Exception {
        // given
        챌린저들(schoolId, ChallengerPart.PLAN, 1);
        챌린저들(schoolId, ChallengerPart.DESIGN, 2);

        // when & then
        mockMvc.perform(post("/test/seed/projects/scenarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"targetStatus":"IN_PROGRESS","projectCount":1}
                    """))
            .andExpect(status().isBadRequest());
        assertThat(프로젝트_수()).isZero();
    }

    @Test
    @DisplayName("승인 대기 시나리오는 PO가 직접 제출해 승인 대기 상태로 남고 TO를 걸지 않는다")
    void seedsPendingReviewProjectsByProductOwner() throws Exception {
        // given
        Long ownerId = 챌린저들(schoolId, ChallengerPart.PLAN, 1).getFirst();

        // when
        JsonNode result = 시딩("""
            {"targetStatus":"PENDING_REVIEW","projectCount":1}
            """);

        // then
        assertThat(result.path("failedProjects")).isEmpty();
        JsonNode created = result.path("createdProjects").get(0);
        Long projectId = created.path("projectId").asLong();
        assertThat(created.path("productOwnerMemberId").asLong()).isEqualTo(ownerId);
        assertThat(created.path("partQuotas")).isEmpty();
        assertThat(loadProjectPort.getById(projectId).getStatus()).isEqualTo(ProjectStatus.PENDING_REVIEW);
        assertThat(loadProjectPartQuotaPort.listByProjectId(projectId)).isEmpty();
    }

    private JsonNode 시딩(String body) throws Exception {
        String response = mockMvc.perform(post("/test/seed/projects/scenarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("result");
    }

    private Map<ChallengerPart, Long> 응답_TO(JsonNode created) {
        List<JsonNode> quotas = new ArrayList<>();
        created.path("partQuotas").forEach(quotas::add);
        return quotas.stream().collect(Collectors.toMap(
            quota -> ChallengerPart.valueOf(quota.path("part").asText()),
            quota -> quota.path("quota").asLong()));
    }

    private List<Long> 챌린저들(Long memberSchoolId, ChallengerPart part, int count) {
        List<Long> memberIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            memberIds.add(챌린저(memberSchoolId, part));
        }
        return memberIds;
    }

    private Long 챌린저(Long memberSchoolId, ChallengerPart part) {
        int sequence = ++memberSequence;
        Member member = saveMemberPort.save(Member.create(
            "시딩" + sequence, "시딩" + sequence, "scenario-seed-" + sequence + "@test.com", memberSchoolId, null));
        challengerFixture.챌린저(member.getId(), part, gisu.getId());
        return member.getId();
    }

    private Long 총괄단() {
        Long memberId = 챌린저(schoolId, ChallengerPart.ADMIN);
        challengerRoleFixture.중앙운영사무국_총괄(챌린저_ID(memberId), gisu.getId());
        evictAuthoritySnapshotCacheUseCase.evictByMemberId(memberId);
        return memberId;
    }

    private Long 지부장(Chapter target) {
        Long memberId = 챌린저(schoolId, ChallengerPart.ADMIN);
        challengerRoleFixture.지부장(챌린저_ID(memberId), target.getId(), gisu.getId());
        evictAuthoritySnapshotCacheUseCase.evictByMemberId(memberId);
        return memberId;
    }

    private Long 챌린저_ID(Long memberId) {
        return entityManager.createQuery("select c.id from Challenger c where c.memberId = :memberId", Long.class)
            .setParameter("memberId", memberId)
            .getSingleResult();
    }

    private long 프로젝트_수() {
        return entityManager.createQuery("select count(p) from Project p", Long.class).getSingleResult();
    }
}
