package com.umc.product.support.fixture;

import com.umc.product.term.application.port.out.SaveTermConsentPort;
import com.umc.product.term.application.port.out.SaveTermPort;
import com.umc.product.term.domain.Term;
import com.umc.product.term.domain.TermConsent;
import com.umc.product.term.domain.enums.TermType;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * 약관(Term) / 약관 동의(TermConsent) 테스트용 Fixture.
 *
 * <p>도메인 규칙(필수/선택, 활성/비활성)을 깨지 않는 유효 기본값을 제공한다. 영속화는 SavePort 를 통해 수행된다.</p>
 */
@Component
public class TermFixture extends FixtureSupport {

    private static final Instant DEFAULT_AGREED_AT = Instant.parse("2024-03-01T00:00:00Z");

    private final SaveTermPort saveTermPort;
    private final SaveTermConsentPort saveTermConsentPort;

    public TermFixture(SaveTermPort saveTermPort, SaveTermConsentPort saveTermConsentPort) {
        this.saveTermPort = saveTermPort;
        this.saveTermConsentPort = saveTermConsentPort;
    }

    /**
     * 필수 동의 약관(현재 활성).
     */
    public Term 필수_약관(TermType type) {
        return saveTermPort.save(Term.builder()
            .type(type)
            .link(fixtureUrl("terms/" + type.name().toLowerCase()))
            .required(true)
            .build());
    }

    /**
     * 선택 동의 약관(현재 활성).
     */
    public Term 선택_약관(TermType type) {
        return saveTermPort.save(Term.builder()
            .type(type)
            .link(fixtureUrl("terms/" + type.name().toLowerCase()))
            .required(false)
            .build());
    }

    /**
     * 회원이 특정 타입의 약관에 동의한 상태(고정 시각).
     */
    public TermConsent 약관_동의(Long memberId, TermType type) {
        return saveTermConsentPort.save(TermConsent.builder()
            .memberId(memberId)
            .termType(type)
            .agreedAt(DEFAULT_AGREED_AT)
            .build());
    }
}
