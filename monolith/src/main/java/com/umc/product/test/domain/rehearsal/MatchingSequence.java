package com.umc.product.test.domain.rehearsal;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.umc.product.project.domain.enums.MatchingType;

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
