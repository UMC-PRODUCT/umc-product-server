package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.application.port.out.SaveSingleAnswerPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SingleAnswerPersistenceAdapter implements SaveSingleAnswerPort {

    private final SingleAnswerJpaRepository singleAnswerJpaRepository;

    @Override
    public void deleteAllByFormResponseIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        singleAnswerJpaRepository.deleteAllByFormResponseIds(ids);
    }
}
