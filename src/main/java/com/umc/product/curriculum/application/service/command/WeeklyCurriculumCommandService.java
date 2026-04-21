package com.umc.product.curriculum.application.service.command;

import com.umc.product.curriculum.application.port.in.command.ManageWeeklyCurriculumUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.curriculum.CreateWeeklyCurriculumCommand;
import com.umc.product.curriculum.application.port.in.command.dto.curriculum.EditWeeklyCurriculumCommand;
import com.umc.product.global.exception.NotImplementedException;

public class WeeklyCurriculumCommandService implements ManageWeeklyCurriculumUseCase {

    @Override
    public Long create(CreateWeeklyCurriculumCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void edit(EditWeeklyCurriculumCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void delete(Long weeklyCurriculumId) {
        throw new NotImplementedException();
    }
}
