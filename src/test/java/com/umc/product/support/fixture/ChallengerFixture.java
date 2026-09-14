package com.umc.product.support.fixture;

import static com.umc.product.support.CommonFixture.MONKEY;

import java.util.List;

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
        Challenger challenger = MONKEY.giveMeBuilder(Challenger.class)
            .set("memberId", memberId)
            .set("part", part)
            // 파트 기반 챌린저이므로 tracks는 비워둔다. (defaultNotNull로 랜덤 다중 트랙이 채워지면
            // Challenger 생성자의 트랙 조합 검증(기본 1개 + infra)에 걸려 Builder introspector가 실패한다.)
            .set("tracks", List.of())
            .set("gisuId", gisuId)
            .sample();
        return saveChallengerPort.save(challenger);
    }

    public Challenger 웹(Long memberId, Long gisuId) {
        return saveChallengerPort.save(new Challenger(memberId, ChallengerPart.WEB, gisuId));
    }

    public Challenger 스프링(Long memberId, Long gisuId) {
        return saveChallengerPort.save(new Challenger(memberId, ChallengerPart.SPRINGBOOT, gisuId));
    }
}
