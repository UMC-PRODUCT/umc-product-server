package com.umc.product.survey.application.port.out;

public interface LoadQuestionOptionPort {
    // List<QuestionOption> findAllByQuestionIdIn(Set<Long> questionIds);

    boolean existsByIdAndQuestionId(Long optionId, Long questionId);
}
