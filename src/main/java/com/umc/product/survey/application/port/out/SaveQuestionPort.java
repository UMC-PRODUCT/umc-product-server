package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.Question;

public interface SaveQuestionPort {
    void deleteByFormIdAndQuestionId(Long formId, Long questionId);

    void deleteById(Long questionId);

    Question save(Question question);
}
