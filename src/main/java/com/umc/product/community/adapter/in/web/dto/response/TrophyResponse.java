package com.umc.product.community.adapter.in.web.dto.response;

import com.umc.product.community.application.port.in.command.trophy.dto.TrophyInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상장 응답")
public record TrophyResponse(
    @Schema(description = "상장 ID", example = "1")
    Long trophyId,

    @Schema(description = "챌린저 ID", example = "123")
    Long challengerId,

    @Schema(description = "주차", example = "3")
    Integer week,

    @Schema(description = "챌린저 이름", example = "홍길동")
    String challengerName,

    @Schema(description = "챌린저 프로필 이미지", example = "https://example.com/profile.jpg")
    String challengerProfileImage,

    @Schema(description = "학교명", example = "서울대학교")
    String school,

    @Schema(description = "파트", example = "SPRINGBOOT")
    String part,

    @Schema(description = "상장 제목", example = "3주차 최우수 챌린저")
    String title,

    @Schema(description = "상장 내용", example = "3주차 미션을 성실히 수행하여...")
    String content,

    @Schema(description = "상장 노션 링크", example = "https://www.notion.so/trophy-page-12345")
    String url
) {
    public static TrophyResponse from(TrophyInfo info) {
        return new TrophyResponse(
            info.trophyId(),
            info.challengerId(),
            info.week(),
            info.challengerName(),
            info.challengerProfileImage(),
            info.school(),
            info.part(),
            info.title(),
            info.content(),
            info.url()
        );
    }
}
