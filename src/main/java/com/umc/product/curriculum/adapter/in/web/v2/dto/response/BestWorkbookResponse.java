package com.umc.product.curriculum.adapter.in.web.v2.dto.response;

import com.umc.product.curriculum.domain.WeeklyBestWorkbook;
import java.util.List;
import lombok.Builder;

/**
 * 베스트 워크북에 대한 정보를 조회합니다.
 *
 * @param weeklyBestWorkbookEntityId {@link WeeklyBestWorkbook}의 PK
 * @param memberId                   베스트 워크북으로 선정받은 회원 ID
 * @param studyGroupId               베스트 워크북으로 선정받은 스터디 그룹 ID
 * @param decidedMemberId            베스트 워크북을 선정한 회원 ID
 * @param reason                     베스트 워크북 선정 사유
 * @param challengerWorkbooks        베스트 워크북으로 선정된 주차의 워크북들
 */
@Builder
public record BestWorkbookResponse(
    Long weeklyBestWorkbookEntityId,
    Long memberId,
    Long studyGroupId, // NOT NULL!
    Long decidedMemberId,
    String reason,
    List<ChallengerWorkbookResponse> challengerWorkbooks
) {
}
