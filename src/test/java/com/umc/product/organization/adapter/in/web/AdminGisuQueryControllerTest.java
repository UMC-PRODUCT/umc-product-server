package com.umc.product.organization.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import com.umc.product.support.DocumentationTest;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

class AdminGisuQueryControllerTest extends DocumentationTest {

    @Test
    void 기수_목록을_조회한다() throws Exception {
        // given
        List<GisuInfo> gisuList = List.of(
                new GisuInfo(1L, 7L, Instant.parse("2024-03-01T00:00:00Z"), Instant.parse("2024-08-31T23:59:59Z"), false),
                new GisuInfo(2L, 8L, Instant.parse("2024-09-01T00:00:00Z"), Instant.parse("2025-02-28T23:59:59Z"), false),
                new GisuInfo(3L, 9L, Instant.parse("2025-03-01T00:00:00Z"), Instant.parse("2025-08-31T23:59:59Z"), true));

        given(getGisuUseCase.getList()).willReturn(gisuList);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/gisu"));

        // then
        result.andExpect(status().isOk()).andDo(restDocsHandler.document(
                responseFields(fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
                        fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("result.gisuList").type(JsonFieldType.ARRAY).description("기수 목록"),
                        fieldWithPath("result.gisuList[].gisuId").type(JsonFieldType.STRING).description("기수 ID"),
                        fieldWithPath("result.gisuList[].generation").type(JsonFieldType.STRING).description("기수 번호"),
                        fieldWithPath("result.gisuList[].startAt").type(JsonFieldType.STRING).description("기수 시작일시"),
                        fieldWithPath("result.gisuList[].endAt").type(JsonFieldType.STRING).description("기수 종료일시"),
                        fieldWithPath("result.gisuList[].isActive").type(JsonFieldType.BOOLEAN)
                                .description("현재 기수 여부"))));
    }
}
