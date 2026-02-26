package com.umc.product.schedule.application.port.in.command.dto;

import java.time.Instant;
import java.util.Objects;

/**
 * 사유 제출 출석 Command
 * <p>
 * 위치 인증이 어려운 경우 사유를 제출하여 출석 체크를 요청합니다.
 * 관리자 승인을 거쳐 인정결석(EXCUSED)으로 처리됩니다.
 */
public record SubmitReasonCommand(
    Long attendanceSheetId,
    Long memberId,
    String reason,
    Instant submittedAt
) {
    public SubmitReasonCommand {
        Objects.requireNonNull(attendanceSheetId, "출석부 ID는 필수입니다");
        Objects.requireNonNull(memberId, "멤버 ID는 필수입니다");
        Objects.requireNonNull(reason, "사유는 필수입니다");
        Objects.requireNonNull(submittedAt, "제출 시간은 필수입니다");

        if (reason.isBlank()) {
            throw new IllegalArgumentException("사유는 비어있을 수 없습니다");
        }
    }
}
