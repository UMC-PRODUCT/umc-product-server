package com.umc.product.community.application.service;

import com.umc.product.community.application.port.in.trophy.Query.GetTrophyListUseCase;
import com.umc.product.community.application.port.in.trophy.Query.TrophySearchQuery;
import com.umc.product.community.application.port.in.trophy.TrophyInfo;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrophyQueryService implements GetTrophyListUseCase {

    @Override
    public List<TrophyInfo> getTrophies(TrophySearchQuery query) {
        // TODO: 구현 필요
        return Collections.emptyList();
    }
}
