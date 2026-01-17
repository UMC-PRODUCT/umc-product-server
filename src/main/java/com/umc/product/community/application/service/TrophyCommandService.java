package com.umc.product.community.application.service;

import com.umc.product.community.application.port.in.trophy.CreateTrophyCommand;
import com.umc.product.community.application.port.in.trophy.CreateTrophyUseCase;
import com.umc.product.community.application.port.in.trophy.TrophyInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TrophyCommandService implements CreateTrophyUseCase {

    @Override
    public TrophyInfo createTrophy(CreateTrophyCommand command) {
        // TODO: 구현 필요
        return null;
    }
}
