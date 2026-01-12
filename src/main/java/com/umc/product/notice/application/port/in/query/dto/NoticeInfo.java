package com.umc.product.notice.application.port.in.query.dto;

import com.umc.product.challenger.domain.enums.ChallengerPart;
import com.umc.product.challenger.domain.enums.OrganizationType;
import com.umc.product.challenger.domain.enums.RoleType;
import com.umc.product.notice.domain.Notice;
import java.time.Instant;
import java.util.List;

public record NoticeInfo(
        Long id,
        String title,
        String content,
        RoleType authorRole,
        Long authorChallengerId,
        OrganizationType scope,
        String scopeDisplayName,
        Long organizationId,
        Long targetGisuId,
        List<RoleType> targetRoles,
        List<ChallengerPart> targetParts,
        List<NoticeVoteInfo> votes,
        List<NoticeImageInfo> images,
        List<NoticeLinkInfo> links,
        Integer viewCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static NoticeInfo of(
            Notice notice,
            RoleType authorRole,
            String scopeDisplayName,
            Integer viewCount,
            List<NoticeVoteInfo> votes,
            List<NoticeImageInfo> images,
            List<NoticeLinkInfo> links
    ) {
        return new NoticeInfo(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                authorRole,
                notice.getAuthorChallengerId(),
                notice.getScope(),
                scopeDisplayName,
                notice.getOrganizationId(),
                notice.getTargetGisuId(),
                notice.getTargetRoles(),
                notice.getTargetParts(),
                votes,
                images,
                links,
                viewCount,
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}
