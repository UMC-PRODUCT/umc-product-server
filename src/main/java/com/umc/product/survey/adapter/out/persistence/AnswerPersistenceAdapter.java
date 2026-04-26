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
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AnswerPersistenceAdapter implements LoadAnswerPort, SaveAnswerPort {

    private final FormResponseJpaRepository formResponseJpaRepository;
    private final AnswerChoiceJpaRepository answerChoiceJpaRepository;
    private final AnswerJpaRepository answerJpaRepository;

    @Override
    public long countTotalParticipants(Long formId) {
        return formResponseJpaRepository.countByFormIdAndStatus(formId, FormResponseStatus.SUBMITTED);
    }

    @Override
    public Map<Long, Long> countVotesByOptionId(Long formId) {
        return answerChoiceJpaRepository.countVotesByOptionId(formId).stream()
            .collect(Collectors.toMap(
                projection -> projection.optionId(),
                projection -> projection.voteCount()
            ));
    }

    @Override
    public List<Long> findSelectedOptionIdsByMember(Long formId, Long memberId) {
        return answerChoiceJpaRepository.findSelectedOptionIdsByMember(formId, memberId);
    }

    @Override
    public Map<Long, List<Long>> findSelectedMemberIdsByOptionId(Long formId) {
        return answerChoiceJpaRepository.findSelectedMemberIdsByOptionId(formId).stream()
            .collect(Collectors.groupingBy(
                projection -> projection.optionId(),
                Collectors.mapping(projection -> projection.memberId(), Collectors.toList())
            ));
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
}
