package com.umc.product.notice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.notice.domain.enums.NoticeTab;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 역할: "이 공지는 누구를 대상으로 하는가"를 표현하는 공지 대상 범위 데이터.
 * <p>
 * 공지 생성/수정 시 Request에 담겨 들어오고(입력), 공지 조회 시 Response에 담겨 나갑니다(출력). null 필드는 해당 차원에서 전체 대상임을 의미합니다.
 * <p>
 * - targetNoticeTab: 운영진 공지의 하한선 역할. CHALLENGER면 일반 챌린저 공지. - from(NoticeTarget): 엔티티 → DTO 변환 (조회 방향)
 */
@Schema(description = "공지 대상 범위 설정. null인 필드는 '전체'를 의미합니다. "
    + "예) targetGisuId만 입력 → 해당 기수 전체 / targetChapterId도 입력 → 해당 지부만 / targetParts도 입력 → 해당 파트만 / "
    + "minTargetRole에 CHALLENGER 외 값 입력 → 운영진 공지")
public record NoticeTargetInfo(
    @Schema(description = "대상 기수 ID. null이면 전체 기수 대상", example = "9", nullable = true)
    Long targetGisuId,

    @Schema(description = "대상 지부 ID. null이면 해당 기수의 모든 지부 대상", example = "3", nullable = true)
    Long targetChapterId,

    @Schema(description = "대상 학교 ID. null이면 전체 학교 대상. 교내운영진 공지 시 필수", example = "5", nullable = true)
    Long targetSchoolId,

    @Schema(description = "대상 파트 목록. 빈 배열([])이면 모든 파트 대상. "
        + "특정 파트만 지정하면 해당 파트 챌린저/파트장에게만 공지됨",
        example = "[\"SPRINGBOOT\", \"WEB\"]")
    List<ChallengerPart> targetParts,

    @Schema(description = "대상 역할 하한선. CHALLENGER면 일반 챌린저 공지. "
        + "CENTRAL_MEMBER/SCHOOL_CORE/SCHOOL_PART_LEADER면 운영진 공지.",
        example = "CHALLENGER")
    @NotNull(message = "대상 역할은 필수입니다. 챌린저 공지의 경우 CHALLENGER를 입력하세요.")
    NoticeTab targetNoticeTab
) {
    /**
     * 엔티티 → DTO 변환 (조회 방향)
     */
    public static NoticeTargetInfo from(NoticeTarget noticeTarget) {
        return new NoticeTargetInfo(
            noticeTarget.getTargetGisuId(),
            noticeTarget.getTargetChapterId(),
            noticeTarget.getTargetSchoolId(),
            noticeTarget.getTargetChallengerPart(),
            noticeTarget.getTargetNoticeTab()
        );
    }

    @JsonIgnore
    public boolean isStaffNotice() {
        return targetNoticeTab != null && targetNoticeTab != NoticeTab.CHALLENGER;
    }

    public boolean isTarget(Long gisuId, Long chapterId, Long schoolId, ChallengerPart part) {
        boolean gisuMatch = (this.targetGisuId == null || this.targetGisuId.equals(gisuId));
        boolean chapterMatch = (this.targetChapterId == null || this.targetChapterId.equals(chapterId));
        boolean schoolMatch = (this.targetSchoolId == null || this.targetSchoolId.equals(schoolId));
        boolean partMatch = (this.targetParts == null || this.targetParts.isEmpty() || this.targetParts.contains(part));

        return gisuMatch && chapterMatch && schoolMatch && partMatch;
    }
}
