package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.application.port.out.SaveQuestionOptionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionOptionPersistenceAdapter implements SaveQuestionOptionPort {

    private final QuestionOptionJpaRepository questionOptionJpaRepository;

    @Override
    public void deleteAllByQuestionId(Long questionId) {
        questionOptionJpaRepository.deleteAllByQuestionId(questionId);
    }
}
