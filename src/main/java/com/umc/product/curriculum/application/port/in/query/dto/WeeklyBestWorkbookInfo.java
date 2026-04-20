package com.umc.product.curriculum.application.port.in.query.dto;

import java.util.List;
import lombok.Builder;

/**
 * 베스트 워크북 조회 결과 Info
 *
 * @param weeklyBestWorkbookEntityId WeeklyBestWorkbook Entity PK
 * @param awardedMemberId            베스트 워크북으로 선정받은 챌린저의 멤버 ID
 * @param studyGroupId               소속 스터디 그룹 ID
 * @param decidedMemberId            베스트 워크북을 선정한 운영진의 멤버 ID
 * @param reason                     선정 사유
 * @param challengerWorkbooks        해당 주차의 챌린저 워크북 목록
 */
@Builder
public record WeeklyBestWorkbookInfo(
    Long weeklyBestWorkbookEntityId,
    Long awardedMemberId,
    Long studyGroupId,
    Long decidedMemberId,
    String reason,
    List<ChallengerWorkbookInfo> challengerWorkbooks
) {
}