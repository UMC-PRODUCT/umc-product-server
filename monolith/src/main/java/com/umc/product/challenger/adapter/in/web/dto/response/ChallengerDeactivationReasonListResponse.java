package com.umc.product.challenger.adapter.in.web.dto.response;

import java.util.List;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationReasonInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationReasonListInfo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "탈부 유형별 사유 목록")
public record ChallengerDeactivationReasonListResponse(
    @Schema(description = "자진 탈부 사유") List<ReasonResponse> voluntaryReasons,
    @Schema(description = "타의 탈부 사유") List<ReasonResponse> involuntaryReasons
) {
    public static ChallengerDeactivationReasonListResponse from(ChallengerDeactivationReasonListInfo info) {
        return new ChallengerDeactivationReasonListResponse(
            info.voluntaryReasons().stream().map(ReasonResponse::from).toList(),
            info.involuntaryReasons().stream().map(ReasonResponse::from).toList()
        );
    }

    @Schema(description = "탈부 사유")
    public record ReasonResponse(
        @Schema(description = "요청에 전달할 탈부 사유 코드", example = "VOLUNTARY_PERSONAL_CIRCUMSTANCES")
        String reason,
        @Schema(description = "화면 표시명", example = "개인 사정") String displayName,
        @Schema(description = "화면 보조 설명", example = "건강, 가정 사정 등") String description,
        @Schema(description = "상세 사유 입력 필수 여부", example = "false") boolean reasonDetailRequired
    ) {
        public static ReasonResponse from(ChallengerDeactivationReasonInfo info) {
            return new ReasonResponse(
                info.reason().name(),
                info.displayName(),
                info.description(),
                info.reasonDetailRequired()
            );
        }
    }
}
