package com.umc.product.notice.application.port.in.query.dto;


import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.enums.NoticeClassification;
import java.time.Instant;
import java.util.List;

public record NoticeInfo(
        Long id,
        String title,
        String content,
        ChallengerRoleType authorRole,
        Long authorChallengerId,
        NoticeClassification scope,
        String scopeDisplayName,
        List<Long> organizationIds,
        Long targetGisuId,
        List<ChallengerRoleType> targetRoleTypes,
        List<ChallengerPart> targetParts,
        List<NoticeVoteInfo> votes,
        List<NoticeImageInfo> images,
        List<NoticeLinkInfo> links,
        Integer viewCount,
        Instant createdAt,
        boolean isUpdated
) {
    public static NoticeInfo of(
            Notice notice,
            ChallengerRoleType authorRoleType,
            String scopeDisplayName,
            Integer viewCount,
            List<NoticeVoteInfo> votes,
            List<NoticeImageInfo> images,
            List<NoticeLinkInfo> links,
            boolean isUpdated
    ) {
        return new NoticeInfo(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                authorRoleType,
                notice.getAuthorChallengerId(),
                notice.getScope(),
                scopeDisplayName,
                notice.getOrganizationIds(),
                notice.getTargetGisuId(),
                notice.getTargetRoles(),
                notice.getTargetParts(),
                votes,
                images,
                links,
                viewCount,
                notice.getCreatedAt(),
                isUpdated
        );
    }
}
