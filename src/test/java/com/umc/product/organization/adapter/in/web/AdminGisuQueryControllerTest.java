package com.umc.product.organization.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import com.umc.product.support.DocumentationTest;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

class AdminGisuQueryControllerTest extends DocumentationTest {

    @Test
    void 기수_목록을_페이징_조회한다() throws Exception {
        // given
        int page = 0;
        int size = 10;

        List<GisuInfo> gisuList = List.of(
                new GisuInfo(3L, 9L, Instant.parse("2025-03-01T00:00:00Z"), Instant.parse("2025-08-31T23:59:59Z"), true),
                new GisuInfo(2L, 8L, Instant.parse("2024-09-01T00:00:00Z"), Instant.parse("2025-02-28T23:59:59Z"), false),
                new GisuInfo(1L, 7L, Instant.parse("2024-03-01T00:00:00Z"), Instant.parse("2024-08-31T23:59:59Z"), false));

        Page<GisuInfo> pageResult = new PageImpl<>(
                gisuList,
                PageRequest.of(page, size),
                3L
        );

        given(getGisuUseCase.getList(any())).willReturn(pageResult);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/gisu")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
        );

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        queryParameters(
                                parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
                                parameterWithName("size").description("페이지 당 조회 수").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
                                fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                fieldWithPath("result.content").type(JsonFieldType.ARRAY).description("기수 목록"),
                                fieldWithPath("result.content[].gisuId").type(JsonFieldType.STRING).description("기수 ID"),
                                fieldWithPath("result.content[].generation").type(JsonFieldType.STRING).description("기수 번호"),
                                fieldWithPath("result.content[].startAt").type(JsonFieldType.STRING).description("기수 시작일시"),
                                fieldWithPath("result.content[].endAt").type(JsonFieldType.STRING).description("기수 종료일시"),
                                fieldWithPath("result.content[].isActive").type(JsonFieldType.BOOLEAN).description("현재 기수 여부"),
                                fieldWithPath("result.page").type(JsonFieldType.STRING).description("현재 페이지 번호 (0부터 시작)"),
                                fieldWithPath("result.size").type(JsonFieldType.STRING).description("페이지 당 조회 수"),
                                fieldWithPath("result.totalElements").type(JsonFieldType.STRING).description("전체 기수 수"),
                                fieldWithPath("result.totalPages").type(JsonFieldType.STRING).description("전체 페이지 수"),
                                fieldWithPath("result.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
                                fieldWithPath("result.hasPrevious").type(JsonFieldType.BOOLEAN).description("이전 페이지 존재 여부")
                        )
                ));
    }
}
