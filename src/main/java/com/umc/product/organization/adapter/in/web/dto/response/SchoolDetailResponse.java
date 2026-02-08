package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.SchoolDetailInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "학교 상세 정보")
public record SchoolDetailResponse(
        @Schema(description = "지부 ID", example = "1")
        Long chapterId,

        @Schema(description = "지부명", example = "서울")
        String chapterName,

        @Schema(description = "학교명", example = "서울대학교")
        String schoolName,

        @Schema(description = "학교 ID", example = "1")
        Long schoolId,

        @Schema(description = "비고", example = "관악캠퍼스")
        String remark,

        @Schema(description = "로고 이미지 URL")
        String logoImageLink,

        @Schema(description = "카카오톡 링크")
        String kakaoLink,

        @Schema(description = "인스타그램 링크")
        String instagramLink,

        @Schema(description = "유튜브 링크")
        String youtubeLink,

        @Schema(description = "생성일", example = "2024-03-01T00:00:00Z")
        Instant createdAt,

        @Schema(description = "수정일", example = "2024-03-15T00:00:00Z")
        Instant updatedAt
) {
    public static SchoolDetailResponse of(SchoolDetailInfo info) {
        return new SchoolDetailResponse(
                info.chapterId(),
                info.chapterName(),
                info.schoolName(),
                info.schoolId(),
                info.remark(),
                info.logoImageUrl(),
                info.kakaoLink(),
                info.instagramLink(),
                info.youtubeLink(),
                info.createdAt(),
                info.updatedAt()
        );
    }
}
