package com.umc.product.challenger.application.port.in.command;

import com.umc.product.challenger.application.port.in.command.dto.AddChallengerDeactivationEvidenceCommand;

public interface AddChallengerDeactivationEvidenceUseCase {

    void addEvidence(AddChallengerDeactivationEvidenceCommand command);
}
