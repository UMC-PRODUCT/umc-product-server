package com.umc.product.curriculum.application.service.command;

import com.umc.product.curriculum.application.port.in.command.ManageCurriculumUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.curriculum.CreateCurriculumCommand;
import com.umc.product.curriculum.application.port.in.command.dto.curriculum.EditCurriculumCommand;
import com.umc.product.global.exception.NotImplementedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CurriculumCommandService implements ManageCurriculumUseCase {

    @Override
    public Long create(CreateCurriculumCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void edit(EditCurriculumCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void delete(Long curriculumId) {
        throw new NotImplementedException();
    }

}
