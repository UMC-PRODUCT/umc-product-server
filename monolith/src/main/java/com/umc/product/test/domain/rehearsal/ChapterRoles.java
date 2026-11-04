package com.umc.product.test.domain.rehearsal;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.test.domain.roster.RehearsalCast;
import com.umc.product.test.domain.roster.RosterKey;

/**
 * 한 지부에서 QA가 직접 맡는 몫. 봇은 QA 프로젝트를 결정하지 않고, A·B·C 계정으로 지원하지 않는다.
 */
public record ChapterRoles(Set<String> qaProjectOwnerEmails, List<ReservedSeat> reserved) {
    public ChapterRoles {
        qaProjectOwnerEmails = Set.copyOf(qaProjectOwnerEmails);
        reserved = List.copyOf(reserved);
    }

    public record ReservedSeat(String email, ChallengerPart part, char label) {
    }

    /**
     * 배역표에 없는 지부(11기 밖)면 QA 몫이 없다.
     */
    public static ChapterRoles of(RehearsalCast cast, String chapter, Function<RosterKey, String> email) {
        if (!cast.chapters().contains(chapter)) {
            return new ChapterRoles(Set.of(), List.of());
        }
        Set<String> owners = cast.startingLine(chapter).qaOwners().stream().map(email).collect(Collectors.toSet());
        List<ReservedSeat> reserved = cast.qaReserved(chapter).stream()
            .map(seat -> new ReservedSeat(email.apply(seat.key()), seat.part(), seat.label()))
            .toList();
        return new ChapterRoles(owners, reserved);
    }

    public Set<String> reservedEmails() {
        return reserved.stream().map(ReservedSeat::email).collect(Collectors.toSet());
    }
}
