package com.umc.product.figma.application.port.out;

import com.umc.product.figma.application.port.out.dto.DiscordDomainBatchMessage;

public interface SendDiscordMentionPort {

    /**
     * 도메인 단위로 묶인 댓글 batch 를 Discord webhook 으로 발송한다. 어댑터가 Discord embed 제약(필드 25개 / 메시지 당 embed 10개 / 전체 6000자) 에 맞춰
     * 자동으로 메시지를 분할한다. allowed_mentions 에 roles, users 가 포함되도록 보장한다.
     */
    void send(DiscordDomainBatchMessage message);
}
