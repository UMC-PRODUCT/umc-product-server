package com.umc.product.curriculum.application.port.in.command;

import com.umc.product.curriculum.application.port.in.dto.SubmitMissionCommand;

public interface SubmitMissionUseCase {

    /**
     * 미션 제출
     */
    Long submit(SubmitMissionCommand command);
}
