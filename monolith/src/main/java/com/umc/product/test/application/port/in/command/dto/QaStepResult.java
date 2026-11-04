package com.umc.product.test.application.port.in.command.dto;

import java.util.Map;

/**
 * QA 시딩 단계 하나의 결과. failed는 개별 실패를 에러 코드별로 센 값이다.
 */
public record QaStepResult(
    QaSeedStep step,
    Status status,
    int created,
    int skipped,
    Map<String, Integer> failed,
    String errorCode,
    String message,
    long elapsedMs
) {
    public QaStepResult {
        failed = Map.copyOf(failed);
    }

    public static QaStepResult notRun(QaSeedStep step) {
        return new QaStepResult(step, Status.NOT_RUN, 0, 0, Map.of(), null, null, 0);
    }

    public enum Status {
        APPLIED,
        UNCHANGED,
        FAILED,
        NOT_RUN
    }
}
