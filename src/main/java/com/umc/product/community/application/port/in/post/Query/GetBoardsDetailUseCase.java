package com.umc.product.community.application.port.in.post.Query;

import com.umc.product.community.application.port.in.post.BoardsInfo;

public interface GetBoardsDetailUseCase {
    BoardsInfo getBoards(Long boardsId);
}
