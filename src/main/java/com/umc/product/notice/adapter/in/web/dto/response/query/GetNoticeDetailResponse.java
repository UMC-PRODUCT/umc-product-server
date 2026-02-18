package com.umc.product.notice.adapter.in.web.dto.response.query;

import com.umc.product.notice.application.port.in.query.dto.NoticeImageInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeLinkInfo;
import com.umc.product.notice.dto.NoticeTargetInfo;
import com.umc.product.survey.application.port.in.query.dto.VoteInfo;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

/**
 * 공지사항 상세 조회 응답
 */
@Builder
public record GetNoticeDetailResponse(
    Long id,
    String title,
    String content,
    Long authorChallengerId,
    Long authorMemberId,

    // 공지사항 부가 내용들
    VoteInfo vote,
    List<NoticeImageInfo> images,
    List<NoticeLinkInfo> links,

    // 수신 대상
    NoticeTargetInfo targetInfo,

    // 메타데이터
    Integer viewCount,
    Instant createdAt
) {
    public static GetNoticeDetailResponse from(
        NoticeInfo noticeInfo
    ) {
        return GetNoticeDetailResponse.builder()
            .id(noticeInfo.id())
            .title(noticeInfo.title())
            .content(noticeInfo.content())
            .authorChallengerId(null)
            .authorMemberId(noticeInfo.authorMemberId())
            .vote(noticeInfo.vote())
            .images(noticeInfo.images())
            .links(noticeInfo.links())
            .targetInfo(noticeInfo.targetInfo())
            .viewCount(noticeInfo.viewCount())
            .createdAt(noticeInfo.createdAt())
            .build();
    }
}
