package com.umc.product.test.application.service;

import com.umc.product.member.application.port.in.command.RegisterIdPwMemberUseCase;
import com.umc.product.member.application.port.in.command.RegisterOAuthMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.OAuthRegisterMemberCommand;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.test.application.port.in.command.SeedMembersUseCase;
import com.umc.product.test.application.port.in.command.dto.SeedMembersCommand;
import com.umc.product.test.application.port.in.command.dto.SeedMembersResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * 멤버 시딩 서비스. ADR-017 참조.
 * <p>
 * Hexagonal 원칙을 따라 다른 도메인의 UseCase 만 호출하고 Port 는 직접 사용하지 않는다.
 * ID/PW 시딩은 per-call 트랜잭션(실패 격리)이고, OAuth 시딩은 batchRegister 단일 트랜잭션이다 —
 * 기존 alpha 시딩의 트랜잭션 정책을 그대로 유지한다.
 */
@Slf4j
@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class MemberSeedService implements SeedMembersUseCase {

    private final SeedProperties properties;
    private final DummyMemberFactory dummyMemberFactory;
    private final GetMemberUseCase getMemberUseCase;
    private final RegisterIdPwMemberUseCase registerIdPwMemberUseCase;
    private final RegisterOAuthMemberUseCase registerOAuthMemberUseCase;

    @Override
    public SeedMembersResult seed(SeedMembersCommand command) {
        long currentMemberCount = getMemberUseCase.countAll();

        if (!command.force() && currentMemberCount > properties.skipIfMemberCountGreaterThan()) {
            String reason = "current member count %d > threshold %d".formatted(
                currentMemberCount, properties.skipIfMemberCountGreaterThan()
            );
            log.info("member seed skipped: {}", reason);
            return SeedMembersResult.skipped(reason);
        }

        long startedAt = System.currentTimeMillis();
        log.info(
            "member seed start: idPw={}, oauth={}, currentMemberCount={}, force={}",
            command.idPwCount(), command.oauthCount(), currentMemberCount, command.force()
        );

        int idPwCreated = seedIdPwMembers(command.idPwCount(), currentMemberCount);
        int oauthCreated = seedOAuthMembers(command.oauthCount());

        long elapsedMs = System.currentTimeMillis() - startedAt;
        log.info("member seed completed in {}ms (idPw={}, oauth={})", elapsedMs, idPwCreated, oauthCreated);

        return SeedMembersResult.of(idPwCreated, oauthCreated);
    }

    /**
     * ID/PW 회원을 target 만큼 등록한다. 각 register 호출은 자체 트랜잭션이며, 한 회원 실패가
     * 다른 회원 등록을 막지 않는다. loginId 시퀀스는 현재 회원 수 + 1 부터 시작해 기존 회원과의
     * 충돌을 피한다.
     */
    private int seedIdPwMembers(int target, long currentMemberCount) {
        if (target <= 0) {
            return 0;
        }
        int created = 0;
        int sequenceStart = Math.toIntExact(currentMemberCount) + 1;
        for (int i = 0; i < target; i++) {
            int seq = sequenceStart + i;
            try {
                registerIdPwMemberUseCase.register(dummyMemberFactory.nextIdPwCommand(seq));
                created++;
            } catch (Exception e) {
                log.error("member seed failed at idPw seq {}: {}", seq, e.toString());
            }
        }
        return created;
    }

    /**
     * OAuth 회원을 target 만큼 batchRegister 로 일괄 등록한다. batchRegister 는 단일 트랜잭션이므로
     * 한 명이라도 실패하면 전체 롤백된다 — 의도된 보수 동작.
     */
    private int seedOAuthMembers(int target) {
        if (target <= 0) {
            return 0;
        }
        try {
            List<OAuthRegisterMemberCommand> commands = dummyMemberFactory.nextOAuthCommands(target);
            List<Long> registeredIds = registerOAuthMemberUseCase.batchRegister(commands);
            return registeredIds.size();
        } catch (Exception e) {
            log.error("member seed failed at oauth batch ({} members): {}", target, e.toString());
            return 0;
        }
    }
}
