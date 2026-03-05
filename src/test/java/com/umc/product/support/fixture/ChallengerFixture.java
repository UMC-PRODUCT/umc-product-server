package com.umc.product.support.fixture;

import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import org.springframework.stereotype.Component;

@Component
public class ChallengerFixture {

    private final SaveChallengerPort saveChallengerPort;

    public ChallengerFixture(SaveChallengerPort saveChallengerPort) {
        this.saveChallengerPort = saveChallengerPort;
    }

    public Challenger 챌린저(Long memberId, ChallengerPart part, Long gisuId) {
        return saveChallengerPort.save(new Challenger(memberId, part, gisuId));
    }

    public Challenger 웹_챌린저(Long memberId, Long gisuId) {
        return saveChallengerPort.save(new Challenger(memberId, ChallengerPart.WEB, gisuId));
    }

    public Challenger 스프링_챌린저(Long memberId, Long gisuId) {
        return saveChallengerPort.save(new Challenger(memberId, ChallengerPart.SPRINGBOOT, gisuId));
    }
}
