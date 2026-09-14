package com.umc.product.audit.adapter.in.event;

import com.umc.product.audit.application.port.in.command.SaveAuditLogUseCase;
import com.umc.product.audit.domain.AuditLogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 감사 로그 이벤트를 비동기로 수신하여 저장합니다.
 * <p>
 * 비즈니스 트랜잭션 커밋 후에만 저장하여 롤백된 트랜잭션의 유령 로그를 방지합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogEventListener {

    private final SaveAuditLogUseCase saveAuditLogUseCase;

    @Async("auditTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handle(AuditLogEvent event) {
        try {
            saveAuditLogUseCase.save(event);
        } catch (Exception e) {
            log.error("감사 로그 저장 실패: domain={}, action={}, target={}:{}, error={}",
                event.domain(), event.action(), event.targetType(), event.targetId(), e.getMessage(), e);
        }
    }
}
