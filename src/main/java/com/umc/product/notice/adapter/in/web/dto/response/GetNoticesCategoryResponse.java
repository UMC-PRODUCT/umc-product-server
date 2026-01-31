package com.umc.product.notice.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.OrganizationType;
import com.umc.product.notice.application.port.in.query.dto.NoticeDetailOrganizationOption;
import com.umc.product.notice.application.port.in.query.dto.WritableNoticeScopeOption;
import com.umc.product.notice.domain.enums.NoticeClassification;
import java.util.List;

public record GetNoticesCategoryResponse(
    OrganizationType category, /* 공지 카테고리 */
    String displayName, /* UI 표시명 */
    NoticeClassification scope, /* 선택 가능한 게시판 분류 */
    boolean requiresOrganizationSelection, /* 조직 선택 필요 여부 (지부장, 중앙의 경우 각각 학교, 지부 선택 가능) */
    List<NoticeDetailOrganizationOption> availableOrganizations, /* 선택 가능한 조직들 */
    boolean requiresPartSelection, /* 파트 선택 필요 여부 (파트장은 false, 전체파트 겨냥하는 경우도 false) */
    List<ChallengerPart> availableParts
) {
    public static GetNoticesCategoryResponse from(WritableNoticeScopeOption option) {
        return new GetNoticesCategoryResponse(
            option.category(),
            option.displayName(),
            option.noticeClassification(),
            option.requiresOrganizationSelection(),
            option.availableOrganizations(),
            option.requiresPartSelection(),
            option.availableParts()
        );
    }
}
