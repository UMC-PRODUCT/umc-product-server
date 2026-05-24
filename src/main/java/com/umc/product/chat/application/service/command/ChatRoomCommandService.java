package com.umc.product.chat.application.service.command;

import com.umc.product.chat.application.port.in.command.CreateChatRoomUseCase;
import com.umc.product.chat.application.port.in.command.DeleteChatRoomUseCase;
import com.umc.product.chat.application.port.in.command.dto.CreateChatRoomCommand;
import com.umc.product.chat.application.port.out.LoadChatRoomPort;
import com.umc.product.chat.application.port.out.SaveChatMemberPort;
import com.umc.product.chat.application.port.out.SaveChatRoomPort;
import com.umc.product.chat.domain.ChatMember;
import com.umc.product.chat.domain.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomCommandService implements CreateChatRoomUseCase, DeleteChatRoomUseCase {

    private final SaveChatRoomPort saveChatRoomPort;
    private final LoadChatRoomPort loadChatRoomPort;
    private final SaveChatMemberPort saveChatMemberPort;

    @Override
    public Long create(CreateChatRoomCommand command) {
        ChatRoom chatRoom = saveChatRoomPort.save(ChatRoom.create());
        saveChatMemberPort.save(ChatMember.of(chatRoom.getId(), command.creatorMemberId()));
        return chatRoom.getId();
    }

    @Override
    public void delete(Long roomId) {
        ChatRoom chatRoom = loadChatRoomPort.getById(roomId);
        saveChatRoomPort.delete(chatRoom);
    }
}
