package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.application.port.out.LoadQuestionOptionPort;
import com.umc.product.survey.application.port.out.SaveQuestionOptionPort;
import com.umc.product.survey.domain.QuestionOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class QuestionOptionPersistenceAdapter implements SaveQuestionOptionPort, LoadQuestionOptionPort {

    private final QuestionOptionJpaRepository questionOptionJpaRepository;
    private final QuestionOptionQueryRepository questionOptionQueryRepository;

    @Override
    public QuestionOption save(QuestionOption option) {
        return questionOptionJpaRepository.save(option);
    }

    @Override
    public List<QuestionOption> saveAll(List<QuestionOption> questionOptions) {
        return questionOptionJpaRepository.saveAll(questionOptions);
    }

    @Override
    public void deleteById(Long optionId) {
        questionOptionJpaRepository.deleteById(optionId);
    }

    @Override
    public void deleteAllByQuestionId(Long questionId) {
        questionOptionJpaRepository.deleteAllByQuestionId(questionId);
    }

    @Override
    public void deleteByFormId(Long formId) {
        questionOptionJpaRepository.deleteByFormId(formId);
    }

    @Override
    public void deleteBySectionId(Long sectionId) {
        questionOptionJpaRepository.deleteBySectionId(sectionId);
    }

    @Override
    public Optional<QuestionOption> findById(Long optionId) {
        return questionOptionJpaRepository.findById(optionId);
    }

    @Override
    public boolean existsByIdAndQuestionId(Long optionId, Long questionId) {
        if (optionId == null || questionId == null) {
            return false;
        }
        return questionOptionJpaRepository.existsByIdAndQuestion_Id(optionId, questionId);
    }

    @Override
    public List<QuestionOption> listByQuestionId(Long questionId) {
        return questionOptionQueryRepository.findAllByQuestionId(questionId);
    }
}
