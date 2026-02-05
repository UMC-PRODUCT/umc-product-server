package com.umc.product.organization.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import com.umc.product.support.DocumentationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

class ChapterQueryControllerTest extends DocumentationTest {

    @Test
    void 지부_목록을_조회합니다() throws Exception {
        // given
        List<ChapterInfo> chapters = List.of(
                new ChapterInfo(1L, "Scorpio 지부"),
                new ChapterInfo(2L, "Ain 지부"),
                new ChapterInfo(3L, "Leo 지부")
        );

        given(getChapterUseCase.getAllChapters()).willReturn(chapters);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/chapters"));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
                                fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                fieldWithPath("result.chapters").type(JsonFieldType.ARRAY).description("지부 목록"),
                                fieldWithPath("result.chapters[].id").type(JsonFieldType.STRING).description("지부 ID"),
                                fieldWithPath("result.chapters[].name").type(JsonFieldType.STRING).description("지부 이름")
                        )
                ));
    }
}
