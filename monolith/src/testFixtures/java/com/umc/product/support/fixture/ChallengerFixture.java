package com.umc.product.support.fixture;

import org.springframework.stereotype.Component;

import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;

@Component
public class ChallengerFixture extends FixtureSupport {

    private final SaveChallengerPort saveChallengerPort;

    public ChallengerFixture(SaveChallengerPort saveChallengerPort) {
        this.saveChallengerPort = saveChallengerPort;
    }

    public Challenger 챌린저(Long memberId, ChallengerPart part, Long gisuId) {
        Challenger challenger = new Challenger(memberId, part, gisuId);
        return saveChallengerPort.save(challenger);
    }

    public Challenger 웹(Long memberId, Long gisuId) {
        return saveChallengerPort.save(new Challenger(memberId, ChallengerPart.WEB, gisuId));
    }

    public Challenger 스프링(Long memberId, Long gisuId) {
        return saveChallengerPort.save(new Challenger(memberId, ChallengerPart.SPRINGBOOT, gisuId));
    }
}
