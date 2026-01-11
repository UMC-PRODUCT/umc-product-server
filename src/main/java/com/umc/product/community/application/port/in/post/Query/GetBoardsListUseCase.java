package com.umc.product.community.application.port.in.post.Query;

import com.umc.product.community.application.port.in.post.BoardsInfo;
import java.util.List;

public interface GetBoardsListUseCase {
    List<BoardsInfo> getBoards(BoardsSearchQuery query);
}
