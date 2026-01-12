package com.umc.product.organization.application.port.service.command;

import com.umc.product.organization.application.port.in.command.ManageGisuUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateGisuCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateGisuCommand;
import org.springframework.stereotype.Service;

@Service
public class GisuController implements ManageGisuUseCase {
    @Override
    public Long register(CreateGisuCommand command) {
        return 0L;
    }

    @Override
    public void updateGisu(UpdateGisuCommand command) {

    }

    @Override
    public void deleteGisu(Long gisuId) {

    }

    @Override
    public void setCurrentGisu(Long gisuId) {

    }
}
