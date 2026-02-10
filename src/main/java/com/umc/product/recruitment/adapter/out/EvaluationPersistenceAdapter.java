package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.application.port.out.LoadEvaluationPort;
import com.umc.product.recruitment.application.port.out.SaveEvaluationPort;
import com.umc.product.recruitment.domain.Evaluation;
import com.umc.product.recruitment.domain.enums.EvaluationStage;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EvaluationPersistenceAdapter implements LoadEvaluationPort, SaveEvaluationPort {

    private final EvaluationRepository evaluationRepository;
    private final EvaluationQueryRepository evaluationQueryRepository;

    // ============== LoadEvaluationPort ==============
    @Override
    public Optional<Evaluation> findByApplicationIdAndEvaluatorUserIdAndStage(
        Long applicationId,
        Long evaluatorUserId,
        EvaluationStage stage
    ) {
        return evaluationRepository.findByApplicationIdAndEvaluatorUserIdAndStage(
            applicationId, evaluatorUserId, stage
        );
    }

    @Override
    public List<Evaluation> findByApplicationIdAndStage(Long applicationId, EvaluationStage evaluationStage) {
        return evaluationRepository.findByApplicationIdAndStage(applicationId, evaluationStage);
    }

    @Override
    public Set<Long> findApplicationIdsWithEvaluations(
        Set<Long> applicationIds,
        Long evaluatorUserId,
        EvaluationStage stage
    ) {
        return evaluationQueryRepository.findApplicationIdsWithEvaluations(applicationIds, evaluatorUserId, stage);
    }

    // ============== SaveEvaluationPort ==============
    @Override
    public Evaluation save(Evaluation evaluation) {
        return evaluationRepository.save(evaluation);
    }
}
