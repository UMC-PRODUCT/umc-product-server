package com.umc.product.figma.application.port.out;

import com.umc.product.figma.application.port.out.dto.DiscordDomainBatchMessage;
import java.util.Set;

public interface SendDiscordMentionPort {

    /**
     * 도메인 단위로 묶인 댓글 batch 를 Discord webhook 으로 발송한다.
     * <p>
     * 어댑터가 Discord embed 제약(필드 25개 / 메시지 당 embed 10개 / 전체 6000자)에 맞춰 자동으로 메시지를 분할한다.
     * 부분 실패(일부 페이지만 발송 성공) 시에도 성공한 페이지에 속한 commentId 를 반환해 호출자가 dispatch 기록을 남길 수 있도록 한다.
     * 첫 페이지부터 실패한 경우 예외를 던진다.
     *
     * @return 실제로 Discord 에 발송 완료된 댓글들의 commentId 집합
     */
    Set<String> send(DiscordDomainBatchMessage message);
}
