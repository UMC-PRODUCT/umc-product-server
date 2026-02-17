package com.umc.product.notice.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.notice.domain.NoticeTarget;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/*
 * Notice 내 공용 DTO - 공지 대상 정보 (실제 작성된 공지의 대상)
 * command에서 분리해서 공지사항 대상자 정보를 따로 관리합니다
 */
@Schema(description = "공지 대상 범위 설정. null인 필드는 '전체'를 의미합니다. "
    + "예) targetGisuId만 입력 → 해당 기수 전체 / targetChapterId도 입력 → 해당 지부만 / targetParts도 입력 → 해당 파트만")
public record NoticeTargetInfo(
    @Schema(description = "대상 기수 ID. null이면 전체 기수 대상", example = "9", nullable = true)
    Long targetGisuId,

    @Schema(description = "대상 지부 ID. null이면 해당 기수의 모든 지부 대상", example = "3", nullable = true)
    Long targetChapterId,

    @Schema(description = "대상 학교 ID. null이면 해당 지부의 모든 학교 대상", example = "5", nullable = true)
    Long targetSchoolId,

    @Schema(description = "대상 파트 목록. 빈 배열([])이면 모든 파트 대상. "
        + "특정 파트만 지정하면 해당 파트 챌린저에게만 공지됨",
        example = "[\"SPRINGBOOT\", \"WEB\"]")
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
        boolean partMatch = (this.targetParts == null || this.targetParts.isEmpty() || this.targetParts.contains(part));

        return gisuMatch && chapterMatch && schoolMatch && partMatch;
    }
}
