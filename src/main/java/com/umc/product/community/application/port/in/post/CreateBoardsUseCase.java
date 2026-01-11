package com.umc.product.community.application.port.in.post;

import com.umc.product.community.application.port.in.CreateLightningCommand;
import com.umc.product.community.application.port.in.post.Command.CreateBoardsCommand;

public interface CreateBoardsUseCase {
    BoardsInfo createBoards(CreateBoardsCommand command);

    BoardsInfo createLightningBoards(CreateLightningCommand command);
}
