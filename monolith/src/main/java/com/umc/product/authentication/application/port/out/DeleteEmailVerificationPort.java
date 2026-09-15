package com.umc.product.authentication.application.port.out;

import java.time.Instant;

public interface DeleteEmailVerificationPort {

    /**
     * expires_at 이 주어진 시각보다 이전인 레코드를 일괄 삭제한다.
     *
     * @return 삭제된 행 수
     */
    int deleteExpiredBefore(Instant threshold);
}
