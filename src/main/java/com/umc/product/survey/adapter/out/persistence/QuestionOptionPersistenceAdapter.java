package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.application.port.out.LoadQuestionOptionPort;
import com.umc.product.survey.application.port.out.SaveQuestionOptionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionOptionPersistenceAdapter implements SaveQuestionOptionPort, LoadQuestionOptionPort {

    private final QuestionOptionJpaRepository questionOptionJpaRepository;

    @Override
    public void deleteAllByQuestionId(Long questionId) {
        questionOptionJpaRepository.deleteAllByQuestionId(questionId);
    }

    @Override
    public boolean existsByIdAndQuestionId(Long optionId, Long questionId) {
        if (optionId == null || questionId == null) {
            return false;
        }
        return questionOptionJpaRepository.existsByIdAndQuestion_Id(optionId, questionId);
    }

    @Override
    public void deleteById(Long optionId) {
        questionOptionJpaRepository.deleteById(optionId);
    }
}
