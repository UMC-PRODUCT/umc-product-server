package com.umc.product.organization.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.organization.adapter.in.web.dto.request.CreateChapterRequest;
import com.umc.product.support.DocumentationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

class ChapterControllerTest extends DocumentationTest {

    @Test
    void 신규_지부를_생성한다() throws Exception {
        // given
        CreateChapterRequest request = new CreateChapterRequest(1L, "Scorpio");

        given(manageChapterUseCase.create(any())).willReturn(1L);

        // when
        ResultActions result = mockMvc.perform(
                post("/v1/admin/chapters").content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk()).andDo(restDocsHandler.document(
                requestFields(fieldWithPath("gisuId").type(JsonFieldType.STRING).description("기수 ID"),
                        fieldWithPath("name").type(JsonFieldType.STRING).description("지부명"))));
    }
}
