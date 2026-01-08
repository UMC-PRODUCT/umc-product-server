package com.umc.product.organization.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.organization.adapter.in.web.dto.response.SchoolDetailResponse;
import com.umc.product.organization.application.port.in.query.dto.SchoolInfo;
import com.umc.product.support.DocumentationTest;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

class SchoolQueryControllerTest extends DocumentationTest {

    @Test
    void 학교_상세정보를_조회합니다() throws Exception {
        // give
        Long schoolId = 1L;
        LocalDate createdAt = LocalDate.of(2026, 1, 1);
        LocalDate updatedAt = LocalDate.of(2026, 1, 4);

        SchoolInfo schoolInfo = new SchoolInfo(3L, "Ain 지부", "중앙대학교", 1L, "비고", createdAt, updatedAt);
        SchoolDetailResponse response = SchoolDetailResponse.from(
                new SchoolInfo(3L, "Ain 지부", "중앙대학교", 1L, "비고", createdAt, updatedAt));

        given(getSchoolUseCase.getSchoolDetail(schoolId)).willReturn(schoolInfo);
        // when
        ResultActions result = mockMvc.perform(get("/api/v1/admin/schools/{schoolId}", schoolId));
        // then
        result.andExpect((status().isOk()))
                .andDo(restDocsHandler.document(pathParameters(parameterWithName("schoolId").description("학교 ID")),
                        responseFields(fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
                                fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                fieldWithPath("result.chapterId").type(JsonFieldType.STRING).description("지부 ID"),
                                fieldWithPath("result.chapterName").type(JsonFieldType.STRING).description("지부 이름"),
                                fieldWithPath("result.schoolName").type(JsonFieldType.STRING).description("학교 이름"),
                                fieldWithPath("result.schoolId").type(JsonFieldType.STRING).description("학교 ID"),
                                fieldWithPath("result.remark").type(JsonFieldType.STRING).description("비고"),
                                fieldWithPath("result.createdAt").type(JsonFieldType.STRING).description("생성일자"),
                                fieldWithPath("result.updatedAt").type(JsonFieldType.STRING).description("수정일자"))));
    }

}