package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.application.port.out.LoadAnswerPort;
import com.umc.product.survey.application.port.out.SaveAnswerPort;
import com.umc.product.survey.domain.Answer;
import com.umc.product.survey.domain.enums.FormResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AnswerPersistenceAdapter implements LoadAnswerPort, SaveAnswerPort {

    private final FormResponseJpaRepository formResponseJpaRepository;
    private final AnswerChoiceJpaRepository answerChoiceJpaRepository;
    private final AnswerJpaRepository answerJpaRepository;

    @Override
    public int countTotalParticipants(Long formId) {
        return formResponseJpaRepository.countByFormIdAndStatus(formId, FormResponseStatus.SUBMITTED);
    }

    @Override
    public Map<Long, Long> countVotesByOptionId(Long formId) {
        List<Object[]> rawResults = answerChoiceJpaRepository.countVotesByOptionIdRaw(formId);
        Map<Long, Long> optionVoteCounts = new HashMap<>();
        
        for (Object[] result : rawResults) {
            Long optionId = ((Number) result[0]).longValue();
            Long voteCount = ((Number) result[1]).longValue();
            optionVoteCounts.put(optionId, voteCount);
        }
        
        return optionVoteCounts;
    }

    @Override
    public List<Long> findSelectedOptionIdsByMember(Long formId, Long memberId) {
        return answerChoiceJpaRepository.findSelectedOptionIdsByMember(formId, memberId);
    }

    @Override
    public Map<Long, List<Long>> findSelectedMemberIdsByOptionId(Long formId) {
        List<Object[]> rawResults = answerChoiceJpaRepository.findSelectedMemberIdsByOptionIdRaw(formId);
        Map<Long, List<Long>> result = new HashMap<>();

        for (Object[] row : rawResults) {
            Long optionId = ((Number) row[0]).longValue();
            Long memberId = ((Number) row[1]).longValue();
            result.computeIfAbsent(optionId, k -> new java.util.ArrayList<>()).add(memberId);
        }

        return result;
    }

    @Override
    public List<Answer> saveAll(List<Answer> answers) {
        return answerJpaRepository.saveAll(answers);
    }

    @Override
    public void deleteAllByFormResponseId(Long formResponseId) {
        answerJpaRepository.deleteAllByFormResponseId(formResponseId);
    }
}
