package com.umc.product.test.application.port.in.command;

import com.umc.product.test.application.port.in.command.dto.AdvanceMatchingRoundResult;
import com.umc.product.test.domain.rehearsal.AdvanceStep;

public interface AdvanceMatchingRoundUseCase {

    AdvanceMatchingRoundResult advance(Long roundId, AdvanceStep step);
}
