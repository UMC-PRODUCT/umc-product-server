package com.umc.product.test.application.service;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.member.application.port.in.command.RegisterEmailMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.EmailRegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.TermConsents;
import com.umc.product.test.application.port.in.command.CreateSeedMemberUseCase;
import com.umc.product.test.application.port.in.command.dto.CreateSeedMemberCommand;
import com.umc.product.test.application.port.in.command.dto.CreateSeedMemberResult;

import lombok.RequiredArgsConstructor;

/**
 * ADR-017 참조.
 * <p>
 * Hexagonal 원칙을 따라 다른 도메인의 UseCase 만 호출하고 Port 는 직접 사용하지 않는다.
 */
@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class MemberSeedService implements CreateSeedMemberUseCase {

    private final SeedProperties properties;
    private final DummyMemberFactory dummyMemberFactory;
    private final RegisterEmailMemberUseCase registerEmailMemberUseCase;

    @Override
    @Transactional
    public CreateSeedMemberResult create(CreateSeedMemberCommand command) {
        List<TermConsents> consents = dummyMemberFactory.snapshotMandatoryConsents();
        Long memberId = registerEmailMemberUseCase.register(EmailRegisterMemberCommand.builder()
            .rawPassword(resolveRawPassword(command.rawPassword()))
            .name(command.name())
            .nickname(command.nickname())
            .email(command.email())
            .schoolId(command.schoolId())
            .termConsents(consents)
            .build());

        return CreateSeedMemberResult.of(memberId, command.email());
    }

    private String resolveRawPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            return properties.defaultPassword();
        }
        return rawPassword;
    }
}
