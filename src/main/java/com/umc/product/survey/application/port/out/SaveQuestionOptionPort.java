package com.umc.product.survey.application.port.out;

public interface SaveQuestionOptionPort {
    void deleteAllByQuestionId(Long questionId);

    void deleteById(Long optionId);
}
