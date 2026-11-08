package com.umc.product.test.application.port.out;

import java.util.List;
import java.util.Optional;

import com.umc.product.test.domain.rehearsal.MatchingSequence;
import com.umc.product.test.domain.rehearsal.RoundTimes;

/**
 * 테스트 전용 SQL로 매칭 차수 시각을 고친다. 1차가 시작되면 운영 API로는 시각을 바꿀 수 없다(PROJECT-0314).
 * 두 메서드는 호출자의 한 트랜잭션 안에서 부른다.
 */
public interface RewriteMatchingRoundTimesPort {

    /** 차수와 같은 지부·유형의 차수를 모두 행 잠금으로 읽는다. 자동 선발·발표의 차수 행 잠금과 순서를 맞춘다. */
    Optional<MatchingSequence> lockSequence(Long roundId);

    void rewrite(List<RoundTimes> rounds);
}
