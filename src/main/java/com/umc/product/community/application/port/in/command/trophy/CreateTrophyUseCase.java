package com.umc.product.community.application.port.in.command.trophy;

import com.umc.product.community.application.port.in.command.trophy.dto.CreateTrophyCommand;
import com.umc.product.community.application.port.in.query.dto.TrophyInfo;

public interface CreateTrophyUseCase {
    TrophyInfo createTrophy(CreateTrophyCommand command);
}
