package com.umc.product.notice.adapter.in.web.dto.response.query;

import com.umc.product.notice.application.port.in.query.dto.NoticeImageInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeLinkInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeVoteInfo;
import com.umc.product.notice.dto.NoticeTargetInfo;
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
    Long authorChallengerId, // TODO: 작성자에 대한 정보를 바로 주는 것이 나아보임

    // 공지사항 부가 내용들
    NoticeVoteInfo vote,
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
            .authorChallengerId(noticeInfo.authorChallengerId())
            .vote(noticeInfo.vote())
            .images(noticeInfo.images())
            .links(noticeInfo.links())
            .targetInfo(noticeInfo.targetInfo())
            .viewCount(noticeInfo.viewCount())
            .createdAt(noticeInfo.createdAt())
            .build();
    }
}
