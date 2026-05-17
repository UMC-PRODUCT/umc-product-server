package com.umc.product.authentication.application.port.out;

import com.umc.product.authentication.domain.EmailVerification;
import java.util.Optional;

public interface LoadEmailVerificationPort {
    EmailVerification getById(Long id);

    EmailVerification getByToken(String token);

    /**
     * 주어진 email 에 대해 실제로 발송이 일어난 가장 최근 세션을 조회한다.
     * throttle 검사 용도로, last_sent_at IS NOT NULL 인 레코드만 대상으로 한다.
     */
    Optional<EmailVerification> findLatestSentByEmail(String email);
}
