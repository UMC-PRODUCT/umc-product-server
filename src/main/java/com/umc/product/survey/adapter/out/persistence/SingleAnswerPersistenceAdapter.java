package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.application.port.out.LoadSingleAnswerPort;
import com.umc.product.survey.application.port.out.SaveSingleAnswerPort;
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
}
