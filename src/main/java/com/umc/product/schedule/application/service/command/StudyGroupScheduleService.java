package com.umc.product.schedule.application.service.command;

import com.umc.product.organization.application.port.in.command.dto.CreateStudyGroupScheduleCommand;
import com.umc.product.schedule.application.port.in.command.CreateStudyGroupScheduleUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyGroupScheduleService implements CreateStudyGroupScheduleUseCase {


    @Override
    public Long create(CreateStudyGroupScheduleCommand command) {
        return null;
    }
}
