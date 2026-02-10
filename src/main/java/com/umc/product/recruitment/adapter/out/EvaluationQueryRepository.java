package com.umc.product.recruitment.adapter.out;

import static com.umc.product.recruitment.domain.QEvaluation.evaluation;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.recruitment.domain.enums.EvaluationStage;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EvaluationQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 주어진 applicationIds 중 특정 evaluator가 특정 stage에서 평가한 applicationId 목록 반환
     */
    public Set<Long> findApplicationIdsWithEvaluations(
        Set<Long> applicationIds,
        Long evaluatorUserId,
        EvaluationStage stage
    ) {
        return new HashSet<>(queryFactory
            .select(evaluation.application.id)
            .from(evaluation)
            .where(
                evaluation.application.id.in(applicationIds),
                evaluation.evaluatorUserId.eq(evaluatorUserId),
                evaluation.stage.eq(stage)
            )
            .fetch());
    }
}
