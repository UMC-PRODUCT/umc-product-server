package com.umc.product.organization.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.organization.application.port.in.query.dto.SchoolLinkInfo;
import com.umc.product.organization.domain.SchoolLinkType;
import com.umc.product.support.DocumentationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

class SchoolLinkQueryControllerTest extends DocumentationTest {

    @Test
    void 학교_링크를_조회합니다() throws Exception {
        // given
        Long schoolId = 1L;
        SchoolLinkInfo schoolLinkInfo = new SchoolLinkInfo(
                List.of(
                        new SchoolLinkInfo.SchoolLinkItem("UMC 카카오톡", SchoolLinkType.KAKAO, "https://pf.kakao.com/_example"),
                        new SchoolLinkInfo.SchoolLinkItem("UMC 인스타그램", SchoolLinkType.INSTAGRAM, "https://instagram.com/umc"),
                        new SchoolLinkInfo.SchoolLinkItem("UMC 유튜브", SchoolLinkType.YOUTUBE, "https://youtube.com/@umc")
                )
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
                                fieldWithPath("result.links").type(JsonFieldType.ARRAY).description("학교 링크 목록"),
                                fieldWithPath("result.links[].title").type(JsonFieldType.STRING).description("링크 제목"),
                                fieldWithPath("result.links[].type").type(JsonFieldType.STRING).description("링크 타입 (KAKAO, INSTAGRAM, YOUTUBE)"),
                                fieldWithPath("result.links[].url").type(JsonFieldType.STRING).description("링크 URL")
                        )
                ));
    }
}
