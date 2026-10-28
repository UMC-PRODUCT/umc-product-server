package com.umc.product.report.application.port.in.command;

import com.umc.product.report.application.port.in.command.dto.RedeliverReportsCommand;
import com.umc.product.report.application.port.in.command.dto.RedeliverReportsResult;

/**
 * 운영 채널로 전달하지 못한 신고(PENDING, FAILED)를 다시 전달하도록 outbox에 넣습니다. SUPER_ADMIN만 실행할 수 있습니다.
 * <p>
 * 방금 접수되어 아직 전달 중인 신고를 두 번 보내지 않도록, 접수 후 일정 시간이 지난 신고만 대상으로 합니다.
 */
public interface RedeliverReportsUseCase {

    RedeliverReportsResult redeliver(RedeliverReportsCommand command);
}
