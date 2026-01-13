package com.umc.product.notice.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.OrganizationType;
import com.umc.product.notice.domain.enums.NoticeClassification;
import java.util.List;

/*
 * 공지 작성시 공지 카테고리 설정 관련된 부분 먼저 동적 조회
 */
public record WritableNoticeScopeOption(
        OrganizationType category, /* 공지 카테고리 */
        String displayName, /* UI 표시명 */
        List<NoticeClassification> noticeClassifications, /* 선택 가능한 게시판 분류 */
        boolean requiresOrganizationSelection, /* 조직 선택 필요 여부 (지부장, 중앙의 경우 각각 학교, 지부 선택 가능) */
        List<NoticeDetailOrganizationOption> availableOrganizations, /* 선택 가능한 조직들 */
        boolean requiresPartSelection, /* 파트 선택 필요 여부 (파트장은 false, 전체파트 겨냥하는 경우도 false) */
        List<ChallengerPart> availableParts
) {
}
