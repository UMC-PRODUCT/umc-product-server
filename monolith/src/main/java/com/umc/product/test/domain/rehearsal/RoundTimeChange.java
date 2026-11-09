package com.umc.product.test.domain.rehearsal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record RoundTimeChange(Long roundId, String field, Instant before, Instant after) {

    public static List<RoundTimeChange> between(RoundTimes before, RoundTimes after) {
        List<RoundTimeChange> changes = new ArrayList<>();
        add(changes, before, "startsAt", before.startsAt(), after.startsAt());
        add(changes, before, "endsAt", before.endsAt(), after.endsAt());
        add(changes, before, "decisionDeadline", before.decisionDeadline(), after.decisionDeadline());
        add(changes, before, "resultPublishAt", before.resultPublishAt(), after.resultPublishAt());
        return changes;
    }

    private static void add(List<RoundTimeChange> changes, RoundTimes round, String field, Instant before,
        Instant after) {
        if (!Objects.equals(before, after)) {
            changes.add(new RoundTimeChange(round.roundId(), field, before, after));
        }
    }
}
