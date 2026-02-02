package com.umc.product.notice.application.port.in.query.dto;


import com.umc.product.notice.dto.NoticeTargetInfo;
import java.time.Instant;
import java.util.List;

/**
 * 공지사항에 대한 상세 정보를 제공하는 DTO
 */
public record NoticeInfo(
    Long id,
    String title,
    String content,
    Long authorChallengerId,
    List<NoticeVoteInfo> votes,
    List<NoticeImageInfo> images,
    List<NoticeLinkInfo> links,
    // 수신 대상
    NoticeTargetInfo targetInfo,

    // 메타데이터
    Integer viewCount,
    Instant createdAt
) {
}
