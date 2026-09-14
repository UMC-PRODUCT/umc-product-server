package com.umc.product.test.application.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authorization.application.port.in.command.ManageChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.command.dto.CreateChallengerRoleCommand;
import com.umc.product.test.application.port.in.command.CreateSeedChallengerRoleUseCase;
import com.umc.product.test.application.port.in.command.dto.CreateSeedChallengerRoleCommand;
import com.umc.product.test.application.port.in.command.dto.CreateSeedChallengerRoleResult;

import lombok.RequiredArgsConstructor;

@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class ChallengerRoleSeedService implements CreateSeedChallengerRoleUseCase {

    private final ManageChallengerRoleUseCase manageChallengerRoleUseCase;

    @Override
    @Transactional
    public CreateSeedChallengerRoleResult create(CreateSeedChallengerRoleCommand command) {
        Long challengerRoleId = manageChallengerRoleUseCase.createChallengerRole(CreateChallengerRoleCommand.builder()
            .challengerId(command.challengerId())
            .roleType(command.roleType())
            .organizationId(command.organizationId())
            .responsiblePart(command.responsiblePart())
            .gisuId(command.gisuId())
            .build());

        return CreateSeedChallengerRoleResult.of(
            challengerRoleId,
            command.challengerId(),
            command.roleType(),
            command.organizationId(),
            command.responsiblePart(),
            command.gisuId()
        );
    }
}
