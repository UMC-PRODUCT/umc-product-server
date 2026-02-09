package com.umc.product.notice.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.notice.adapter.in.web.dto.request.CreateNoticeRequest;
import com.umc.product.notice.adapter.in.web.dto.request.SendNoticeReminderRequest;
import com.umc.product.notice.adapter.in.web.dto.request.UpdateNoticeRequest;
import com.umc.product.notice.dto.NoticeTargetInfo;
import com.umc.product.support.DocumentationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class NoticeControllerTest extends DocumentationTest {

    @Test
    void 공지사항을_생성한다() throws Exception {
        // given
        NoticeTargetInfo targetInfo = new NoticeTargetInfo(1L, 2L, 3L, List.of(ChallengerPart.SPRINGBOOT, ChallengerPart.WEB));
        CreateNoticeRequest request = new CreateNoticeRequest("공지 제목", "공지 내용입니다.", true, targetInfo);

        given(manageNoticeUseCase.createNotice(any())).willReturn(1L);

        // when
        ResultActions result = mockMvc.perform(
            post("/api/v1/notices")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
            .andDo(restDocsHandler.document(
                requestFields(
                    fieldWithPath("title").type(JsonFieldType.STRING).description("공지 제목"),
                    fieldWithPath("content").type(JsonFieldType.STRING).description("공지 내용"),
                    fieldWithPath("shouldNotify").type(JsonFieldType.BOOLEAN).description("알림 발송 여부"),
                    fieldWithPath("targetInfo").type(JsonFieldType.OBJECT).description("공지 대상 정보"),
                    fieldWithPath("targetInfo.targetGisuId").optional().type(JsonFieldType.STRING).description("대상 기수 ID"),
                    fieldWithPath("targetInfo.targetChapterId").optional().type(JsonFieldType.STRING).description("대상 지부 ID"),
                    fieldWithPath("targetInfo.targetSchoolId").optional().type(JsonFieldType.STRING).description("대상 학교 ID"),
                    fieldWithPath("targetInfo.targetParts").optional().type(JsonFieldType.ARRAY).description("대상 파트 목록")
                ),
                responseFields(
                    fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                    fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                    fieldWithPath("result.noticeId").type(JsonFieldType.STRING).description("생성된 공지사항 ID")
                )
            ));
    }

    @Test
    void 공지사항을_삭제한다() throws Exception {
        // given
        Long noticeId = 1L;

        // when
        ResultActions result = mockMvc.perform(
            delete("/api/v1/notices/{noticeId}", noticeId));

        // then
        result.andExpect(status().isOk())
            .andDo(restDocsHandler.document(
                pathParameters(
                    parameterWithName("noticeId").description("삭제할 공지사항 ID")
                )
            ));
    }

    @Test
    void 공지사항을_수정한다() throws Exception {
        // given
        Long noticeId = 1L;
        UpdateNoticeRequest request = new UpdateNoticeRequest("수정된 제목", "수정된 내용");

        // when
        ResultActions result = mockMvc.perform(
            patch("/api/v1/notices/{noticeId}", noticeId)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
            .andDo(restDocsHandler.document(
                pathParameters(
                    parameterWithName("noticeId").description("수정할 공지사항 ID")
                ),
                requestFields(
                    fieldWithPath("title").type(JsonFieldType.STRING).description("수정할 공지 제목"),
                    fieldWithPath("content").type(JsonFieldType.STRING).description("수정할 공지 내용")
                )
            ));
    }

    @Test
    void 공지사항_리마인드_알림을_보낸다() throws Exception {
        // given
        Long noticeId = 1L;
        SendNoticeReminderRequest request = new SendNoticeReminderRequest(List.of(1L, 2L, 3L));

        // when
        ResultActions result = mockMvc.perform(
            post("/api/v1/notices/{noticeId}/reminders", noticeId)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
            .andDo(restDocsHandler.document(
                pathParameters(
                    parameterWithName("noticeId").description("리마인드할 공지사항 ID")
                ),
                requestFields(
                    fieldWithPath("targetIds").type(JsonFieldType.ARRAY).description("리마인드 대상 ID 목록")
                )
            ));
    }

    @Test
    void 공지사항을_읽음_처리한다() throws Exception {
        // given
        Long noticeId = 1L;

        // when
        ResultActions result = mockMvc.perform(
            post("/api/v1/notices/{noticeId}/read", noticeId));

        // then
        result.andExpect(status().isOk())
            .andDo(restDocsHandler.document(
                pathParameters(
                    parameterWithName("noticeId").description("읽음 처리할 공지사항 ID")
                ),
                responseFields(
                    fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                    fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                )
            ));
    }
}
