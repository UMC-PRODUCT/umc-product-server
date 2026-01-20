package com.umc.product.organization.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.organization.adapter.in.web.dto.request.CreateGisuRequest;
import com.umc.product.organization.adapter.in.web.dto.request.UpdateGisuRequest;
import com.umc.product.support.DocumentationTest;
import java.time.LocalDate;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

class GisuControllerTest extends DocumentationTest {

    @Test
    @Disabled("아직 없는 기능")
    void 신규_기수를_추가한다() throws Exception {
        // given
        CreateGisuRequest request = new CreateGisuRequest(9L, LocalDate.of(2025, 3, 1), LocalDate.of(2025, 8, 31));

        given(manageGisuUseCase.register(any())).willReturn(1L);

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/admin/gisuId").content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk()).andDo(restDocsHandler.document(
                requestFields(fieldWithPath("number").type(JsonFieldType.STRING).description("기수 번호"),
                        fieldWithPath("startAt").type(JsonFieldType.STRING).description("기수 시작일시"),
                        fieldWithPath("endAt").type(JsonFieldType.STRING).description("기수 종료일시"))));
    }

    @Test
    @Disabled("아직 없는 기능")
    void 기수_정보를_수정한다() throws Exception {
        // given
        Long gisuId = 1L;
        UpdateGisuRequest request = new UpdateGisuRequest(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 9, 30));

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/admin/gisuId/{gisuId}", gisuId).content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(pathParameters(parameterWithName("gisuId").description("기수 ID")),
                        requestFields(fieldWithPath("startAt").type(JsonFieldType.STRING).description("기수 시작일시"),
                                fieldWithPath("endAt").type(JsonFieldType.STRING).description("기수 종료일시"))));
    }

    @Test
    @Disabled("아직 없는 기능")
    void 기수를_삭제한다() throws Exception {
        // given
        Long gisuId = 1L;

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/admin/gisuId/{gisuId}", gisuId));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(pathParameters(parameterWithName("gisuId").description("기수 ID"))));
    }

    @Test
    void 현재_기수를_설정한다() throws Exception {
        // given
        Long gisuId = 3L;

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/admin/gisuId/{gisuId}/active", gisuId));

        // then
        result.andExpect(status().isOk()).andDo(restDocsHandler.document(
                pathParameters(parameterWithName("gisuId").description("현재 기수로 설정할 기수 ID"))));
    }
}
