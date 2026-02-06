package com.umc.product.organization.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.organization.application.port.in.query.dto.SchoolLinkInfo;
import com.umc.product.support.DocumentationTest;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

class SchoolLinkQueryControllerTest extends DocumentationTest {

    @Test
    void 학교_링크를_조회합니다() throws Exception {
        // given
        Long schoolId = 1L;
        SchoolLinkInfo schoolLinkInfo = new SchoolLinkInfo(
                "https://pf.kakao.com/_example",
                "https://instagram.com/umc",
                "https://youtube.com/@umc"
        );

        given(getSchoolUseCase.getSchoolLink(schoolId)).willReturn(schoolLinkInfo);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/schools/link/{schoolId}", schoolId));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        pathParameters(
                                parameterWithName("schoolId").description("학교 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
                                fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                fieldWithPath("result.kakaoLink").type(JsonFieldType.STRING).description("카카오 링크").optional(),
                                fieldWithPath("result.instagramLink").type(JsonFieldType.STRING).description("인스타그램 링크").optional(),
                                fieldWithPath("result.youtubeLink").type(JsonFieldType.STRING).description("유튜브 링크").optional()
                        )
                ));
    }
}

