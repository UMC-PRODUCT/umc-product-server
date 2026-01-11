package com.umc.product.community.application.port.in;

import com.umc.product.community.domain.Boards;
import com.umc.product.community.domain.Category;
import java.time.LocalDateTime;

public record BoardsInfo(
        Long boardsId,
        String title,
        String content,
        Category category,
        String region,
        boolean anonymous,
        LocalDateTime meetAt,
        String location,
        Integer maxParticipants
) {
    public static BoardsInfo from(Boards boards) {
        Long boardsId = boards.getBoardsId() != null ? boards.getBoardsId().id() : null;

        // 번개글인 경우
        if (boards.isLightning()) {
            Boards.LightningInfo info = boards.getLightningInfoOrThrow();
            return new BoardsInfo(
                    boardsId,
                    boards.getTitle(),
                    boards.getContent(),
                    boards.getCategory(),
                    boards.getRegion(),
                    boards.isAnonymous(),
                    info.meetAt(),
                    info.location(),
                    info.maxParticipants()
            );
        }

        // 일반 게시글
        return new BoardsInfo(
                boardsId,
                boards.getTitle(),
                boards.getContent(),
                boards.getCategory(),
                boards.getRegion(),
                boards.isAnonymous(),
                null,
                null,
                null
        );
    }
}
