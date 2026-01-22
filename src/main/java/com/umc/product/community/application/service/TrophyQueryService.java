package com.umc.product.community.application.service;

import com.umc.product.community.application.port.in.trophy.TrophyInfo;
import com.umc.product.community.application.port.in.trophy.query.GetTrophyListUseCase;
import com.umc.product.community.application.port.in.trophy.query.TrophySearchQuery;
import com.umc.product.community.application.port.out.LoadTrophyPort;
import com.umc.product.community.domain.Trophy;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrophyQueryService implements GetTrophyListUseCase {

    private final LoadTrophyPort loadTrophyPort;

    @Override
    public List<TrophyInfo> getTrophies(TrophySearchQuery query) {
        List<Trophy> trophies = loadTrophyPort.findAllByQuery(query);

        // TODO: challengerName, school, part는 Challenger/Organization 도메인에서 조회 필요
        return trophies.stream()
                .map(trophy -> new TrophyInfo(
                        trophy.getTrophyId() != null ? trophy.getTrophyId().id() : null,
                        trophy.getWeek(),
                        null,
                        null,
                        null,
                        trophy.getTitle(),
                        trophy.getContent(),
                        trophy.getUrl()
                ))
                .toList();
    }
}
