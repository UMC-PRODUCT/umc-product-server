package com.umc.product.community.application.port.in.post;

import com.umc.product.community.application.port.in.post.Command.UpdateBoardsCommand;

public interface UpdateBoardsUseCase {
    BoardsInfo updateBoards(Long boardsId, UpdateBoardsCommand command);
}
