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

public class AdminSchoolControllerTest extends DocumentationTest {


    @Test
    void 총괄_신규학교를_추가한다() throws Exception {
        // given when
        CreateSchoolRequest request = CreateSchoolRequest.builder().schoolName("중앙대학교")
                .remark("중앙대는 멋집니다.").logoImageId("file-123")
                .kakaoLink("https://open.kakao.com/o/example")
                .instagramLink("https://instagram.com/example")
                .youtubeLink("https://youtube.com/@example").build();

        // then
        ResultActions result = mockMvc.perform(
                post("/api/v1/schools").content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk()).andDo(restDocsHandler.document(
                requestFields(fieldWithPath("schoolName").type(JsonFieldType.STRING).description("학교 이름"),
                        fieldWithPath("remark").type(JsonFieldType.STRING).description("비고"),
                        fieldWithPath("logoImageId").optional().type(JsonFieldType.STRING).description("로고 이미지 파일 ID"),
                        fieldWithPath("kakaoLink").optional().type(JsonFieldType.STRING).description("카카오톡 링크"),
                        fieldWithPath("instagramLink").optional().type(JsonFieldType.STRING).description("인스타그램 링크"),
                        fieldWithPath("youtubeLink").optional().type(JsonFieldType.STRING).description("유튜브 링크"))));

    }

    @Test
    void 총괄_학교정보를_수정한다() throws Exception {
        // given // when
        Long schoolId = 1L;

        CreateSchoolRequest request = CreateSchoolRequest.builder().schoolName("동국대학교")
                .remark("신승호 라면이 맛있습니다.").logoImageId("file-456").build();

        ResultActions result = mockMvc.perform(
                patch("/api/v1/schools/{schoolId}", schoolId).content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(pathParameters(parameterWithName("schoolId").description("학교 ID")),
                        requestFields(
                                fieldWithPath("schoolName").optional().type(JsonFieldType.STRING).description("학교 이름"),
                                fieldWithPath("remark").optional().type(JsonFieldType.STRING).description("비고"),
                                fieldWithPath("logoImageId").optional().type(JsonFieldType.STRING).description("로고 이미지 파일 ID"),
                                fieldWithPath("kakaoLink").optional().type(JsonFieldType.STRING).description("카카오톡 링크"),
                                fieldWithPath("instagramLink").optional().type(JsonFieldType.STRING).description("인스타그램 링크"),
                                fieldWithPath("youtubeLink").optional().type(JsonFieldType.STRING).description("유튜브 링크"))));

    }

    @Test
    void 총괄_학교를_일괄_삭제한다() throws Exception {
        // given
        DeleteSchoolsRequest request = new DeleteSchoolsRequest(List.of(1L, 2L, 3L));

        // when
        ResultActions result = mockMvc.perform(
                delete("/api/v1/schools").content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk()).andDo(restDocsHandler.document(
                requestFields(fieldWithPath("schoolIds").type(JsonFieldType.ARRAY).description("삭제할 학교 ID 목록"))));
    }

}
