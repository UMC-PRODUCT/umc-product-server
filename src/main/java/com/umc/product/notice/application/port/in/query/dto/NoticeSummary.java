package com.umc.product.notice.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.enums.NoticeClassification;
import com.umc.product.notice.domain.enums.NoticeContentType;
import java.time.Instant;
import java.util.List;

public record NoticeSummary(
        Long id,
        String title,
        String content,
        ChallengerRoleType authorRole, // 작성자 role
        NoticeClassification scope,  // 알림 카테고리
        Boolean read, // 읽음 여부 (빨간 점 표시용)
        List<NoticeContentType> includeContentTypes,
        Integer viewCount,
        Instant createdAt,
        List<ChallengerPart> targetParts // 대상 파트 (필터링용, 화면엔 배지로 표시)
) {

    public static NoticeSummary of(
            Notice notice,
            ChallengerRoleType authorRole,
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

    public static NoticeSummary from(Notice notice, ChallengerRoleType authorRole) {
        return new NoticeSummary(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                authorRole,
                notice.getScope(),
                false, // read - 기본값
                List.of(), // includeContentTypes - 빈 리스트
                0,     // viewCount - 기본값
                notice.getCreatedAt(),
                notice.getTargetParts()
        );
    }
}
