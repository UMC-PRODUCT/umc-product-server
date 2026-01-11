package com.umc.product.community.application.port.in.trophy;

import java.util.List;

public interface TrophyUseCase {
    List<TrophyInfo> getTrophies(TrophySearchQuery query);

    TrophyInfo createTrophy(CreateTrophyCommand command);
}
