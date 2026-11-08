package com.umc.product.test.adapter.in.web.dto;

import java.util.List;
import java.util.Map;

import com.umc.product.test.application.port.in.command.dto.QaStepResult;

public record QaSeedResponse(List<StepResponse> steps) {

    public static QaSeedResponse from(List<QaStepResult> results) {
        return new QaSeedResponse(results.stream().map(StepResponse::from).toList());
    }

    public record StepResponse(
        String step,
        String status,
        int created,
        int skipped,
        Map<String, Integer> failed,
        String errorCode,
        String message,
        long elapsedMs
    ) {
        static StepResponse from(QaStepResult result) {
            return new StepResponse(result.step().path(), result.status().name(), result.created(),
                result.skipped(), result.failed(), result.errorCode(), result.message(), result.elapsedMs());
        }
    }
}
