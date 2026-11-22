package com.umc.product.challenger.application.port.in.command;

import com.umc.product.challenger.application.port.in.command.dto.ChallengerDeactivationPurgeResult;

public interface PurgeExpiredChallengerDeactivationsUseCase {

    ChallengerDeactivationPurgeResult purgeExpired();
}
