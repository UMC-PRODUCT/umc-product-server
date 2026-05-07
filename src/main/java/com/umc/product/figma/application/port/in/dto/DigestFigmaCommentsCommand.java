package com.umc.product.figma.application.port.in.dto;

import java.time.Instant;

/**
 * 사이클과 무관하게 운영진이 명시적으로 지정한 시간창 [from, to] 의 댓글을 도메인별로 묶어 Discord 로 발송하기 위한 입력. sync 상태(last_synced_comment_id) 는 변경하지
 * 않는다.
 */
public record DigestFigmaCommentsCommand(
    Instant from,
    Instant to
) {
}
