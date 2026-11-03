package com.umc.product.report.application.port.in.command.dto;

/**
 * @param requesterMemberId 요청한 회원 ID
 * @param limit             한 번에 다시 보낼 최대 신고 수
 */
public record RedeliverReportsCommand(Long requesterMemberId, int limit) {

    public static final int MAX_LIMIT = 100;

    public RedeliverReportsCommand {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and " + MAX_LIMIT);
        }
    }
}
