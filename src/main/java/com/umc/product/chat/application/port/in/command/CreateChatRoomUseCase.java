package com.umc.product.chat.application.port.in.command;

import com.umc.product.chat.application.port.in.command.dto.CreateChatRoomCommand;

public interface CreateChatRoomUseCase {

    Long create(CreateChatRoomCommand command);
}
