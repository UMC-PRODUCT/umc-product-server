package com.umc.product.notice.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.notice.domain.enums.NoticeTargetRole;
import java.util.Set;

/**
 * 공지를 조회하는 멤버의 소속 정보.
 * <p>
 * 공지 목록 조회 시 파트 필터링 및 지부/학교 필터 enrichment에 사용됩니다. roles는 운영진 공지 조회 시 대상 여부 판별에 사용됩니다.
 */
public record NoticeViewerInfo(
    Set<ChallengerPart> memberParts, // 속한 파트 + 파트장 맡은 파트
    Long schoolId,
    Long chapterId,
    Set<NoticeTargetRole> roles // 보유한 운영진 역할 (운영진 공지 조회용)
) {
}
