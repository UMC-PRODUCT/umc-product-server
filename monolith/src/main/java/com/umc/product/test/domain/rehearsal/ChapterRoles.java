package com.umc.product.test.domain.rehearsal;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.test.domain.roster.RehearsalCast;
import com.umc.product.test.domain.roster.RosterKey;

public record ChapterRoles(Set<String> qaProjectOwnerEmails, List<ReservedSeat> reserved) {
    public ChapterRoles {
        qaProjectOwnerEmails = Set.copyOf(qaProjectOwnerEmails);
        reserved = List.copyOf(reserved);
    }

    public record ReservedSeat(String email, ChallengerPart part, char label) {
    }

    public static ChapterRoles of(RehearsalCast cast, String chapter) {
        if (!cast.chapters().contains(chapter)) {
            return new ChapterRoles(Set.of(), List.of());
        }
        Set<String> owners = cast.startingLine(chapter).qaOwners().stream()
            .map(RosterKey::email)
            .collect(Collectors.toSet());
        List<ReservedSeat> reserved = cast.qaReserved(chapter).stream()
            .map(seat -> new ReservedSeat(seat.key().email(), seat.part(), seat.label()))
            .toList();
        return new ChapterRoles(owners, reserved);
    }

    public Set<String> reservedEmails() {
        return reserved.stream().map(ReservedSeat::email).collect(Collectors.toSet());
    }
}
