package com.umc.product.organization.adapter.in.web;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.organization.adapter.in.web.dto.request.CreateSchoolRequest;
import com.umc.product.organization.adapter.in.web.dto.request.DeleteSchoolsRequest;
import com.umc.product.support.DocumentationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class SchoolControllerTest extends DocumentationTest {


    @Test
    void 총괄_신규학교를_추가한다() throws Exception {
        // given when
        CreateSchoolRequest request = CreateSchoolRequest.builder().schoolName("중앙대학교").chapterId("3")
                .remark("중앙대는 멋집니다.").build();

        // then
        ResultActions result = mockMvc.perform(
                post("/api/v1/admin/schools").content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk()).andDo(restDocsHandler.document(
                requestFields(fieldWithPath("schoolName").type(JsonFieldType.STRING).description("학교 이름"),
                        fieldWithPath("chapterId").type(JsonFieldType.STRING).description("소속 지부 ID"),
                        fieldWithPath("remark").type(JsonFieldType.STRING).description("비고"))));

    }

    @Test
    void 총괄_학교정보를_수정한다() throws Exception {
        // given // when
        Long schoolId = 1L;

        CreateSchoolRequest request = CreateSchoolRequest.builder().schoolName("동국대학교").chapterId("3")
                .remark("신승호 라면이 맛있습니다.").build();

        ResultActions result = mockMvc.perform(
                patch("/api/v1/admin/schools/{schoolId}", schoolId).content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(pathParameters(parameterWithName("schoolId").description("학교 ID")),
                        requestFields(fieldWithPath("schoolName").type(JsonFieldType.STRING).description("학교 이름"),
                                fieldWithPath("chapterId").type(JsonFieldType.STRING).description("소속 지부 ID"),
                                fieldWithPath("remark").type(JsonFieldType.STRING).description("비고"))));

    }

    @Test
    void 총괄_학교를_제거한다() throws Exception {
        // given // when
        Long schoolId = 1L;

        ResultActions result = mockMvc.perform(delete("/api/v1/admin/schools/{schoolId}", schoolId));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(pathParameters(parameterWithName("schoolId").description("학교 ID")

                )));

    }

    @Test
    void 총괄_학교를_일괄_삭제한다() throws Exception {
        // given
        DeleteSchoolsRequest request = new DeleteSchoolsRequest(List.of(1L, 2L, 3L));

        // when
        ResultActions result = mockMvc.perform(
                delete("/api/v1/admin/schools")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        requestFields(
                                fieldWithPath("schoolIds").type(JsonFieldType.ARRAY).description("삭제할 학교 ID 목록")
                        )
                ));
    }

}
