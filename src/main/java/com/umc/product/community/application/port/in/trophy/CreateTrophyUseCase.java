package com.umc.product.community.application.port.in.trophy;

public interface CreateTrophyUseCase {
    TrophyInfo createTrophy(CreateTrophyCommand command);
}
