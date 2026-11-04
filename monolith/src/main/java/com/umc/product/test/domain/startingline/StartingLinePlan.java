package com.umc.product.test.domain.startingline;

import java.util.List;

import com.umc.product.test.domain.roster.RosterKey;

public record StartingLinePlan(
    String chapter,
    List<RosterKey> withoutProject,
    List<RosterKey> qaOwners,
    List<RosterKey> seededOwners
) {
    public StartingLinePlan {
        withoutProject = List.copyOf(withoutProject);
        qaOwners = List.copyOf(qaOwners);
        seededOwners = List.copyOf(seededOwners);
    }
}
