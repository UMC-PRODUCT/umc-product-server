package com.umc.product.community.application.service.command;

import com.umc.product.community.application.port.in.command.trophy.CreateTrophyUseCase;
import com.umc.product.community.application.port.in.command.trophy.dto.CreateTrophyCommand;
import com.umc.product.community.application.port.in.query.dto.TrophyInfo;
import com.umc.product.community.application.port.out.trophy.SaveTrophyPort;
import com.umc.product.community.domain.Trophy;
import com.umc.product.community.domain.Trophy.ChallengerId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TrophyCommandService implements CreateTrophyUseCase {

    private final SaveTrophyPort saveTrophyPort;

    @Override
    public TrophyInfo createTrophy(CreateTrophyCommand command) {
        Trophy trophy = Trophy.create(
            command.week(),
            new ChallengerId(command.challengerId()),
            command.title(),
            command.content(),
            command.url()
        );

        Trophy savedTrophy = saveTrophyPort.save(trophy);

        return TrophyInfo.from(savedTrophy);
    }
}
