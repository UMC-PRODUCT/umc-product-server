package com.umc.product.support.fixture;

import static com.umc.product.support.CommonFixture.MONKEY;

import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import org.springframework.stereotype.Component;

@Component
public class ChallengerFixture extends FixtureSupport {

    private final SaveChallengerPort saveChallengerPort;

    public ChallengerFixture(SaveChallengerPort saveChallengerPort) {
        this.saveChallengerPort = saveChallengerPort;
    }

    public Challenger normal(Long memberId, ChallengerPart part, Long gisuId) {
        Challenger challenger = MONKEY.giveMeBuilder(Challenger.class)
            .set("memberId", memberId)
            .set("part", part)
            .set("gisuId", gisuId)
            .sample();
        return saveChallengerPort.save(challenger);
    }

    public Challenger web(Long memberId, Long gisuId) {
        return saveChallengerPort.save(new Challenger(memberId, ChallengerPart.WEB, gisuId));
    }

    public Challenger springBoot(Long memberId, Long gisuId) {
        return saveChallengerPort.save(new Challenger(memberId, ChallengerPart.SPRINGBOOT, gisuId));
    }
}
