package com.umc.product.community.application.port.in.query;

import com.umc.product.community.application.port.in.query.dto.TrophyInfo;
import com.umc.product.community.application.port.in.query.dto.TrophySearchQuery;
import java.util.List;

public interface GetTrophyListUseCase {
    List<TrophyInfo> getTrophies(TrophySearchQuery query);
}
