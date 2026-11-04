package com.umc.product.test.domain.roster;

import com.umc.product.common.domain.enums.ChallengerPart;

public record QaReservedApplicant(RosterKey key, ChallengerPart part, char label) {
}
