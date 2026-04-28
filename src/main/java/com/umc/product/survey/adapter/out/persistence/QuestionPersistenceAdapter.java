package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.application.port.out.LoadQuestionPort;
import com.umc.product.survey.application.port.out.SaveQuestionPort;
import com.umc.product.survey.domain.Question;
import com.umc.product.survey.domain.enums.QuestionType;
import com.umc.product.survey.domain.exception.SurveyDomainException;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class QuestionPersistenceAdapter implements SaveQuestionPort, LoadQuestionPort {
    private final QuestionJpaRepository questionJpaRepository;
    private final QuestionQueryRepository questionQueryRepository;

    @Override
    public void deleteByFormIdAndQuestionId(Long formId, Long questionId) {
        int deleted = questionJpaRepository.deleteByFormIdAndQuestionId(formId, questionId);
        if (deleted == 0) {
            throw new SurveyDomainException(SurveyErrorCode.QUESTION_NOT_FOUND);
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
    public void deleteByFormId(Long formId) {
        questionJpaRepository.deleteByFormId(formId);
    }

    @Override
    public void deleteBySectionId(Long sectionId) {
        questionJpaRepository.deleteBySectionId(sectionId);
    }

    @Override
    public Optional<Question> findById(Long questionId) {
        return questionJpaRepository.findById(questionId);
    }

    @Override
    public Optional<Question> findFirstByFormIdAndType(Long formId, QuestionType type) {
        return questionJpaRepository.findFirstByFormIdAndType(formId, type);
    }

    @Override
    public List<Question> listByFormId(Long formId) {
        return questionJpaRepository.findAllByFormId(formId);
    }

    @Override
    public List<Question> listBySectionId(Long sectionId) {
        return questionQueryRepository.findAllBySectionId(sectionId);
    }

    @Override
    public List<Question> listBySectionIdIn(Set<Long> sectionIds) {
        return questionQueryRepository.findAllBySectionIdIn(sectionIds);
    }

    @Override
    public Question save(Question question) {
        return questionJpaRepository.save(question);
    }
}
