package com.umc.product.community.application.port.in.trophy.query;

import com.umc.product.community.application.port.in.trophy.TrophyInfo;
import java.util.List;

public interface GetTrophyListUseCase {
    List<TrophyInfo> getTrophies(TrophySearchQuery query);
}
