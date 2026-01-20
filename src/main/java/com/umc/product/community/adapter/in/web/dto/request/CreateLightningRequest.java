package com.umc.product.community.adapter.in.web.dto.request;

import com.umc.product.community.application.port.in.post.command.CreateLightningCommand;
import java.time.LocalDateTime;
import java.util.Objects;

public record CreateLightningRequest(
        String title,
        String content,
        String region,
        boolean anonymous,
        LocalDateTime meetAt,
        String location,
        Integer maxParticipants
) {
    public CreateLightningRequest {
        Objects.requireNonNull(title, "제목은 필수입니다");
        Objects.requireNonNull(content, "내용은 필수입니다");
        Objects.requireNonNull(region, "지역은 필수입니다");
        Objects.requireNonNull(meetAt, "모임 시간은 필수입니다");
        Objects.requireNonNull(location, "모임 장소는 필수입니다");
        Objects.requireNonNull(maxParticipants, "최대 참가자 수는 필수입니다");

        if (title.isBlank()) {
            throw new IllegalArgumentException("제목은 비어있을 수 없습니다");
        }
        if (content.isBlank()) {
            throw new IllegalArgumentException("내용은 비어있을 수 없습니다");
        }
        if (region.isBlank()) {
            throw new IllegalArgumentException("지역은 비어있을 수 없습니다");
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
        return new CreateLightningCommand(title, content, region, anonymous, meetAt, location, maxParticipants);
    }
}
