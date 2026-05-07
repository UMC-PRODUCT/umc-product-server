package com.umc.product.figma.application.port.out;

import com.umc.product.figma.application.port.out.dto.DiscordMentionMessage;

public interface SendDiscordMentionPort {

    /**
     * 파트별 Discord webhook으로 role mention과 함께 메시지를 전송한다.
     * allowed_mentions.parse 에 "roles" 가 포함되도록 보장해야 한다.
     */
    void send(DiscordMentionMessage message);
}
