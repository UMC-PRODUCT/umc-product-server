package com.umc.product.figma.application.port.in.dto;

import java.time.Instant;

/**
 * 사이클과 무관하게 운영진이 명시적으로 지정한 시간창 [from, to] 의 댓글을 도메인별로 묶어 Discord 로 발송하기 위한 입력. cursor 는 변경하지 않으며, force=true 로 dispatch
 * 행이 있는 댓글도 재발송한다 (ADR-004 §Decision 2).
 */
public record DigestFigmaCommentsCommand(
    Instant from,
    Instant to
) {
}
