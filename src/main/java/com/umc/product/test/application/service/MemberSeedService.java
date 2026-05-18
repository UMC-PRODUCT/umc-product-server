package com.umc.product.test.application.service;

import com.umc.product.member.application.port.in.command.RegisterEmailMemberUseCase;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.test.application.port.in.command.SeedMembersUseCase;
import com.umc.product.test.application.port.in.command.dto.SeedMembersCommand;
import com.umc.product.test.application.port.in.command.dto.SeedMembersResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 멤버 시딩 서비스. ADR-017 참조.
 * <p>
 * Hexagonal 원칙을 따라 다른 도메인의 UseCase 만 호출하고 Port 는 직접 사용하지 않는다.
 * <p>
 * <b>트랜잭션 정책</b>: {@code seed()} 메서드는 {@link Propagation#NOT_SUPPORTED} 로 외부 트랜잭션을
 * 의도적으로 차단한다. 각 {@code RegisterEmailMemberUseCase.register} 호출이 자체 트랜잭션 경계를 가져
 * 한 회원 등록 실패가 다른 회원 등록을 롤백시키지 않게 하는 것이 목적이다. seed() 자체에 기본
 * {@code @Transactional}을 붙이면 내부 호출이 모두 같은 트랜잭션에 묶여 한 명 실패 시 전체 롤백된다.
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

    /**
     * 이메일 회원을 target 만큼 등록한다. 각 register 호출은 자체 트랜잭션이며, 한 회원 실패가
     * 다른 회원 등록을 막지 않는다. email 시퀀스는 현재 회원 수 + 1 부터 시작해 기존 회원과의
     * 충돌을 피한다.
     */
    private int seedEmailMembers(int target, long currentMemberCount) {
        if (target <= 0) {
            return 0;
        }
        int created = 0;
        long sequenceStart = currentMemberCount + 1;
        for (int i = 0; i < target; i++) {
            long seq = sequenceStart + i;
            try {
                registerEmailMemberUseCase.register(dummyMemberFactory.nextEmailCommand(seq));
                created++;
            } catch (Exception e) {
                log.error("member seed failed at seq {}: {}", seq, e.toString());
            }
        }
        return created;
    }
}
