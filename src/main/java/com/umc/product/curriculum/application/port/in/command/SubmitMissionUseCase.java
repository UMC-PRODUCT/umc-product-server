package com.umc.product.curriculum.application.port.in.command;

public interface SubmitMissionUseCase {

    /**
     * 미션 제출
     *
     * @param command 미션 제출 커맨드 (미션 ID, 워크북 ID, 제출 내용 포함)
     * @return 제출된 미션의 ID
     */
    Long submit(SubmitMissionCommand command);
}
