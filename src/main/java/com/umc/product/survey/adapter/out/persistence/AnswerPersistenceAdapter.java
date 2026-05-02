package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.application.port.out.LoadAnswerPort;
import com.umc.product.survey.application.port.out.SaveAnswerPort;
import com.umc.product.survey.domain.Answer;
import com.umc.product.survey.domain.AnswerChoice;
import com.umc.product.survey.domain.enums.FormResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AnswerPersistenceAdapter implements LoadAnswerPort, SaveAnswerPort {

    private final FormResponseJpaRepository formResponseJpaRepository;
    private final AnswerChoiceJpaRepository answerChoiceJpaRepository;
    private final AnswerChoiceQueryRepository answerChoiceQueryRepository;
    private final AnswerJpaRepository answerJpaRepository;
    private final AnswerQueryRepository answerQueryRepository;

    @Override
    public Optional<Answer> findById(Long answerId) {
        return answerJpaRepository.findById(answerId);
    }

    @Override
    public boolean existsByFormResponseIdAndQuestionId(Long formResponseId, Long questionId) {
        return answerQueryRepository.existsByFormResponseIdAndQuestionId(formResponseId, questionId);
    }

    @Override
    public List<Answer> listByFormResponseId(Long formResponseId) {
        return answerQueryRepository.findAllByFormResponseId(formResponseId);
    }

    @Override
    public List<AnswerChoice> listChoicesByAnswerIdIn(Set<Long> answerIds) {
        return answerChoiceQueryRepository.findAllByAnswerIdIn(answerIds);
    }

    @Override
    public long countTotalParticipants(Long formId) {
        return formResponseJpaRepository.countByFormIdAndStatus(formId, FormResponseStatus.SUBMITTED);
    }

    @Override
    public Map<Long, Long> countVotesByOptionId(Long formId) {
        return answerChoiceQueryRepository.countVotesByOptionId(formId);
    }

    @Override
    public List<Long> findSelectedOptionIdsByMember(Long formId, Long memberId) {
        return answerChoiceJpaRepository.findSelectedOptionIdsByMember(formId, memberId);
    }

    @Override
    public Map<Long, List<Long>> findSelectedMemberIdsByOptionId(Long formId) {
        return answerChoiceQueryRepository.findSelectedMemberIdsByOptionId(formId);
    }

    @Override
    public Answer save(Answer answer) {
        return answerJpaRepository.save(answer);
    }

    @Override
    public List<Answer> saveAll(List<Answer> answers) {
        return answerJpaRepository.saveAll(answers);
    }

    @Override
    public List<AnswerChoice> saveAllChoices(List<AnswerChoice> choices) {
        return answerChoiceJpaRepository.saveAll(choices);
    }

    @Override
    public void deleteAllByFormResponseId(Long formResponseId) {
        answerChoiceJpaRepository.deleteAllByFormResponseId(formResponseId);
        answerJpaRepository.deleteAllByFormResponseId(formResponseId);
    }

    @Override
    public void deleteByFormId(Long formId) {
        // FK 의존성 거꾸로: AnswerChoice -> Answer 순으로 삭제
        answerChoiceJpaRepository.deleteByFormId(formId);
        answerJpaRepository.deleteByFormId(formId);
    }

    @Override
    public void deleteByQuestionId(Long questionId) {
        // FK 의존성 거꾸로: AnswerChoice -> Answer 순으로 삭제
        answerChoiceJpaRepository.deleteByQuestionId(questionId);
        answerJpaRepository.deleteByQuestionId(questionId);
    }

    @Override
    public void deleteByAnswerId(Long answerId) {
        // FK 의존성 거꾸로: AnswerChoice (bulk @Query) -> Answer (단건 deleteById) 순
        answerChoiceJpaRepository.deleteByAnswerId(answerId);
        answerJpaRepository.deleteById(answerId);
    }

    @Override
    public void deleteChoicesByAnswerId(Long answerId) {
        answerChoiceJpaRepository.deleteByAnswerId(answerId);
    }
}
