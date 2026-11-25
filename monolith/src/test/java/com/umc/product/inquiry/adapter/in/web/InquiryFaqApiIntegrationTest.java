package com.umc.product.inquiry.adapter.in.web;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.umc.product.maintenance.application.port.in.command.RefreshMaintenanceStateUseCase;
import com.umc.product.member.domain.MemberSystemRoleType;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.InquiryFixture;
import com.umc.product.support.fixture.MemberFixture;

@DisplayName("대표 질문(FAQ) API")
class InquiryFaqApiIntegrationTest extends IntegrationTestSupport {

    private static final String USER = "user-token";
    private static final String OTHER_USER = "other-user-token";
    private static final String THIRD_USER = "third-user-token";
    private static final String STAFF = "staff-token";
    private static final Map<String, Boolean> ALL_CHECKED = Map.of(
        "noPersonalInformation", true, "scopeConfirmed", true, "answerVerified", true
    );

    @Autowired
    MemberFixture memberFixture;
    @Autowired
    InquiryFixture inquiryFixture;
    @Autowired
    RefreshMaintenanceStateUseCase refreshMaintenanceStateUseCase;
    @Autowired
    JdbcTemplate jdbcTemplate;

    private Long staffId;
    private Long categoryId;
    private Long otherCategoryId;
    private Long audienceId;

    @BeforeEach
    void setUp() {
        refreshMaintenanceStateUseCase.refresh();
        Long userId = memberFixture.일반("user").getId();
        Long otherUserId = memberFixture.일반("other").getId();
        staffId = memberFixture.일반("staff").getId();
        memberFixture.시스템_역할_부여(staffId, MemberSystemRoleType.UMC_PRODUCT);
        categoryId = inquiryFixture.카테고리("계정·로그인", 2).getId();
        otherCategoryId = inquiryFixture.카테고리("출석·과제", 1).getId();
        audienceId = inquiryFixture.대상("UMC 서비스 사용자").getId();
        토큰(USER, userId);
        토큰(OTHER_USER, otherUserId);
        토큰(THIRD_USER, memberFixture.일반("third").getId());
        토큰(STAFF, staffId);
    }

    @Test
    @DisplayName("초안은 질문만으로 만들고, 검증 체크가 빠지거나 답변·카테고리·대상이 없으면 발행할 수 없다")
    void 초안과_발행_조건() throws Exception {
        // given
        long faqId = 초안(Map.of("question", "  비밀번호를 잊었어요  "));

        // when & then
        운영(get("/api/v1/inquiries/admin/faqs/{faqId}", faqId), STAFF)
            .andExpect(jsonPath("$.result.status").value("DRAFT"))
            .andExpect(jsonPath("$.result.draft.question").value("비밀번호를 잊었어요"))
            .andExpect(jsonPath("$.result.published").value(nullValue()))
            .andExpect(jsonPath("$.result.verifiedBy").value("UMC PRODUCT"));
        발행(faqId, Map.of("noPersonalInformation", true, "scopeConfirmed", false, "answerVerified", true))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INQUIRY-0027"));
        발행(faqId, ALL_CHECKED)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INQUIRY-0026"));
        mockMvc.perform(get("/api/v1/inquiries/faqs/{faqId}", faqId).header("Authorization", "Bearer " + USER))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("INQUIRY-0020"));
        운영(post("/api/v1/inquiries/admin/faqs/{faqId}/archive", faqId), STAFF)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("INQUIRY-0028"));
        운영(post("/api/v1/inquiries/admin/faqs"), USER, Map.of("question", "질문"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("발행 중에 고치면 기존 발행본이 계속 공개되고, 다시 발행하면 수정본이 공개된다")
    void 발행본과_수정본() throws Exception {
        // given
        long faqId = 발행된_대표_질문("비밀번호를 잊었어요", "재설정 메일을 보내요", categoryId);

        // when
        운영(put("/api/v1/inquiries/admin/faqs/{faqId}/draft", faqId), STAFF,
            내용("비밀번호를 잊었어요", "앱에서 재설정하세요", categoryId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.hasUnpublishedChanges").value(true))
            .andExpect(jsonPath("$.result.published.answer").value("재설정 메일을 보내요"))
            .andExpect(jsonPath("$.result.draft.answer").value("앱에서 재설정하세요"));

        // then
        사용자(get("/api/v1/inquiries/faqs/{faqId}", faqId))
            .andExpect(jsonPath("$.result.answer").value("재설정 메일을 보내요"))
            .andExpect(jsonPath("$.result.verifiedBy").value("UMC PRODUCT"))
            .andExpect(jsonPath("$.result.audience.name").value("UMC 서비스 사용자"))
            .andExpect(jsonPath("$.result.aliases").doesNotExist());
        발행(faqId, ALL_CHECKED)
            .andExpect(jsonPath("$.result.hasUnpublishedChanges").value(false))
            .andExpect(jsonPath("$.result.publishedBy.memberId").value(staffId));
        사용자(get("/api/v1/inquiries/faqs/{faqId}", faqId))
            .andExpect(jsonPath("$.result.answer").value("앱에서 재설정하세요"));
    }

    @Test
    @DisplayName("보관하면 공개 목록·상세·검색에서 바로 빠지고, 다시 발행할 수 있다. 종료일이 지나면 자동으로 내려간다")
    void 보관과_종료일() throws Exception {
        // given
        long archived = 발행된_대표_질문("출석이 안 찍혀요", "출석 화면을 새로고침하세요", categoryId);
        long expired = 초안(내용("지난 행사 안내", "끝난 행사예요", categoryId, LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1)));
        발행(expired, ALL_CHECKED).andExpect(status().isOk());
        long endsToday = 초안(내용("오늘까지 안내", "오늘 끝나요", categoryId, LocalDate.now(ZoneId.of("Asia/Seoul"))));
        발행(endsToday, ALL_CHECKED).andExpect(status().isOk());

        // when
        운영(post("/api/v1/inquiries/admin/faqs/{faqId}/archive", archived), STAFF)
            .andExpect(jsonPath("$.result.status").value("ARCHIVED"));

        // then
        사용자(get("/api/v1/inquiries/faqs").param("categoryId", categoryId.toString()))
            .andExpect(jsonPath("$.result[*].faqId", contains(String.valueOf(endsToday))));
        사용자(get("/api/v1/inquiries/faqs/{faqId}", archived)).andExpect(status().isNotFound());
        사용자(get("/api/v1/inquiries/faqs/{faqId}", expired)).andExpect(status().isNotFound());
        사용자(get("/api/v1/inquiries/faqs/search").param("keyword", "출석"))
            .andExpect(jsonPath("$.result", empty()));

        발행(archived, ALL_CHECKED).andExpect(jsonPath("$.result.status").value("PUBLISHED"));
        사용자(get("/api/v1/inquiries/faqs/{faqId}", archived)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("검색은 질문·별칭·답변을 대소문자 없이 부분 일치로 찾고, 결과가 없으면 빈 목록이다")
    void 검색() throws Exception {
        // given
        Map<String, Object> content = 내용("앱 로그인이 안 돼요", "SSO 설정을 확인하세요", categoryId);
        content.put("aliases", List.of("Login 오류", "  로그인 실패 ", "로그인 실패"));
        long faqId = 초안(content);
        발행(faqId, ALL_CHECKED)
            .andExpect(jsonPath("$.result.published.aliases", contains("Login 오류", "로그인 실패")));
        발행된_대표_질문("수료증은 어디서 받나요", "마이페이지에서 받아요", otherCategoryId);

        // when & then
        사용자(get("/api/v1/inquiries/faqs/search").param("keyword", "LOGIN"))
            .andExpect(jsonPath("$.result[*].faqId", contains(String.valueOf(faqId))))
            .andExpect(jsonPath("$.result[0].question").value("앱 로그인이 안 돼요"))
            .andExpect(jsonPath("$.result[0].category.name").value("계정·로그인"))
            .andExpect(jsonPath("$.result[0].audience.name").value("UMC 서비스 사용자"));
        사용자(get("/api/v1/inquiries/faqs/search").param("keyword", "sso"))
            .andExpect(jsonPath("$.result", hasSize(1)));
        사용자(get("/api/v1/inquiries/faqs/search").param("keyword", "100%"))
            .andExpect(jsonPath("$.result", empty()));
    }

    @Test
    @DisplayName("해결됐어요는 회원마다 한 번만 세고 운영진만 수를 보며, 공개 중이 아니면 404다")
    void 해결됐어요() throws Exception {
        // given
        long faqId = 발행된_대표_질문("비밀번호를 잊었어요", "재설정 메일을 보내요", categoryId);
        long draftId = 초안(Map.of("question", "초안"));

        // when
        사용자(post("/api/v1/inquiries/faqs/{faqId}/resolution", faqId)).andExpect(status().isOk());
        사용자(post("/api/v1/inquiries/faqs/{faqId}/resolution", faqId)).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/inquiries/faqs/{faqId}/resolution", faqId)
                .header("Authorization", "Bearer " + OTHER_USER))
            .andExpect(status().isOk());

        // then
        사용자(post("/api/v1/inquiries/faqs/{faqId}/resolution", draftId)).andExpect(status().isNotFound());
        운영(get("/api/v1/inquiries/admin/faqs/{faqId}", faqId), STAFF)
            .andExpect(jsonPath("$.result.resolvedCount").value(2));
        운영(get("/api/v1/inquiries/admin/faqs").param("status", "PUBLISHED"), STAFF)
            .andExpect(jsonPath("$.result[0].resolvedCount").value(2));
        사용자(get("/api/v1/inquiries/faqs/{faqId}", faqId))
            .andExpect(jsonPath("$.result.resolvedCount").doesNotExist());
    }

    @Test
    @DisplayName("관련 안내는 공개 중일 때만 사용자에게 보이고, 자기 자신은 관련 안내가 될 수 없다")
    void 관련_안내() throws Exception {
        // given
        long related = 발행된_대표_질문("계정을 바꾸고 싶어요", "설정에서 바꿔요", categoryId);
        Map<String, Object> content = 내용("로그인이 안 돼요", "다시 로그인하세요", categoryId);
        content.put("relatedFaqId", related);
        long faqId = 초안(content);
        발행(faqId, ALL_CHECKED).andExpect(status().isOk());

        // when & then
        사용자(get("/api/v1/inquiries/faqs/{faqId}", faqId))
            .andExpect(jsonPath("$.result.related.faqId").value(related))
            .andExpect(jsonPath("$.result.related.question").value("계정을 바꾸고 싶어요"));
        운영(post("/api/v1/inquiries/admin/faqs/{faqId}/archive", related), STAFF).andExpect(status().isOk());
        사용자(get("/api/v1/inquiries/faqs/{faqId}", faqId))
            .andExpect(jsonPath("$.result.related").value(nullValue()));

        Map<String, Object> self = 내용("로그인이 안 돼요", "다시 로그인하세요", categoryId);
        self.put("relatedFaqId", faqId);
        운영(put("/api/v1/inquiries/admin/faqs/{faqId}/draft", faqId), STAFF, self)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INQUIRY-0024"));
    }

    @Test
    @DisplayName("먼저 확인해 보세요는 분류에서 최근 발행한 대표 질문을, 없으면 null을 돌려준다")
    void 먼저_확인해_보세요() throws Exception {
        // given
        발행된_대표_질문("예전 안내", "예전 답변", categoryId);
        long latest = 발행된_대표_질문("최근 안내", "최근 답변", categoryId);

        // when & then
        사용자(get("/api/v1/inquiries/faqs/suggestion").param("categoryId", categoryId.toString()))
            .andExpect(jsonPath("$.result.faq.faqId").value(latest));
        사용자(get("/api/v1/inquiries/faqs/suggestion").param("categoryId", otherCategoryId.toString()))
            .andExpect(jsonPath("$.result.faq").value(nullValue()));
    }

    @Test
    @DisplayName("문의를 대표 질문에 연결·해제하고, 문의 제목으로 새 초안을 만들어 연결하며, 미분류 필터와 참고한 대표 질문이 보인다")
    void 문의_연결() throws Exception {
        // given
        long referenced = 발행된_대표_질문("비밀번호를 잊었어요", "재설정 메일을 보내요", categoryId);
        long linked = 문의_등록("로그인이 안 돼요\n어제부터요", referenced);
        long unclassified = 문의_등록("출석이 안 찍혀요", null);

        // when
        운영(put("/api/v1/inquiries/admin/{inquiryId}/faq", linked), STAFF, Map.of("faqId", referenced))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.faq.faqId").value(referenced))
            .andExpect(jsonPath("$.result.referencedFaq.question").value("비밀번호를 잊었어요"));

        // then
        운영(get("/api/v1/inquiries/admin").param("unclassified", "true"), STAFF)
            .andExpect(jsonPath("$.result.inquiries[*].inquiryId", contains(String.valueOf(unclassified))));
        운영(post("/api/v1/inquiries/admin/{inquiryId}/faq/draft", unclassified), STAFF)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.faq.question").value("출석이 안 찍혀요"));
        운영(get("/api/v1/inquiries/admin/faqs").param("status", "DRAFT"), STAFF)
            .andExpect(jsonPath("$.result[0].question").value("출석이 안 찍혀요"))
            .andExpect(jsonPath("$.result[0].category.name").value("계정·로그인"));
        Map<String, Object> unlink = new HashMap<>();
        unlink.put("faqId", null);
        운영(put("/api/v1/inquiries/admin/{inquiryId}/faq", linked), STAFF, unlink)
            .andExpect(jsonPath("$.result.faq").value(nullValue()));
        mockMvc.perform(post("/api/v1/inquiries")
                .header("Authorization", "Bearer " + USER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "categoryId", categoryId, "content", "내용", "clientRequestId", UUID.randomUUID().toString(),
                    "referencedFaqId", 999_999
                ))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("INQUIRY-0020"));
    }

    @Test
    @DisplayName("인기 순위는 공개 중인 대표 질문을 서로 다른 문의자 수로 세고, 같은 수면 최근 문의가 앞이며, 기간 밖과 미분류는 세지 않는다")
    void 인기_순위() throws Exception {
        // given
        long popular = 발행된_대표_질문("로그인이 안 돼요", "다시 로그인하세요", categoryId);
        long older = 발행된_대표_질문("출석이 안 찍혀요", "새로고침하세요", otherCategoryId);
        long newer = 발행된_대표_질문("수료증은 어디서 받나요", "마이페이지에서 받아요", otherCategoryId);
        long archived = 발행된_대표_질문("보관할 안내", "보관해요", categoryId);
        long draft = 초안(내용("아직 답이 없는 질문", null, categoryId));
        연결된_문의(USER, popular);
        연결된_문의(USER, popular);
        연결된_문의(OTHER_USER, popular);
        연결된_문의(USER, older);
        연결된_문의(OTHER_USER, newer);
        long weekOld = 연결된_문의(THIRD_USER, older);
        jdbcTemplate.update("update inquiry set created_at = now() - interval '3 days' where id = ?", weekOld);
        연결된_문의(USER, archived);
        연결된_문의(OTHER_USER, archived);
        연결된_문의(THIRD_USER, archived);
        운영(post("/api/v1/inquiries/admin/faqs/{faqId}/archive", archived), STAFF).andExpect(status().isOk());
        연결된_문의(USER, draft);
        연결된_문의(OTHER_USER, draft);
        문의_등록("미분류 문의", null);

        // when & then
        사용자(get("/api/v1/inquiries/faqs/rankings").param("period", "DAY"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.period").value("DAY"))
            .andExpect(jsonPath("$.result.refreshedAt").exists())
            .andExpect(jsonPath("$.result.items[*].faqId",
                contains(String.valueOf(popular), String.valueOf(newer), String.valueOf(older))))
            .andExpect(jsonPath("$.result.items[*].askedMemberCount", contains("2", "1", "1")))
            .andExpect(jsonPath("$.result.items[0].rank").value(1))
            .andExpect(jsonPath("$.result.items[0].question").value("로그인이 안 돼요"))
            .andExpect(jsonPath("$.result.items[0].category.name").value("계정·로그인"))
            .andExpect(jsonPath("$.result.items[0].audience.name").value("UMC 서비스 사용자"));
        사용자(get("/api/v1/inquiries/faqs/rankings").param("period", "WEEK"))
            .andExpect(jsonPath("$.result.items[*].faqId",
                contains(String.valueOf(older), String.valueOf(popular), String.valueOf(newer))))
            .andExpect(jsonPath("$.result.items[*].askedMemberCount", contains("2", "2", "1")));
        운영(get("/api/v1/inquiries/admin/faqs/answer-needed"), STAFF)
            .andExpect(jsonPath("$.result[*].faqId", contains(String.valueOf(draft))))
            .andExpect(jsonPath("$.result[0].question").value("아직 답이 없는 질문"))
            .andExpect(jsonPath("$.result[0].askedMemberCount").value(2));
        운영(get("/api/v1/inquiries/admin/faqs/answer-needed"), USER).andExpect(status().isForbidden());
        사용자(get("/api/v1/inquiries/faqs/suggestion").param("categoryId", otherCategoryId.toString()))
            .andExpect(jsonPath("$.result.faq.faqId").value(older));
    }

    private long 연결된_문의(String token, long faqId) throws Exception {
        String response = 운영(post("/api/v1/inquiries"), token, Map.of(
                "categoryId", categoryId, "content", "문의", "clientRequestId", UUID.randomUUID().toString()
            ))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long inquiryId = objectMapper.readTree(response).at("/result/inquiryId").asLong();
        운영(put("/api/v1/inquiries/admin/{inquiryId}/faq", inquiryId), STAFF, Map.of("faqId", faqId))
            .andExpect(status().isOk());
        return inquiryId;
    }

    private long 발행된_대표_질문(String question, String answer, Long category) throws Exception {
        long faqId = 초안(내용(question, answer, category));
        발행(faqId, ALL_CHECKED).andExpect(status().isOk());
        return faqId;
    }

    private Map<String, Object> 내용(String question, String answer, Long category) {
        return 내용(question, answer, category, null);
    }

    private Map<String, Object> 내용(String question, String answer, Long category, LocalDate endDate) {
        Map<String, Object> content = new HashMap<>();
        content.put("question", question);
        content.put("answer", answer);
        content.put("categoryId", category);
        content.put("audienceId", audienceId);
        content.put("endDate", endDate == null ? null : endDate.toString());
        return content;
    }

    private long 초안(Map<String, ?> content) throws Exception {
        String response = 운영(post("/api/v1/inquiries/admin/faqs"), STAFF, content)
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).at("/result/faqId").asLong();
    }

    private ResultActions 발행(long faqId, Map<String, Boolean> checks) throws Exception {
        return 운영(post("/api/v1/inquiries/admin/faqs/{faqId}/publish", faqId), STAFF, checks);
    }

    private long 문의_등록(String content, Long referencedFaqId) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("categoryId", categoryId);
        body.put("content", content);
        body.put("clientRequestId", UUID.randomUUID().toString());
        body.put("referencedFaqId", referencedFaqId);
        String response = 운영(post("/api/v1/inquiries"), USER, body)
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).at("/result/inquiryId").asLong();
    }

    private ResultActions 사용자(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request.header("Authorization", "Bearer " + USER));
    }

    private ResultActions 운영(MockHttpServletRequestBuilder request, String token) throws Exception {
        return mockMvc.perform(request.header("Authorization", "Bearer " + token));
    }

    private ResultActions 운영(MockHttpServletRequestBuilder request, String token, Object body) throws Exception {
        return mockMvc.perform(request
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)));
    }

    private void 토큰(String token, Long memberId) {
        given(jwtTokenProvider.validateAccessToken(token)).willReturn(true);
        given(jwtTokenProvider.parseAccessToken(token)).willReturn(memberId);
        given(jwtTokenProvider.getRolesFromAccessToken(token)).willReturn(List.of());
    }
}
