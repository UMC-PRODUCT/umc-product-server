package com.umc.product.notice.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.notice.adapter.in.web.dto.request.AddNoticeImagesRequest;
import com.umc.product.notice.adapter.in.web.dto.request.AddNoticeLinksRequest;
import com.umc.product.notice.adapter.in.web.dto.request.AddNoticeVotesRequest;
import com.umc.product.notice.adapter.in.web.dto.request.ReplaceNoticeImagesRequest;
import com.umc.product.notice.adapter.in.web.dto.request.ReplaceNoticeLinksRequest;
import com.umc.product.notice.adapter.in.web.dto.request.ReplaceNoticeVotesRequest;
import com.umc.product.support.DocumentationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class NoticeContentControllerTest extends DocumentationTest {

    @Test
    void 공지사항에_이미지를_추가한다() throws Exception {
        // given
        Long noticeId = 1L;
        AddNoticeImagesRequest request = new AddNoticeImagesRequest(List.of("file-123", "file-456"));

        given(manageNoticeContentUseCase.addImages(any(), eq(noticeId))).willReturn(List.of(1L, 2L));

        // when
        ResultActions result = mockMvc.perform(
            post("/api/v1/notices/{noticeId}/images", noticeId)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
            .andDo(restDocsHandler.document(
                pathParameters(
                    parameterWithName("noticeId").description("공지사항 ID")
                ),
                requestFields(
                    fieldWithPath("imageIds").type(JsonFieldType.ARRAY).description("추가할 이미지 파일 ID 목록")
                ),
                responseFields(
                    fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                    fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                    fieldWithPath("result.imageIds").type(JsonFieldType.ARRAY).description("생성된 공지 이미지 ID 목록")
                )
            ));
    }

    @Test
    void 공지사항에_링크를_추가한다() throws Exception {
        // given
        Long noticeId = 1L;
        AddNoticeLinksRequest request = new AddNoticeLinksRequest(List.of("https://example.com", "https://umc.com"));

        given(manageNoticeContentUseCase.addLinks(any(), eq(noticeId))).willReturn(List.of(1L, 2L));

        // when
        ResultActions result = mockMvc.perform(
            post("/api/v1/notices/{noticeId}/links", noticeId)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
            .andDo(restDocsHandler.document(
                pathParameters(
                    parameterWithName("noticeId").description("공지사항 ID")
                ),
                requestFields(
                    fieldWithPath("links").type(JsonFieldType.ARRAY).description("추가할 링크 URL 목록")
                ),
                responseFields(
                    fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                    fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                    fieldWithPath("result.linkIds").type(JsonFieldType.ARRAY).description("생성된 공지 링크 ID 목록")
                )
            ));
    }

    @Test
    void 공지사항에_투표를_추가한다() throws Exception {
        // given
        Long noticeId = 1L;
        AddNoticeVotesRequest request = new AddNoticeVotesRequest(List.of(100L, 200L));

        given(manageNoticeContentUseCase.addVotes(any(), eq(noticeId))).willReturn(List.of(1L, 2L));

        // when
        ResultActions result = mockMvc.perform(
            post("/api/v1/notices/{noticeId}/votes", noticeId)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
            .andDo(restDocsHandler.document(
                pathParameters(
                    parameterWithName("noticeId").description("공지사항 ID")
                ),
                requestFields(
                    fieldWithPath("voteIds").type(JsonFieldType.ARRAY).description("추가할 투표 ID 목록")
                ),
                responseFields(
                    fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                    fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                    fieldWithPath("result.voteIds").type(JsonFieldType.ARRAY).description("생성된 공지 투표 ID 목록")
                )
            ));
    }

    @Test
    void 공지사항_이미지를_전체_수정한다() throws Exception {
        // given
        Long noticeId = 1L;
        ReplaceNoticeImagesRequest request = new ReplaceNoticeImagesRequest(List.of("file-789", "file-012"));

        // when
        ResultActions result = mockMvc.perform(
            patch("/api/v1/notices/{noticeId}/images", noticeId)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
            .andDo(restDocsHandler.document(
                pathParameters(
                    parameterWithName("noticeId").description("공지사항 ID")
                ),
                requestFields(
                    fieldWithPath("imageIds").type(JsonFieldType.ARRAY).description("교체할 이미지 파일 ID 목록")
                ),
                responseFields(
                    fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                    fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                )
            ));
    }

    @Test
    void 공지사항_링크를_전체_수정한다() throws Exception {
        // given
        Long noticeId = 1L;
        ReplaceNoticeLinksRequest request = new ReplaceNoticeLinksRequest(List.of("https://new-link.com"));

        // when
        ResultActions result = mockMvc.perform(
            patch("/api/v1/notices/{noticeId}/links", noticeId)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
            .andDo(restDocsHandler.document(
                pathParameters(
                    parameterWithName("noticeId").description("공지사항 ID")
                ),
                requestFields(
                    fieldWithPath("links").type(JsonFieldType.ARRAY).description("교체할 링크 URL 목록")
                ),
                responseFields(
                    fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                    fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                )
            ));
    }

    @Test
    void 공지사항_투표를_전체_수정한다() throws Exception {
        // given
        Long noticeId = 1L;
        ReplaceNoticeVotesRequest request = new ReplaceNoticeVotesRequest(List.of(300L, 400L));

        // when
        ResultActions result = mockMvc.perform(
            patch("/api/v1/notices/{noticeId}/votes", noticeId)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
            .andDo(restDocsHandler.document(
                pathParameters(
                    parameterWithName("noticeId").description("공지사항 ID")
                ),
                requestFields(
                    fieldWithPath("voteIds").type(JsonFieldType.ARRAY).description("교체할 투표 ID 목록")
                ),
                responseFields(
                    fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                    fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                )
            ));
    }
}
