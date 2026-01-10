package com.umc.product.community.application.port.in;

import java.util.List;

public interface BoardsUseCase {
    BoardsResponse createBoards(CreateBoardsCommand command);

    BoardsResponse createLightningBoards(CreateLightningCommand command);

    List<BoardsResponse> getBoards(BoardsSearchQuery query);

    BoardsResponse getBoards(Long boardsId);

    void deleteBoards(Long boardsId);

    BoardsResponse updateBoards(Long boardsId, UpdateBoardsCommand command);
}
