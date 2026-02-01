package com.umc.product.notice.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;

/*
 * Notice 내 공용 DTO - 공지 대상 정보 (실제 작성된 공지의 대상)
 * command에서 분리해서 공지사항 대상자 정보를 따로 관리합니다
 */
public record NoticeTargetInfo(
    Long targetGisuId,
    Long targetChapterId,
    Long targetSchoolId,
    List<ChallengerPart> targetParts
) {
}
