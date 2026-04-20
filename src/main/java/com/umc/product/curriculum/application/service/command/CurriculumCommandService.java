package com.umc.product.curriculum.application.service.command;

import com.umc.product.curriculum.application.port.in.command.ManageCurriculumUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.CreateCurriculumCommand;
import com.umc.product.curriculum.application.port.in.command.dto.CreateWeeklyCurriculumCommand;
import com.umc.product.curriculum.application.port.in.command.dto.EditCurriculumCommand;
import com.umc.product.curriculum.application.port.in.command.dto.EditWeeklyCurriculumCommand;
import com.umc.product.global.exception.NotImplementedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CurriculumCommandService implements ManageCurriculumUseCase {

    @Override
    public Long createCurriculum(CreateCurriculumCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void editCurriculum(EditCurriculumCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteCurriculum(Long curriculumId) {
        throw new NotImplementedException();
    }

    @Override
    public Long createWeeklyCurriculum(CreateWeeklyCurriculumCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void editWeeklyCurriculum(EditWeeklyCurriculumCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteWeeklyCurriculum(Long weeklyCurriculumId) {
        throw new NotImplementedException();
    }
}