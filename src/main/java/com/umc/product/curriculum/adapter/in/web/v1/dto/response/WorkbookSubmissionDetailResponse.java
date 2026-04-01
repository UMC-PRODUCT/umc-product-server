package com.umc.product.curriculum.adapter.in.web.v1.dto.response;

import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionDetailInfo;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "워크북 제출 상세 응답")
public record WorkbookSubmissionDetailResponse(
    @Schema(description = "챌린저 워크북 ID", example = "1")
    Long challengerWorkbookId,
    @Schema(description = "워크북 상태", example = "PASS")
    WorkbookStatus status,
    @Schema(description = "제출 내용", example = "https://github.com/user/repo")
    String content,
    @Schema(description = "리뷰 목록")
    List<ReviewResponse> reviews
) {
    public static WorkbookSubmissionDetailResponse from(WorkbookSubmissionDetailInfo info) {
        return new WorkbookSubmissionDetailResponse(
            info.challengerWorkbookId(),
            info.status(),
            info.content(),
            info.reviews().stream()
                .map(ReviewResponse::from)
                .toList()
        );
    }

    @Schema(description = "리뷰 정보")
    public record ReviewResponse(
        @Schema(description = "리뷰 ID", example = "1")
        Long reviewId,
        @Schema(description = "리뷰어 챌린저 ID", example = "5")
        Long reviewerChallengerId,
        @Schema(description = "피드백", example = "잘 작성하셨습니다!")
        String feedback,
        @Schema(description = "베스트 선정 이유", example = "꼼꼼한 분석이 돋보입니다.")
        String bestReason
    ) {
        public static ReviewResponse from(WorkbookSubmissionDetailInfo.ReviewInfo info) {
            return new ReviewResponse(
                info.reviewId(),
                info.reviewerChallengerId(),
                info.feedback(),
                info.bestReason()
            );
        }
    }
}
