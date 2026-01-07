package com.umc.product.organization.application.in;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.organization.adapter.in.web.dto.request.CreateSchoolRequest;
import com.umc.product.support.DocumentationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class SchoolControllerTest extends DocumentationTest {


    @Test
    void 총괄_신규학교를_추가합니다() throws Exception {
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

}
