package com.umc.product.community.adapter.in.web.dto.request;

import com.umc.product.community.application.port.in.post.command.CreateLightningCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Objects;

@Schema(description = "번개 게시글 작성 요청")
public record CreateLightningRequest(
        @Schema(description = "제목", example = "오늘 저녁 치킨 먹을 사람!")
        String title,

        @Schema(description = "내용", example = "강남역 근처에서 치킨 먹을 분 구합니다.")
        String content,

        @Schema(description = "모임 시간", example = "2026-03-16T18:00:00")
        LocalDateTime meetAt,

        @Schema(description = "모임 장소", example = "강남역 2번 출구")
        String location,

        @Schema(description = "최대 참가자 수", example = "5")
        Integer maxParticipants
) {
    public CreateLightningRequest {
        Objects.requireNonNull(title, "제목은 필수입니다");
        Objects.requireNonNull(content, "내용은 필수입니다");
        Objects.requireNonNull(meetAt, "모임 시간은 필수입니다");
        Objects.requireNonNull(location, "모임 장소는 필수입니다");
        Objects.requireNonNull(maxParticipants, "최대 참가자 수는 필수입니다");

        if (title.isBlank()) {
            throw new IllegalArgumentException("제목은 비어있을 수 없습니다");
        }
        if (content.isBlank()) {
            throw new IllegalArgumentException("내용은 비어있을 수 없습니다");
        }
        if (location.isBlank()) {
            throw new IllegalArgumentException("모임 장소는 비어있을 수 없습니다");
        }
        if (meetAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("모임 시간은 현재 이후여야 합니다");
        }
        if (maxParticipants < 1) {
            throw new IllegalArgumentException("최대 참가자는 1명 이상이어야 합니다");
        }
    }

    public CreateLightningCommand toCommand() {
        return new CreateLightningCommand(title, content, meetAt, location, maxParticipants);
    }
}
