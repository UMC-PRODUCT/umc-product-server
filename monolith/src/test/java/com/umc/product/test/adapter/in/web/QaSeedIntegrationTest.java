package com.umc.product.test.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.databind.JsonNode;
import com.umc.product.authorization.application.port.in.query.ListChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleInfo;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.project.application.port.in.query.GetMyMatchingAffiliationUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingAffiliationInfo;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.test.application.port.out.LoadQaRosterPort;
import com.umc.product.test.domain.roster.QaAccountPlan;
import com.umc.product.test.domain.roster.QaTransfers;
import com.umc.product.test.domain.roster.QaTransfers.QaTransfer;
import com.umc.product.test.domain.roster.RosterKey;

/**
 * 테스트가 끝나면 골격까지 지워지므로, 클래스 시작 전에 컨텍스트를 버려 새 DB를 받고 시나리오를 한 테스트에 담는다.
 */
@TestPropertySource(properties = {
    "app.seed.enabled=true",
    "app.seed.email-domain=qa.test"
})
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@Import(CheapPasswordEncoderConfig.class)
class QaSeedIntegrationTest extends IntegrationTestSupport {

    private static final List<String> COUNTED_TABLES = List.of(
        "member", "challenger", "challenger_role", "member_system_role", "study_group", "weekly_curriculum",
        "schedule", "notice", "notice_read", "challenger_point", "community_thread", "chat_message",
        "project_matching_chapter_transfer");

    @Autowired private LoadQaRosterPort loadQaRosterPort;
    @Autowired private GetMemberUseCase getMemberUseCase;
    @Autowired private GetGisuUseCase getGisuUseCase;
    @Autowired private ListChallengerRoleUseCase listChallengerRoleUseCase;
    @Autowired private GetMyMatchingAffiliationUseCase getMyMatchingAffiliationUseCase;

    @Test
    @DisplayName("빈 DB에서 QA 시딩 전체를 부르면 계정 593개와 운영진 역할을 만들고, 다시 부르면 아무것도 바꾸지 않는다")
    void seedsQaBaseDataFromEmptyDatabaseAndSecondCallIsNoOp() throws Exception {
        // when
        JsonNode first = seedAll();

        // then
        assertThat(first.findValuesAsText("status")).as(first.toPrettyString()).doesNotContain("FAILED", "NOT_RUN");
        assertThat(first.get(0).path("created").asInt()).as(first.toPrettyString()).isEqualTo(593 + 1);

        QaAccountPlan plan = QaAccountPlan.from(loadQaRosterPort.load());
        Map<String, Long> memberIds = getMemberUseCase.findIdsByEmails(new HashSet<>(plan.emails()));
        assertThat(memberIds).hasSize(593);
        assertThat(getMemberUseCase.countAll()).isEqualTo(593);

        Long gisu11 = getGisuUseCase.getActiveGisuId();
        assertThat(roles(memberIds, RosterKey.of("duksung", 3), gisu11))
            .contains(ChallengerRoleType.CENTRAL_PRESIDENT, ChallengerRoleType.SCHOOL_ETC_ADMIN);
        assertThat(roles(memberIds, RosterKey.of("gachon", 30), gisu11))
            .containsExactly(ChallengerRoleType.CENTRAL_VICE_PRESIDENT);
        for (RosterKey chapterPresident : List.of(RosterKey.of("gachon", 2), RosterKey.of("soongsil", 1),
            RosterKey.of("cau", 1), RosterKey.of("hongik", 1), RosterKey.of("dongduk", 1))) {
            assertThat(roles(memberIds, chapterPresident, gisu11))
                .containsExactlyInAnyOrder(ChallengerRoleType.SCHOOL_PRESIDENT, ChallengerRoleType.CHAPTER_PRESIDENT);
        }
        assertThat(count("SELECT count(*) FROM challenger_role WHERE gisu_id = " + gisu11)).isEqualTo(152);
        assertThat(count("""
            SELECT count(*) FROM member_system_role r JOIN member m ON m.id = r.member_id
            WHERE m.email = 'superadmin@university.neordinary.com' AND r.role_type = 'SUPER_ADMIN'
            """)).isEqualTo(1);
        for (QaTransfer transfer : QaTransfers.ALL) {
            ProjectMatchingAffiliationInfo affiliation = getMyMatchingAffiliationUseCase.get(
                memberIds.get(transfer.member().email()), gisu11);
            assertThat(affiliation.transferred()).isTrue();
            assertThat(affiliation.matchingChapter().name()).isEqualTo(transfer.toChapter());
        }
        Map<String, Long> afterFirst = tableCounts();
        assertThat(afterFirst.get("project_matching_chapter_transfer")).isEqualTo(4);

        // when
        JsonNode second = seedAll();

        // then
        assertThat(second.findValuesAsText("status")).as(second.toPrettyString()).containsOnly("UNCHANGED");
        assertThat(tableCounts()).isEqualTo(afterFirst);
    }

    private JsonNode seedAll() throws Exception {
        String response = mockMvc.perform(post("/test/seed/qa"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("result").path("steps");
    }

    private List<ChallengerRoleType> roles(Map<String, Long> memberIds, RosterKey key, Long gisuId) {
        return listChallengerRoleUseCase.listByMemberIdAndGisuId(memberIds.get(key.email()), gisuId).stream()
            .map(ChallengerRoleInfo::roleType).toList();
    }

    private Map<String, Long> tableCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        COUNTED_TABLES.forEach(table -> counts.put(table, count("SELECT count(*) FROM " + table)));
        return counts;
    }

    private long count(String sql) {
        return ((Number) entityManager.createNativeQuery(sql).getSingleResult()).longValue();
    }
}
