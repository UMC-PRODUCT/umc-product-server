package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.QuestionOption;

import java.util.List;

public interface SaveQuestionOptionPort {
    void deleteAllByQuestionId(Long questionId);

    void deleteById(Long optionId);

    List<QuestionOption> saveAll(List<QuestionOption> questionOptions);
}
