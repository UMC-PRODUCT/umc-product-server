package com.umc.product.community.adapter.in.web.dto.request;

import com.umc.product.community.application.port.in.trophy.CreateTrophyCommand;
import java.util.Objects;

public record CreateTrophyRequest(
        Long challengerId,
        Integer week,
        String title,
        String content,
        String url
) {
    public CreateTrophyRequest {
        Objects.requireNonNull(challengerId, "챌린저 ID는 필수입니다");
        Objects.requireNonNull(week, "주차는 필수입니다");
        Objects.requireNonNull(title, "제목은 필수입니다");
        Objects.requireNonNull(content, "내용은 필수입니다");
        Objects.requireNonNull(url, "링크는 필수입니다");

        if (week <= 0) {
            throw new IllegalArgumentException("주차는 1 이상이어야 합니다");
        }
        if (title.isBlank()) {
            throw new IllegalArgumentException("제목은 비어있을 수 없습니다");
        }
        if (content.isBlank()) {
            throw new IllegalArgumentException("내용은 비어있을 수 없습니다");
        }
        if (url.isBlank()) {
            throw new IllegalArgumentException("링크는 비어있을 수 없습니다");
        }
    }

    public CreateTrophyCommand toCommand() {
        return new CreateTrophyCommand(challengerId, week, title, content, url);
    }
}
