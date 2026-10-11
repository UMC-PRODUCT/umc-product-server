package com.umc.product.chat.application.port.in.command;

import com.umc.product.chat.application.port.in.command.dto.CreateChatMessageCommand;

/**
 * 신규 메시지의 첨부 검증
 */
@FunctionalInterface
public interface ChatMessageAttachmentValidation {

    void validate(CreateChatMessageCommand command);
}
