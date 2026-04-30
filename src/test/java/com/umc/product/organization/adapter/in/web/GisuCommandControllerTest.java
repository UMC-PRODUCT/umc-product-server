package com.umc.product.organization.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.organization.adapter.in.web.dto.request.CreateGisuRequest;
import com.umc.product.support.DocumentationTest;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

class GisuCommandControllerTest extends DocumentationTest {

    private static final Instant START_AT = Instant.parse("2025-03-01T00:00:00Z");
    private static final Instant END_AT = Instant.parse("2025-08-31T23:59:59Z");

    @Test
    void 신규_기수를_추가한다() throws Exception {
        // given
        CreateGisuRequest request = new CreateGisuRequest(9L, START_AT, END_AT);

        given(manageGisuUseCase.create(any())).willReturn(1L);

        // when
        ResultActions result = mockMvc.perform(
            post("/api/v1/gisu")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk()).andDo(restDocsHandler.document(
            requestFields(
                fieldWithPath("generation").type(JsonFieldType.STRING).description("기수 번호"),
                fieldWithPath("startAt").type(JsonFieldType.STRING).description("기수 시작일시"),
                fieldWithPath("endAt").type(JsonFieldType.STRING).description("기수 종료일시"))));
    }

    @Test
    void 기수를_삭제한다() throws Exception {
        // given
        Long gisuId = 1L;

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/gisu/{gisuId}", gisuId));

        // then
        result.andExpect(status().isOk())
            .andDo(restDocsHandler.document(pathParameters(parameterWithName("gisuId").description("기수 ID"))));
    }

    @Test
    void 현재_기수를_설정한다() throws Exception {
        // given
        Long gisuId = 3L;

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/gisu/{gisuId}/active", gisuId));

        // then
        result.andExpect(status().isOk()).andDo(restDocsHandler.document(
            pathParameters(parameterWithName("gisuId").description("현재 기수로 설정할 기수 ID"))));
    }
}
