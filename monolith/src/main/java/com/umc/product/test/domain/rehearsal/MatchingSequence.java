package com.umc.product.test.domain.rehearsal;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.umc.product.project.domain.enums.MatchingType;

/**
 * 같은 지부·유형의 차수 묶음(1차, 2차, 3차, 랜덤). 넘기기는 차수 하나 단위지만 앞 차수 발표와 뒤 차수 시각 같은 규칙이 묶음에 걸린다.
 */
public record MatchingSequence(MatchingType type, Long chapterId, List<RoundTimes> rounds) {

    public MatchingSequence {
        rounds = rounds.stream().sorted(Comparator.comparing(RoundTimes::phase)).toList();
    }

    public RoundTimes get(Long roundId) {
        return rounds.get(indexOf(roundId));
    }

    public Optional<RoundTimes> previous(Long roundId) {
        int index = indexOf(roundId);
        return index == 0 ? Optional.empty() : Optional.of(rounds.get(index - 1));
    }

    public List<RoundTimes> after(Long roundId) {
        return rounds.subList(indexOf(roundId) + 1, rounds.size());
    }

    private int indexOf(Long roundId) {
        for (int i = 0; i < rounds.size(); i++) {
            if (rounds.get(i).roundId().equals(roundId)) {
                return i;
            }
        }
        throw new IllegalArgumentException("묶음에 없는 차수입니다: " + roundId);
    }
}
