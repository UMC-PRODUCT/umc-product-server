package com.umc.product.global.seed;

import com.umc.product.member.application.port.in.command.RegisterIdPwMemberUseCase;
import com.umc.product.member.application.port.out.LoadMemberPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * alpha 환경 부팅 시점에 더미 회원을 자동 시딩한다. ADR-007 참조.
 * <p>
 * profile=alpha + app.seed.alpha.enabled=true 두 조건을 모두 만족할 때만 빈으로 등록되어 동작한다.
 * 둘 중 하나라도 빠지면 본 클래스 자체가 컴포넌트 스캔 대상에서 제외된다 — 운영 환경에 시딩이
 * 우발적으로 트리거될 가능성을 빈 등록 단계에서 차단한다.
 * <p>
 * 부팅 시점에 회원 수가 이미 임계값을 넘으면 시딩을 건너뛴다 (멱등성).
 * 시딩 중 발생하는 예외는 ERROR 로그로 남기고 swallow 한다 — 시딩 실패가 서버 부팅을 막지
 * 않도록 하기 위함이다.
 */
@Slf4j
@Component
@Profile("alpha")
@ConditionalOnProperty(prefix = "app.seed.alpha", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class AlphaSeedRunner implements ApplicationRunner {

    private final AlphaSeedProperties properties;
    private final LoadMemberPort loadMemberPort;
    private final RegisterIdPwMemberUseCase registerIdPwMemberUseCase;
    private final AlphaDummyMemberFactory dummyMemberFactory;

    @Override
    public void run(ApplicationArguments args) {
        long startedAt = System.currentTimeMillis();
        long currentMemberCount = loadMemberPort.countAllMembers();

        if (currentMemberCount > properties.skipIfMemberCountGreaterThan()) {
            log.info(
                "alpha seed skipped: current member count {} > threshold {}",
                currentMemberCount, properties.skipIfMemberCountGreaterThan()
            );
            return;
        }

        log.info(
            "alpha seed start: idPw={}, oauth={}, currentMemberCount={}",
            properties.idPwMemberCount(), properties.oauthMemberCount(), currentMemberCount
        );

        int idPwCreated = seedIdPwMembers();

        long elapsedMs = System.currentTimeMillis() - startedAt;
        log.info("alpha seed completed in {}ms (idPw={})", elapsedMs, idPwCreated);
    }

    /**
     * ID/PW 회원을 properties.idPwMemberCount() 만큼 등록한다.
     * 각 register 호출은 자체 트랜잭션이며, 한 회원 실패가 다른 회원 등록을 막지 않는다.
     */
    private int seedIdPwMembers() {
        int created = 0;
        int target = properties.idPwMemberCount();
        for (int seq = 1; seq <= target; seq++) {
            try {
                registerIdPwMemberUseCase.register(dummyMemberFactory.nextIdPwCommand(seq));
                created++;
            } catch (Exception e) {
                log.error("alpha seed failed at idPw seq {}: {}", seq, e.toString());
            }
        }
        return created;
    }
}
