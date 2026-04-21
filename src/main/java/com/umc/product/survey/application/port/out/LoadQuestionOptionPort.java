package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.QuestionOption;
import java.util.List;

public interface LoadQuestionOptionPort {
    // List<QuestionOption> findAllByQuestionIdIn(Set<Long> questionIds);

    boolean existsByIdAndQuestionId(Long optionId, Long questionId);

    List<QuestionOption> findAllByQuestionId(Long questionId);
}
