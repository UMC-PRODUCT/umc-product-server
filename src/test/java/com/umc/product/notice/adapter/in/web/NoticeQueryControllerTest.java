package com.umc.product.notice.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.notice.application.port.in.query.dto.NoticeImageInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeLinkInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusResult;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusSummary;
import com.umc.product.notice.application.port.in.query.dto.NoticeSummary;
import com.umc.product.notice.application.port.in.query.dto.NoticeVoteInfo;
import com.umc.product.notice.dto.NoticeTargetInfo;
import com.umc.product.support.DocumentationTest;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class NoticeQueryControllerTest extends DocumentationTest {

    @Test
    void 공지사항_전체를_조회한다() throws Exception {
        // given
        NoticeTargetInfo targetInfo = new NoticeTargetInfo(1L, 2L, 3L, List.of(ChallengerPart.SPRINGBOOT));
        NoticeSummary summary = new NoticeSummary(
            1L, "공지 제목", "공지 내용", true, 42, Instant.now(),
            targetInfo, 10L, "닉네임", "홍길동"
        );
        Page<NoticeSummary> page = new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1);

        given(getNoticeUseCase.getAllNoticeSummaries(any(), any())).willReturn(page);

        // when
        ResultActions result = mockMvc.perform(
            get("/api/v1/notices")
                .param("gisuId", "1")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "createdAt,desc"));

        // then
        result.andExpect(status().isOk())
            .andDo(restDocsHandler.document(
                queryParameters(
                    parameterWithName("gisuId").description("기수 ID (필수)"),
                    parameterWithName("chapterId").optional().description("지부 ID"),
                    parameterWithName("schoolId").optional().description("학교 ID"),
                    parameterWithName("part").optional().description("파트 (PLAN, DESIGN, WEB, IOS, ANDROID, SPRINGBOOT, NODEJS)"),
                    parameterWithName("page").optional().description("페이지 번호 (기본값: 0)"),
                    parameterWithName("size").optional().description("페이지 크기 (기본값: 10)"),
                    parameterWithName("sort").optional().description("정렬 기준 (기본값: createdAt,desc)")
                ),
                responseFields(
                    fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                    fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                    fieldWithPath("result.content[].id").type(JsonFieldType.STRING).description("공지사항 ID"),
                    fieldWithPath("result.content[].title").type(JsonFieldType.STRING).description("공지 제목"),
                    fieldWithPath("result.content[].content").type(JsonFieldType.STRING).description("공지 내용"),
                    fieldWithPath("result.content[].shouldSendNotification").type(JsonFieldType.BOOLEAN).description("알림 발송 여부"),
                    fieldWithPath("result.content[].viewCount").type(JsonFieldType.STRING).description("조회수"),
                    fieldWithPath("result.content[].createdAt").type(JsonFieldType.STRING).description("생성 일시"),
                    fieldWithPath("result.content[].targetInfo").type(JsonFieldType.OBJECT).description("공지 대상 정보"),
                    fieldWithPath("result.content[].targetInfo.targetGisuId").optional().type(JsonFieldType.STRING).description("대상 기수 ID"),
                    fieldWithPath("result.content[].targetInfo.targetChapterId").optional().type(JsonFieldType.STRING).description("대상 지부 ID"),
                    fieldWithPath("result.content[].targetInfo.targetSchoolId").optional().type(JsonFieldType.STRING).description("대상 학교 ID"),
                    fieldWithPath("result.content[].targetInfo.targetParts").optional().type(JsonFieldType.ARRAY).description("대상 파트 목록"),
                    fieldWithPath("result.content[].authorChallengerId").type(JsonFieldType.STRING).description("작성자 챌린저 ID"),
                    fieldWithPath("result.content[].authorNickname").type(JsonFieldType.STRING).description("작성자 닉네임"),
                    fieldWithPath("result.content[].authorName").type(JsonFieldType.STRING).description("작성자 이름"),
                    fieldWithPath("result.page").type(JsonFieldType.STRING).description("현재 페이지 번호"),
                    fieldWithPath("result.size").type(JsonFieldType.STRING).description("페이지 크기"),
                    fieldWithPath("result.totalElements").type(JsonFieldType.STRING).description("전체 요소 수"),
                    fieldWithPath("result.totalPages").type(JsonFieldType.STRING).description("전체 페이지 수"),
                    fieldWithPath("result.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
                    fieldWithPath("result.hasPrevious").type(JsonFieldType.BOOLEAN).description("이전 페이지 존재 여부")
                )
            ));
    }

    @Test
    void 검색어로_공지사항을_조회한다() throws Exception {
        // given
        NoticeTargetInfo targetInfo = new NoticeTargetInfo(1L, null, null, List.of());
        NoticeSummary summary = new NoticeSummary(
            1L, "검색된 공지", "검색 결과 내용", false, 10, Instant.now(),
            targetInfo, 5L, "닉네임", "김철수"
        );
        Page<NoticeSummary> page = new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1);

        given(getNoticeUseCase.searchNoticesByKeyword(any(), any(), any())).willReturn(page);

        // when
        ResultActions result = mockMvc.perform(
            get("/api/v1/notices/search")
                .param("keyword", "검색어")
                .param("gisuId", "1")
                .param("page", "0")
                .param("size", "10"));

        // then
        result.andExpect(status().isOk())
            .andDo(restDocsHandler.document(
                queryParameters(
                    parameterWithName("keyword").description("검색 키워드"),
                    parameterWithName("gisuId").description("기수 ID (필수)"),
                    parameterWithName("chapterId").optional().description("지부 ID"),
                    parameterWithName("schoolId").optional().description("학교 ID"),
                    parameterWithName("part").optional().description("파트"),
                    parameterWithName("page").optional().description("페이지 번호 (기본값: 0)"),
                    parameterWithName("size").optional().description("페이지 크기 (기본값: 10)"),
                    parameterWithName("sort").optional().description("정렬 기준 (기본값: createdAt,desc)")
                ),
                responseFields(
                    fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                    fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                    fieldWithPath("result.content[].id").type(JsonFieldType.STRING).description("공지사항 ID"),
                    fieldWithPath("result.content[].title").type(JsonFieldType.STRING).description("공지 제목"),
                    fieldWithPath("result.content[].content").type(JsonFieldType.STRING).description("공지 내용"),
                    fieldWithPath("result.content[].shouldSendNotification").type(JsonFieldType.BOOLEAN).description("알림 발송 여부"),
                    fieldWithPath("result.content[].viewCount").type(JsonFieldType.STRING).description("조회수"),
                    fieldWithPath("result.content[].createdAt").type(JsonFieldType.STRING).description("생성 일시"),
                    fieldWithPath("result.content[].targetInfo").type(JsonFieldType.OBJECT).description("공지 대상 정보"),
                    fieldWithPath("result.content[].targetInfo.targetGisuId").optional().type(JsonFieldType.STRING).description("대상 기수 ID"),
                    fieldWithPath("result.content[].targetInfo.targetChapterId").optional().type(JsonFieldType.NULL).description("대상 지부 ID"),
                    fieldWithPath("result.content[].targetInfo.targetSchoolId").optional().type(JsonFieldType.NULL).description("대상 학교 ID"),
                    fieldWithPath("result.content[].targetInfo.targetParts").optional().type(JsonFieldType.ARRAY).description("대상 파트 목록"),
                    fieldWithPath("result.content[].authorChallengerId").type(JsonFieldType.STRING).description("작성자 챌린저 ID"),
                    fieldWithPath("result.content[].authorNickname").type(JsonFieldType.STRING).description("작성자 닉네임"),
                    fieldWithPath("result.content[].authorName").type(JsonFieldType.STRING).description("작성자 이름"),
                    fieldWithPath("result.page").type(JsonFieldType.STRING).description("현재 페이지 번호"),
                    fieldWithPath("result.size").type(JsonFieldType.STRING).description("페이지 크기"),
                    fieldWithPath("result.totalElements").type(JsonFieldType.STRING).description("전체 요소 수"),
                    fieldWithPath("result.totalPages").type(JsonFieldType.STRING).description("전체 페이지 수"),
                    fieldWithPath("result.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
                    fieldWithPath("result.hasPrevious").type(JsonFieldType.BOOLEAN).description("이전 페이지 존재 여부")
                )
            ));
    }

    @Test
    void 공지사항을_상세_조회한다() throws Exception {
        // given
        Long noticeId = 1L;
        NoticeTargetInfo targetInfo = new NoticeTargetInfo(1L, 2L, 3L, List.of(ChallengerPart.WEB));
        NoticeInfo noticeInfo = new NoticeInfo(
            1L, "공지 제목", "공지 상세 내용", 10L,
            List.of(new NoticeVoteInfo(1L, 100L)),
            List.of(new NoticeImageInfo(1L, "https://example.com/image.png", 1)),
            List.of(new NoticeLinkInfo(1L, "https://example.com", 1)),
            targetInfo, 42, Instant.now()
        );

        given(getNoticeUseCase.getNoticeDetail(noticeId)).willReturn(noticeInfo);

        // when
        ResultActions result = mockMvc.perform(
            get("/api/v1/notices/{noticeId}", noticeId));

        // then
        result.andExpect(status().isOk())
            .andDo(restDocsHandler.document(
                pathParameters(
                    parameterWithName("noticeId").description("공지사항 ID")
                ),
                responseFields(
                    fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                    fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                    fieldWithPath("result.id").type(JsonFieldType.STRING).description("공지사항 ID"),
                    fieldWithPath("result.title").type(JsonFieldType.STRING).description("공지 제목"),
                    fieldWithPath("result.content").type(JsonFieldType.STRING).description("공지 내용"),
                    fieldWithPath("result.authorChallengerId").type(JsonFieldType.STRING).description("작성자 챌린저 ID"),
                    fieldWithPath("result.votes").type(JsonFieldType.ARRAY).description("투표 목록"),
                    fieldWithPath("result.votes[].noticeVoteId").type(JsonFieldType.STRING).description("공지-투표 연결 ID"),
                    fieldWithPath("result.votes[].voteId").type(JsonFieldType.STRING).description("투표 ID"),
                    fieldWithPath("result.votes[].displayOrder").type(JsonFieldType.STRING).description("표시 순서"),
                    fieldWithPath("result.images").type(JsonFieldType.ARRAY).description("이미지 목록"),
                    fieldWithPath("result.images[].id").type(JsonFieldType.STRING).description("이미지 ID"),
                    fieldWithPath("result.images[].url").type(JsonFieldType.STRING).description("이미지 URL"),
                    fieldWithPath("result.images[].displayOrder").type(JsonFieldType.STRING).description("표시 순서"),
                    fieldWithPath("result.links").type(JsonFieldType.ARRAY).description("링크 목록"),
                    fieldWithPath("result.links[].id").type(JsonFieldType.STRING).description("링크 ID"),
                    fieldWithPath("result.links[].url").type(JsonFieldType.STRING).description("링크 URL"),
                    fieldWithPath("result.links[].displayOrder").type(JsonFieldType.STRING).description("표시 순서"),
                    fieldWithPath("result.targetInfo").type(JsonFieldType.OBJECT).description("공지 대상 정보"),
                    fieldWithPath("result.targetInfo.targetGisuId").optional().type(JsonFieldType.STRING).description("대상 기수 ID"),
                    fieldWithPath("result.targetInfo.targetChapterId").optional().type(JsonFieldType.STRING).description("대상 지부 ID"),
                    fieldWithPath("result.targetInfo.targetSchoolId").optional().type(JsonFieldType.STRING).description("대상 학교 ID"),
                    fieldWithPath("result.targetInfo.targetParts").optional().type(JsonFieldType.ARRAY).description("대상 파트 목록"),
                    fieldWithPath("result.viewCount").type(JsonFieldType.STRING).description("조회수"),
                    fieldWithPath("result.createdAt").type(JsonFieldType.STRING).description("생성 일시")
                )
            ));
    }

    @Test
    void 공지사항_수신_현황_통계를_조회한다() throws Exception {
        // given
        Long noticeId = 1L;
        NoticeReadStatusSummary summary = new NoticeReadStatusSummary(100, 75, 25, 0.75f);

        given(getNoticeUseCase.getReadStatistics(noticeId)).willReturn(summary);

        // when
        ResultActions result = mockMvc.perform(
            get("/api/v1/notices/{noticeId}/read-statics", noticeId));

        // then
        result.andExpect(status().isOk())
            .andDo(restDocsHandler.document(
                pathParameters(
                    parameterWithName("noticeId").description("공지사항 ID")
                ),
                responseFields(
                    fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                    fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                    fieldWithPath("result.totalCount").type(JsonFieldType.STRING).description("전체 대상자 수"),
                    fieldWithPath("result.readCount").type(JsonFieldType.STRING).description("읽은 사람 수"),
                    fieldWithPath("result.unreadCount").type(JsonFieldType.STRING).description("읽지 않은 사람 수"),
                    fieldWithPath("result.readRate").type(JsonFieldType.STRING).description("읽음 비율")
                )
            ));
    }

    @Test
    void 공지사항_수신_현황을_조회한다() throws Exception {
        // given
        Long noticeId = 1L;
        NoticeReadStatusInfo statusInfo = new NoticeReadStatusInfo(
            1L, "홍길동", "https://example.com/profile.png", ChallengerPart.SPRINGBOOT,
            10L, "중앙대학교", 5L, "서울지부"
        );
        NoticeReadStatusResult statusResult = new NoticeReadStatusResult(List.of(statusInfo), 1L, false);

        given(getNoticeUseCase.getReadStatus(any())).willReturn(statusResult);

        // when
        ResultActions result = mockMvc.perform(
            get("/api/v1/notices/{noticeId}/read-status", noticeId)
                .param("filterType", "ALL")
                .param("status", "READ"));

        // then
        result.andExpect(status().isOk())
            .andDo(restDocsHandler.document(
                pathParameters(
                    parameterWithName("noticeId").description("공지사항 ID")
                ),
                queryParameters(
                    parameterWithName("cursorId").optional().description("커서 ID (읽은 경우: NoticeReadId, 읽지 않은 경우: ChallengerId)"),
                    parameterWithName("filterType").description("필터 타입 (ALL, CHAPTER, SCHOOL)"),
                    parameterWithName("organizationIds").optional().description("필터 대상 조직 ID 목록"),
                    parameterWithName("status").description("읽음 상태 (READ, UNREAD)")
                ),
                responseFields(
                    fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                    fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                    fieldWithPath("result.content[].challengerId").type(JsonFieldType.STRING).description("챌린저 ID"),
                    fieldWithPath("result.content[].name").type(JsonFieldType.STRING).description("챌린저 이름"),
                    fieldWithPath("result.content[].profileImageUrl").type(JsonFieldType.STRING).description("프로필 이미지 URL"),
                    fieldWithPath("result.content[].part").type(JsonFieldType.STRING).description("파트"),
                    fieldWithPath("result.content[].schoolId").type(JsonFieldType.STRING).description("학교 ID"),
                    fieldWithPath("result.content[].schoolName").type(JsonFieldType.STRING).description("학교 이름"),
                    fieldWithPath("result.content[].chapterId").type(JsonFieldType.STRING).description("지부 ID"),
                    fieldWithPath("result.content[].chapterName").type(JsonFieldType.STRING).description("지부 이름"),
                    fieldWithPath("result.nextCursor").type(JsonFieldType.STRING).description("다음 커서 ID"),
                    fieldWithPath("result.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부")
                )
            ));
    }
}
