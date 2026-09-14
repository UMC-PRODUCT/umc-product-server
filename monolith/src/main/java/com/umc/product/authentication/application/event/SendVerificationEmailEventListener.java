package com.umc.product.authentication.application.event;

import com.umc.product.notification.application.port.in.SendEmailUseCase;
import com.umc.product.notification.application.port.in.dto.SendVerificationEmailCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * {@link SendVerificationEmailEvent} 를 받아 트랜잭션 commit 직후 실제 메일 발송을 트리거한다.
 * <p>
 * SendEmailUseCase 구현이 자체적으로 @Async 비동기 처리를 수행하므로, 이 리스너는 큐잉 역할만 한다.
 * 트랜잭션이 롤백되면 발송 이벤트도 함께 사라져, 세션이 영속화되지 않은 상태에서 메일이 나가는
 * 일을 막는다.
 */
@Component
@RequiredArgsConstructor
public class SendVerificationEmailEventListener {

    private final SendEmailUseCase sendEmailUseCase;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SendVerificationEmailEvent event) {
        sendEmailUseCase.sendVerificationEmail(
            SendVerificationEmailCommand.builder()
                .to(event.email())
                .verificationCode(event.verificationCode())
                .build()
        );
    }
}
