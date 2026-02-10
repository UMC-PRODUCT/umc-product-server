package com.umc.product.schedule.application.port.in.command;

import com.umc.product.schedule.application.port.in.command.dto.CreateStudyGroupScheduleCommand;

/**
 * 스터디 그룹 일정 생성 UseCase
 */
public interface CreateStudyGroupScheduleUseCase {

    /**
     * 스터디 그룹 일정을 생성하고, 그룹 멤버 전원을 출석 대상으로 등록
     *
     * @param command 스터디 그룹 일정 생성 Command
     * @return 생성된 일정 ID
     */
    Long create(CreateStudyGroupScheduleCommand command);
}
