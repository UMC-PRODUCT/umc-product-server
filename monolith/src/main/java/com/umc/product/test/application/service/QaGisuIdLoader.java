package com.umc.product.test.application.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;

import lombok.RequiredArgsConstructor;

@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class QaGisuIdLoader {

    private final GetGisuUseCase getGisuUseCase;

    public Map<Integer, Long> load(Collection<Integer> generations) {
        Map<Integer, Long> gisuIds = new HashMap<>();
        for (GisuInfo gisu : getGisuUseCase.getList()) {
            int generation = gisu.generation().intValue();
            if (generations.contains(generation) && gisuIds.put(generation, gisu.gisuId()) != null) {
                throw new IllegalStateException("QA 시딩 기준 기수가 중복됐습니다: " + gisu.generation() + "기");
            }
        }
        return Map.copyOf(gisuIds);
    }

    public Optional<Long> find(int generation) {
        return Optional.ofNullable(load(List.of(generation)).get(generation));
    }
}
