package com.umc.product.recruitment.application.service.command;

import com.umc.product.recruitment.application.port.in.command.UpdateFinalStatusUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateFinalStatusCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateFinalStatusResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecruitmentFinalSelectionService implements UpdateFinalStatusUseCase {

    @Override
    public UpdateFinalStatusResult update(UpdateFinalStatusCommand command) {
        return null;
    }

}
