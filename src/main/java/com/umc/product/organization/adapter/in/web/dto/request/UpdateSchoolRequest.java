package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.UpdateSchoolCommand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "학교 수정 요청")
public record UpdateSchoolRequest(
        @Schema(description = "학교명 (수정할 경우만 입력)", example = "서울대학교")
        String schoolName,

        @Schema(description = "지부 ID (수정할 경우만 입력)", example = "1")
        Long chapterId,

        @Schema(description = "비고 (수정할 경우만 입력)", example = "관악캠퍼스")
        String remark,

        @Schema(description = "로고 이미지 파일 ID (수정할 경우만 입력)", example = "abc123-def456")
        String logoImageId,

        @Schema(description = "카카오톡 링크 (수정할 경우만 입력)", example = "https://open.kakao.com/o/example")
        String kakaoLink,

        @Schema(description = "인스타그램 링크 (수정할 경우만 입력)", example = "https://instagram.com/example")
        String instagramLink,

        @Schema(description = "유튜브 링크 (수정할 경우만 입력)", example = "https://youtube.com/@example")
        String youtubeLink
) {
    public UpdateSchoolCommand toCommand() {
        return new UpdateSchoolCommand(schoolName, chapterId, remark, logoImageId, kakaoLink, instagramLink, youtubeLink);
    }
}
