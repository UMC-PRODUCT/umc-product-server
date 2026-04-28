package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.QuestionOption;
import java.util.List;
import java.util.Optional;

public interface LoadQuestionOptionPort {

    Optional<QuestionOption> findById(Long optionId);

    boolean existsByIdAndQuestionId(Long optionId, Long questionId);

    List<QuestionOption> listByQuestionId(Long questionId);
}
