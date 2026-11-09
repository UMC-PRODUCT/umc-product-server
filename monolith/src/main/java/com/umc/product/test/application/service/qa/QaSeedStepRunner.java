package com.umc.product.test.application.service.qa;

import com.umc.product.test.application.port.in.command.dto.QaSeedStep;

interface QaSeedStepRunner {
    QaSeedStep step();

    /**
     * 이미 있는 데이터는 사전 조회로 건너뛴다. 트랜잭션 경계는 구현체가 정한다.
     */
    StepTally run(QaDirectory directory);
}
