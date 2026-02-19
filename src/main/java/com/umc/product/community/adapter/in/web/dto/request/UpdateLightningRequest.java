package com.umc.product.community.adapter.in.web.dto.request;

import com.umc.product.community.application.port.in.command.post.dto.UpdateLightningCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Objects;

@Schema(description = "번개 게시글 수정 요청")
public record UpdateLightningRequest(
    @Schema(description = "제목", example = "오늘 저녁 치킨 먹을 사람! (수정)")
    String title,

    @Schema(description = "내용", example = "강남역 근처에서 치킨 먹을 분 구합니다. (수정된 내용)")
    String content,

    @Schema(description = "모임 시간", example = "2026-03-16T19:00:00")
    LocalDateTime meetAt,

    @Schema(description = "모임 장소", example = "강남역 3번 출구")
    String location,

    @Schema(description = "최대 참가자 수", example = "6")
    Integer maxParticipants,

    @Schema(description = "오픈 채팅 링크", example = "https://open.kakao.com/o/sxxxxxx")
    String openChatUrl
) {
    public UpdateLightningRequest {
        Objects.requireNonNull(title, "제목은 필수입니다");
        Objects.requireNonNull(content, "내용은 필수입니다");
        Objects.requireNonNull(meetAt, "모임 시간은 필수입니다");
        Objects.requireNonNull(location, "모임 장소는 필수입니다");
        Objects.requireNonNull(maxParticipants, "최대 참가자 수는 필수입니다");
        Objects.requireNonNull(openChatUrl, "오픈 채팅 링크는 필수입니다");

        if (title.isBlank()) {
            throw new IllegalArgumentException("제목은 비어있을 수 없습니다");
        }
        if (content.isBlank()) {
            throw new IllegalArgumentException("내용은 비어있을 수 없습니다");
        }
        if (location.isBlank()) {
            throw new IllegalArgumentException("모임 장소는 비어있을 수 없습니다");
        }
        if (openChatUrl.isBlank()) {
            throw new IllegalArgumentException("오픈 채팅 링크는 비어있을 수 없습니다");
        }
        if (meetAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("모임 시간은 현재 이후여야 합니다");
        }
        if (maxParticipants < 1) {
            throw new IllegalArgumentException("최대 참가자는 1명 이상이어야 합니다");
        }
    }

    public UpdateLightningCommand toCommand(Long postId) {
        return new UpdateLightningCommand(postId, title, content, meetAt, location, maxParticipants, openChatUrl);
    }
}
