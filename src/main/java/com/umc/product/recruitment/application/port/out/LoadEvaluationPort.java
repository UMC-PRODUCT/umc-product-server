package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.Evaluation;
import com.umc.product.recruitment.domain.enums.EvaluationStage;
import java.util.Optional;

public interface LoadEvaluationPort {

    Optional<Evaluation> findByApplicationIdAndEvaluatorUserIdAndStage(
        Long applicationId,
        Long evaluatorUserId,
        EvaluationStage stage
    );
}
