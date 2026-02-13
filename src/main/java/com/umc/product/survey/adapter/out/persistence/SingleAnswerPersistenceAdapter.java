package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.application.port.out.LoadSingleAnswerPort;
import com.umc.product.survey.application.port.out.SaveSingleAnswerPort;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SingleAnswerPersistenceAdapter implements SaveSingleAnswerPort, LoadSingleAnswerPort {

    private final SingleAnswerJpaRepository singleAnswerJpaRepository;
    private final SingleAnswerQueryRepository singleAnswerQueryRepository;

    @Override
    public void deleteAllByFormResponseIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        singleAnswerJpaRepository.deleteAllByFormResponseIds(ids);
    }

    @Override
    public Map<Long, Map<String, Object>> findScheduleValuesByFormResponseIds(List<Long> formResponseIds) {
        return singleAnswerQueryRepository.findScheduleValuesByFormResponseIds(formResponseIds);
    }

    @Override
    public Map<Long, Integer> countVotesByOptionId(Long formId) {
        List<Object[]> rows = singleAnswerJpaRepository.countVotesByOptionId(formId);

        Map<Long, Integer> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Long optionId = ((Number) row[0]).longValue();
            Integer cnt = ((Number) row[1]).intValue();
            result.put(optionId, cnt);
        }
        return result;
    }
}
