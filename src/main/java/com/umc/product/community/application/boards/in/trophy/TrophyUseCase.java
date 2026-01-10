package com.umc.product.community.application.boards.in.trophy;

import java.util.List;

public interface TrophyUseCase {
    List<TrophyResponse> getTrophies(TrophySearchQuery query);

    TrophyResponse createTrophy(CreateTrophyCommand command);
}
