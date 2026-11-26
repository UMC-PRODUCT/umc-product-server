package com.umc.product.inquiry.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.JsonNode;
import com.umc.product.maintenance.application.port.in.command.RefreshMaintenanceStateUseCase;
import com.umc.product.member.domain.MemberSystemRoleType;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.InquiryFixture;
import com.umc.product.support.fixture.MemberFixture;

class InquiryApiIntegrationTest extends IntegrationTestSupport {

    private static final String AUTHOR = "author-token";
    private static final String STAFF = "staff-token";
    private static final String STRANGER = "stranger-token";
    private static final String SUPER_ADMIN = "super-admin-token";

    @Autowired
    MemberFixture memberFixture;
    @Autowired
    InquiryFixture inquiryFixture;
    @Autowired
    RefreshMaintenanceStateUseCase refreshMaintenanceStateUseCase;

    private Long authorId;
    private Long staffId;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        refreshMaintenanceStateUseCase.refresh();
        authorId = memberFixture.일반("author").getId();
        staffId = memberFixture.일반("staff").getId();
        Long strangerId = memberFixture.일반("stranger").getId();
        memberFixture.시스템_역할_부여(staffId, MemberSystemRoleType.UMC_PRODUCT);
        categoryId = inquiryFixture.카테고리("계정·로그인", 2).getId();
        inquiryFixture.카테고리("출석·과제", 1);
        토큰(AUTHOR, authorId);
        토큰(STAFF, staffId);
        토큰(STRANGER, strangerId);
        Long superAdminId = memberFixture.일반("super-admin").getId();
        memberFixture.시스템_역할_부여(superAdminId, MemberSystemRoleType.SUPER_ADMIN);
        토큰(SUPER_ADMIN, superAdminId);
    }

    @Test
    @DisplayName("문의를 등록하면 201과 문의 ID를 돌려주고, 작성자는 상세를 본다")
    void 등록과_상세() throws Exception {
        // when
        long inquiryId = 등록();

        // then
        mockMvc.perform(get("/api/v1/inquiries/{inquiryId}", inquiryId)
                .header("Authorization", "Bearer " + AUTHOR))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.status").value("RECEIVED"))
            .andExpect(jsonPath("$.result.title").value("앱 로그인이 안 돼요"))
            .andExpect(jsonPath("$.result.targetTeam").value("PLATFORM"))
            .andExpect(jsonPath("$.result.category.name").value("계정·로그인"))
            .andExpect(jsonPath("$.result.canClose").value(false));
    }

    @Test
    @DisplayName("활성 문의 유형을 표시 순서대로 돌려준다")
    void 유형_목록() throws Exception {
        mockMvc.perform(get("/api/v1/inquiries/categories")
                .header("Authorization", "Bearer " + AUTHOR))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result[0].name").value("출석·과제"))
            .andExpect(jsonPath("$.result[1].name").value("계정·로그인"));
    }

    @Test
    @DisplayName("제3자는 403, 운영진은 메시지를 보고 문의를 종료한다")
    void 권한별_HTTP_상태() throws Exception {
        // given
        long inquiryId = 등록();

        // when & then
        mockMvc.perform(get("/api/v1/inquiries/{inquiryId}/messages", inquiryId)
                .header("Authorization", "Bearer " + STRANGER))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("INQUIRY-0002"));
        mockMvc.perform(post("/api/v1/inquiries/admin/{inquiryId}/close", inquiryId)
                .header("Authorization", "Bearer " + AUTHOR))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("INQUIRY-0003"));

        mockMvc.perform(get("/api/v1/inquiries/{inquiryId}/messages", inquiryId)
                .header("Authorization", "Bearer " + STAFF))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.messages[0].senderType").value("AUTHOR"));
        mockMvc.perform(post("/api/v1/inquiries/admin/{inquiryId}/close", inquiryId)
                .header("Authorization", "Bearer " + STAFF))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.status").value("CLOSED"));
        mockMvc.perform(post("/api/v1/inquiries/admin/{inquiryId}/close", inquiryId)
                .header("Authorization", "Bearer " + STAFF))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("작성자와 운영진 모두 보낸 사람을 실명과 회원 ID로 본다")
    void 보낸_사람_표시() throws Exception {
        // given
        long inquiryId = 등록();

        // when & then
        메시지_전송(STAFF, inquiryId, "확인해 볼게요")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.senderType").value("STAFF"))
            .andExpect(jsonPath("$.result.senderMemberId").value(staffId))
            .andExpect(jsonPath("$.result.senderName").value("staff"));
        메시지_전송(AUTHOR, inquiryId, "추가로 보내요")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.senderType").value("AUTHOR"))
            .andExpect(jsonPath("$.result.senderMemberId").value(authorId))
            .andExpect(jsonPath("$.result.senderName").value("author"));

        메시지_조회(AUTHOR, inquiryId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.messages[0].senderMemberId").value(authorId))
            .andExpect(jsonPath("$.result.messages[0].senderName").value("author"))
            .andExpect(jsonPath("$.result.messages[1].senderType").value("STAFF"))
            .andExpect(jsonPath("$.result.messages[1].senderMemberId").value(staffId))
            .andExpect(jsonPath("$.result.messages[1].senderName").value("staff"))
            .andExpect(jsonPath("$.result.messages[1].content").value("확인해 볼게요"));
        메시지_조회(STAFF, inquiryId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.messages[0].senderName").value("author"))
            .andExpect(jsonPath("$.result.messages[1].senderMemberId").value(staffId))
            .andExpect(jsonPath("$.result.messages[1].senderName").value("staff"));
    }

    @Test
    @DisplayName("SUPER_ADMIN만 가진 회원도 메시지를 조회하면 운영진 실명과 회원 ID를 본다")
    void 슈퍼_어드민_보낸_사람_표시() throws Exception {
        // given
        long inquiryId = 등록();
        메시지_전송(STAFF, inquiryId, "확인해 볼게요").andExpect(status().isOk());

        // when & then
        메시지_조회(SUPER_ADMIN, inquiryId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.messages[0].senderType").value("STAFF"))
            .andExpect(jsonPath("$.result.messages[0].senderMemberId").value(staffId))
            .andExpect(jsonPath("$.result.messages[0].senderName").value("staff"));
    }

    @Test
    @DisplayName("clientMessageId 없이 메시지를 보내면 400을 돌려준다")
    void 메시지_요청_검증() throws Exception {
        long inquiryId = 등록();

        mockMvc.perform(post("/api/v1/inquiries/{inquiryId}/messages", inquiryId)
                .header("Authorization", "Bearer " + AUTHOR)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("content", "안녕하세요"))))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("해석할 수 없는 목록 커서는 400과 문의 에러 코드를 돌려준다")
    void 잘못된_목록_커서() throws Exception {
        mockMvc.perform(get("/api/v1/inquiries/me")
                .param("cursor", "%%%")
                .header("Authorization", "Bearer " + AUTHOR))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INQUIRY-0014"));
    }

    @Test
    @DisplayName("본문 길이와 첨부 개수 한도를 넘으면 400과 문의 에러 코드를 돌려준다")
    void 길이와_개수_한도() throws Exception {
        long inquiryId = 등록();
        List<String> sixFiles = List.of("a", "b", "c", "d", "e", "f");

        등록_요청("가".repeat(2001), List.of())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INQUIRY-0007"));
        등록_요청("   ", List.of())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INQUIRY-0012"));
        등록_요청("내용", sixFiles)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INQUIRY-0010"));
        보내기_요청(inquiryId, Map.of("content", " ", "clientMessageId", UUID.randomUUID().toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INQUIRY-0008"));
        보내기_요청(inquiryId, Map.of(
            "content", "가".repeat(2001), "clientMessageId", UUID.randomUUID().toString()
        ))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INQUIRY-0007"));
        보내기_요청(inquiryId, Map.of(
            "fileMetadataIds", sixFiles, "clientMessageId", UUID.randomUUID().toString()
        ))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INQUIRY-0010"));
    }

    @Test
    @DisplayName("운영진 새 문의 알림은 처음엔 꺼져 있고, 켜고 끌 수 있으며 같은 값을 다시 보내도 결과가 같다")
    void 알림_설정() throws Exception {
        알림_설정_조회(STAFF)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.enabled").value(false));

        알림_설정_변경(STAFF, Map.of("enabled", true))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.enabled").value(true));
        알림_설정_변경(STAFF, Map.of("enabled", true))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.enabled").value(true));
        알림_설정_조회(STAFF)
            .andExpect(jsonPath("$.result.enabled").value(true));

        알림_설정_변경(STAFF, Map.of("enabled", false))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.enabled").value(false));
        알림_설정_조회(STAFF)
            .andExpect(jsonPath("$.result.enabled").value(false));
    }

    @Test
    @DisplayName("운영진이 아니면 문의 알림 설정을 보거나 바꿀 수 없고, enabled 없이 바꾸면 400이다")
    void 알림_설정_권한과_검증() throws Exception {
        알림_설정_조회(AUTHOR)
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("INQUIRY-0003"));
        알림_설정_변경(AUTHOR, Map.of("enabled", false))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("INQUIRY-0003"));
        알림_설정_변경(STAFF, Map.of())
            .andExpect(status().isBadRequest());
    }

    private ResultActions 메시지_전송(String token, long inquiryId, String content) throws Exception {
        return mockMvc.perform(post("/api/v1/inquiries/{inquiryId}/messages", inquiryId)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of(
                "content", content, "clientMessageId", UUID.randomUUID().toString()
            ))));
    }

    private ResultActions 메시지_조회(String token, long inquiryId) throws Exception {
        return mockMvc.perform(get("/api/v1/inquiries/{inquiryId}/messages", inquiryId)
            .header("Authorization", "Bearer " + token));
    }

    private ResultActions 알림_설정_조회(String token) throws Exception {
        return mockMvc.perform(get("/api/v1/inquiries/admin/alarm-setting")
            .header("Authorization", "Bearer " + token));
    }

    private ResultActions 알림_설정_변경(String token, Map<String, Object> body) throws Exception {
        return mockMvc.perform(put("/api/v1/inquiries/admin/alarm-setting")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)));
    }

    private long 등록() throws Exception {
        String response = 등록_요청("앱 로그인이 안 돼요\n로그인 버튼을 눌러도 반응이 없어요", List.of())
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        JsonNode result = objectMapper.readTree(response).path("result");
        return result.path("inquiryId").asLong();
    }

    private ResultActions 등록_요청(String content, List<String> fileMetadataIds) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
            "categoryId", categoryId,
            "targetTeam", "PLATFORM",
            "content", content,
            "fileMetadataIds", fileMetadataIds,
            "clientRequestId", UUID.randomUUID().toString()
        ));
        return mockMvc.perform(post("/api/v1/inquiries")
            .header("Authorization", "Bearer " + AUTHOR)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));
    }

    private ResultActions 보내기_요청(long inquiryId, Map<String, Object> body) throws Exception {
        return mockMvc.perform(post("/api/v1/inquiries/{inquiryId}/messages", inquiryId)
            .header("Authorization", "Bearer " + AUTHOR)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)));
    }

    private void 토큰(String token, Long memberId) {
        given(jwtTokenProvider.validateAccessToken(token)).willReturn(true);
        given(jwtTokenProvider.parseAccessToken(token)).willReturn(memberId);
        given(jwtTokenProvider.getRolesFromAccessToken(token)).willReturn(List.of());
    }
}
