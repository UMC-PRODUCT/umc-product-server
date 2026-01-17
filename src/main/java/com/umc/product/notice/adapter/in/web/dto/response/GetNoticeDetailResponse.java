package com.umc.product.notice.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.notice.application.port.in.query.dto.NoticeImageInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeLinkInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeVoteInfo;
import com.umc.product.notice.domain.enums.NoticeClassification;
import java.time.Instant;
import java.util.List;

public record GetNoticeDetailResponse(
        Long id,
        String title,
        String content,
        ChallengerRoleType authorRole,
        Long authorChallengerId,
        NoticeClassification scope,
        String scopeDisplayName,
        Long organizationId,
        Long targetGisuId,
        List<ChallengerRoleType> targetRoleTypes,
        List<ChallengerPart> targetParts,
        List<NoticeVoteInfo> votes,
        List<NoticeImageInfo> images,
        List<NoticeLinkInfo> links,
        Integer viewCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static GetNoticeDetailResponse from(NoticeInfo noticeInfo) {
        return new GetNoticeDetailResponse(
                noticeInfo.id(),
                noticeInfo.title(),
                noticeInfo.content(),
                noticeInfo.authorRole(),
                noticeInfo.authorChallengerId(),
                noticeInfo.scope(),
                noticeInfo.scopeDisplayName(),
                noticeInfo.organizationId(),
                noticeInfo.targetGisuId(),
                noticeInfo.targetRoleTypes(),
                noticeInfo.targetParts(),
                noticeInfo.votes(),
                noticeInfo.images(),
                noticeInfo.links(),
                noticeInfo.viewCount(),
                noticeInfo.createdAt(),
                noticeInfo.updatedAt()
        );
    }
}
