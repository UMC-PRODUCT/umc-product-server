package com.umc.product.survey.application.port.out;

public interface SaveQuestionPort {
    void deleteByFormIdAndQuestionId(Long formId, Long questionId);

    void deleteById(Long questionId);
}
