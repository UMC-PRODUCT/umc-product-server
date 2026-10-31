package com.umc.product.community.adapter.in.report;

import org.springframework.stereotype.Component;

import com.umc.product.community.application.port.in.command.thread.message.ModerateCommunityThreadMessageUseCase;
import com.umc.product.report.application.port.out.RemoveReportTargetPort;
import com.umc.product.report.domain.enums.ReportTargetRemoval;

import lombok.RequiredArgsConstructor;

/**
 * 신고 처리에서 스레드 메시지 삭제를 요청하면 community의 운영 삭제 use case로 넘기는 어댑터입니다.
 */
@Component
@RequiredArgsConstructor
public class CommunityReportTargetRemovalAdapter implements RemoveReportTargetPort {

    private final ModerateCommunityThreadMessageUseCase moderateCommunityThreadMessageUseCase;

    @Override
    public ReportTargetRemoval removeThreadMessage(Long threadId, Long messageId) {
        return switch (moderateCommunityThreadMessageUseCase.removeByOperator(threadId, messageId)) {
            case DELETED -> ReportTargetRemoval.DELETED;
            case ALREADY_DELETED -> ReportTargetRemoval.ALREADY_DELETED;
            case NOT_FOUND -> ReportTargetRemoval.NOT_FOUND;
        };
    }
}
