package com.umc.product.test.application.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.member.application.port.in.command.RegisterEmailMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.EmailRegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.TermConsents;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.test.application.port.in.command.CreateSeedMemberUseCase;
import com.umc.product.test.application.port.in.command.SeedMembersUseCase;
import com.umc.product.test.application.port.in.command.dto.CreateSeedMemberCommand;
import com.umc.product.test.application.port.in.command.dto.CreateSeedMemberResult;
import com.umc.product.test.application.port.in.command.dto.SeedMembersCommand;
import com.umc.product.test.application.port.in.command.dto.SeedMembersResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 멤버 시딩 서비스. ADR-017 참조.
 * <p>
 * Hexagonal 원칙을 따라 다른 도메인의 UseCase 만 호출하고 Port 는 직접 사용하지 않는다.
 * <p>
 * <b>트랜잭션 정책</b>: {@code seed()} 메서드는 {@link Propagation#NOT_SUPPORTED} 로 외부 트랜잭션을
 * 차단해 내부의 {@code batchRegister} 호출이 한 개의 새 트랜잭션으로 시작되도록 한다. batch 는
 * atomic 이므로 한 건 실패 시 전체 롤백 — 시딩 시퀀스 오프셋으로 unique 충돌을 회피한 상황에서
 * commit 1회 + 1차 캐시 효과를 얻기 위한 의도된 trade-off 다. (이전에는 per-call 트랜잭션으로
 * 실패 격리를 우선했으나, batch UseCase 도입으로 정책을 바꿨다.)
 */
@Slf4j
@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class MemberSeedService implements SeedMembersUseCase, CreateSeedMemberUseCase {

    private final SeedProperties properties;
    private final DummyMemberFactory dummyMemberFactory;
    private final GetMemberUseCase getMemberUseCase;
    private final RegisterEmailMemberUseCase registerEmailMemberUseCase;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
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
            "member seed start: count={}, currentMemberCount={}, force={}",
            command.count(), currentMemberCount, command.force()
        );

        int created = seedEmailMembers(command.count(), currentMemberCount);

        long elapsedMs = System.currentTimeMillis() - startedAt;
        log.info("member seed completed in {}ms (created={})", elapsedMs, created);

        return SeedMembersResult.of(created);
    }

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

    /**
     * 이메일 회원을 target 만큼 등록한다. 약관 동의는 시딩 시작 시 1 회만 조회해 모든 Command 에
     * 재사용한다. 모든 Command 를 미리 만들어 {@link RegisterEmailMemberUseCase#batchRegister}
     * 1 회 호출로 전달한다 — 한 트랜잭션 안에서 N 건이 처리되므로 commit/1차 캐시 효과를 얻는다.
     * <p>
     * batch 는 atomic 이라 한 건 실패 시 전체 롤백된다. 시딩 시퀀스는 현재 회원 수 + 1 부터
     * 시작해 기존 회원과의 email unique 충돌을 회피한다.
     */
    private int seedEmailMembers(int target, long currentMemberCount) {
        if (target <= 0) {
            return 0;
        }
        List<TermConsents> consents = dummyMemberFactory.snapshotMandatoryConsents();
        long sequenceStart = currentMemberCount + 1;
        List<EmailRegisterMemberCommand> commands = new ArrayList<>(target);
        for (int i = 0; i < target; i++) {
            commands.add(dummyMemberFactory.nextEmailCommand(sequenceStart + i, consents));
        }
        try {
            return registerEmailMemberUseCase.batchRegister(commands).size();
        } catch (Exception e) {
            log.error("member seed batchRegister failed (target={}): {}", target, e.toString());
            return 0;
        }
    }

    private String resolveRawPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            return properties.defaultPassword();
        }
        return rawPassword;
    }
}
