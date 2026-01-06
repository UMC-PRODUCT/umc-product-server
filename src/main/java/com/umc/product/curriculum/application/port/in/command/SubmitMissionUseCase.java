package com.umc.product.curriculum.application.port.in.command;

public interface SubmitMissionUseCase {

    /**
     * 미션 제출
     */
    Long submit(SubmitMissionCommand command);
}
