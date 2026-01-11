package com.umc.product.notice.application.port.in.query;

import com.umc.product.challenger.domain.enums.ChallengerPart;
import com.umc.product.challenger.domain.enums.OrganizationType;
import com.umc.product.challenger.domain.enums.RoleType;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.enums.NoticeContentType;
import java.time.Instant;
import java.util.List;

public record NoticeSummary(
        Long id,
        String title,
        String content,
        RoleType authorRole, // 작성자 role
        OrganizationType scope,  // 조직 타입 (전체, 중앙운영사무국 등)
        Boolean read, // 읽음 여부 (빨간 점 표시용)
        List<NoticeContentType> includeContentTypes,
        Integer viewCount,
        Instant createdAt,
        List<ChallengerPart> targetParts // 대상 파트 (필터링용, 화면엔 배지로 표시)
) {

    public static NoticeSummary of(
            Notice notice,
            RoleType authorRole,
            Integer viewCount,
            Boolean isRead,
            List<NoticeContentType> includeContentTypes
    ) {
        return new NoticeSummary(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                authorRole,
                notice.getScope(),
                isRead,
                includeContentTypes,
                viewCount,
                notice.getCreatedAt(),
                notice.getTargetParts()
        );
    }
}
