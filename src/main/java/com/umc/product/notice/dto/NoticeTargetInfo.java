package com.umc.product.notice.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.notice.domain.NoticeTarget;
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
    public static NoticeTargetInfo from(NoticeTarget noticeTarget) {
        return new NoticeTargetInfo(
            noticeTarget.getTargetGisuId(),
            noticeTarget.getTargetChapterId(),
            noticeTarget.getTargetSchoolId(),
            noticeTarget.getTargetChallengerPart()
        );
    }

    public boolean isTarget(Long gisuId, Long chapterId, Long schoolId, ChallengerPart part) {
        boolean gisuMatch = (this.targetGisuId == null || this.targetGisuId.equals(gisuId));
        boolean chapterMatch = (this.targetChapterId == null || this.targetChapterId.equals(chapterId));
        boolean schoolMatch = (this.targetSchoolId == null || this.targetSchoolId.equals(schoolId));
        boolean partMatch = (this.targetParts.isEmpty() || this.targetParts.contains(part));

        return gisuMatch && chapterMatch && schoolMatch && partMatch;
    }
}
