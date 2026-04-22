package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.application.port.out.SaveSingleAnswerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Deprecated
public class LegacySingleAnswerPersistenceAdapter implements SaveSingleAnswerPort {

    private final LegacySingleAnswerJpaRepository legacySingleAnswerJpaRepository;

    @Override
    public void deleteAllByFormResponseIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        legacySingleAnswerJpaRepository.deleteAllByFormResponseIds(ids);
    }
}
