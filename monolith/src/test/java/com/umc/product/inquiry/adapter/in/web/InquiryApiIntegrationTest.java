package com.umc.product.inquiry.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.umc.product.maintenance.application.port.in.command.RefreshMaintenanceStateUseCase;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.InquiryFixture;
import com.umc.product.support.fixture.MemberFixture;

class InquiryApiIntegrationTest extends IntegrationTestSupport {

    private static final String AUTHOR = "author-token";
    private static final String STAFF = "staff-token";
    private static final String STRANGER = "stranger-token";

    @Autowired
    MemberFixture memberFixture;
    @Autowired
    InquiryFixture inquiryFixture;
    @Autowired
    RefreshMaintenanceStateUseCase refreshMaintenanceStateUseCase;

    private Long categoryId;

    @BeforeEach
    void setUp() {
        refreshMaintenanceStateUseCase.refresh();
        Long authorId = memberFixture.일반("author").getId();
        Long staffId = memberFixture.일반("staff").getId();
        Long strangerId = memberFixture.일반("stranger").getId();
        inquiryFixture.운영진으로_지정(staffId);
        categoryId = inquiryFixture.카테고리("UMC 서비스", 1).getId();
        inquiryFixture.카테고리("기타", 2);
        토큰(AUTHOR, authorId);
        토큰(STAFF, staffId);
        토큰(STRANGER, strangerId);
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
            .andExpect(jsonPath("$.result.targetTeam").value("PLATFORM"))
            .andExpect(jsonPath("$.result.category.name").value("UMC 서비스"))
            .andExpect(jsonPath("$.result.canSendMessage").value(true))
            .andExpect(jsonPath("$.result.canClose").value(false));
    }

    @Test
    @DisplayName("활성 문의 유형을 표시 순서대로 돌려준다")
    void 유형_목록() throws Exception {
        mockMvc.perform(get("/api/v1/inquiries/categories")
                .header("Authorization", "Bearer " + AUTHOR))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result[0].name").value("UMC 서비스"))
            .andExpect(jsonPath("$.result[1].name").value("기타"));
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
    @DisplayName("clientMessageId 없이 메시지를 보내면 400을 돌려준다")
    void 메시지_요청_검증() throws Exception {
        long inquiryId = 등록();

        mockMvc.perform(post("/api/v1/inquiries/{inquiryId}/messages", inquiryId)
                .header("Authorization", "Bearer " + AUTHOR)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("content", "안녕하세요"))))
            .andExpect(status().isBadRequest());
    }

    private long 등록() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
            "categoryId", categoryId,
            "targetTeam", "PLATFORM",
            "title", "앱 로그인이 안 돼요",
            "content", "로그인 버튼을 눌러도 반응이 없어요",
            "fileMetadataIds", List.of(),
            "clientRequestId", UUID.randomUUID().toString()
        ));
        String response = mockMvc.perform(post("/api/v1/inquiries")
                .header("Authorization", "Bearer " + AUTHOR)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        JsonNode result = objectMapper.readTree(response).path("result");
        return result.path("inquiryId").asLong();
    }

    private void 토큰(String token, Long memberId) {
        given(jwtTokenProvider.validateAccessToken(token)).willReturn(true);
        given(jwtTokenProvider.parseAccessToken(token)).willReturn(memberId);
        given(jwtTokenProvider.getRolesFromAccessToken(token)).willReturn(List.of());
    }
}
