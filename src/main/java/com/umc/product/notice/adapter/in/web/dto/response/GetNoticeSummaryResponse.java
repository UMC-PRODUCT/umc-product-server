package com.umc.product.notice.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.notice.application.port.in.query.dto.NoticeSummary;
import com.umc.product.notice.domain.enums.NoticeClassification;
import com.umc.product.notice.domain.enums.NoticeContentType;
import java.time.Instant;
import java.util.List;

public record GetNoticeSummaryResponse(
        Long id,
        String title,
        String content,
        ChallengerRoleType authorRole, // 작성자 role
        NoticeClassification scope,  // 조직 타입 (전체, 중앙운영사무국 등)
        Boolean read, // 읽음 여부 (빨간 점 표시용)
        List<NoticeContentType> includeContentTypes,
        Integer viewCount,
        Instant createdAt,
        List<ChallengerPart> targetParts // 대상 파트 (필터링용, 화면엔 배지로 표시)
) {
    public static GetNoticeSummaryResponse from(NoticeSummary summary) {
        return new GetNoticeSummaryResponse(
                summary.id(),
                summary.title(),
                summary.content(),
                summary.authorRole(),
                summary.scope(),
                summary.read(),
                summary.includeContentTypes(),
                summary.viewCount(),
                summary.createdAt(),
                summary.targetParts()
        );
    }
}
