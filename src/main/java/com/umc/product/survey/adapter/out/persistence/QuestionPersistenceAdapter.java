package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.survey.application.port.out.LoadQuestionPort;
import com.umc.product.survey.application.port.out.SaveQuestionPort;
import com.umc.product.survey.domain.Question;
import com.umc.product.survey.domain.enums.QuestionType;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionPersistenceAdapter implements SaveQuestionPort, LoadQuestionPort {
    private final QuestionJpaRepository questionJpaRepository;

    @Override
    public void deleteByFormIdAndQuestionId(Long formId, Long questionId) {
        int deleted = questionJpaRepository.deleteByFormIdAndQuestionId(formId, questionId);
        if (deleted == 0) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.QUESTION_NOT_FOUND);
        }
    }

    @Override
    public List<Question> findAllByFormSectionIdIn(Set<Long> formSectionIds) {
        return questionJpaRepository.findAllByFormSectionIdIn(formSectionIds);
    }

    @Override
    public boolean existsByIdAndFormId(Long questionId, Long formId) {
        return questionJpaRepository.existsByIdAndFormSection_Form_Id(questionId, formId);
    }

    @Override
    public void deleteById(Long questionId) {
        questionJpaRepository.deleteById(questionId);
    }

    @Override
    public Optional<Question> findById(Long questionId) {
        return questionJpaRepository.findById(questionId);
    }

    @Override
    public Optional<Question> findFirstByFormIdAndType(Long formId, QuestionType type) {
        return questionJpaRepository.findFirstByFormIdAndType(formId, type);
    }
}
