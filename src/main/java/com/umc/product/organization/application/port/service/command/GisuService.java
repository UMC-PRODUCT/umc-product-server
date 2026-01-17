package com.umc.product.organization.application.port.service.command;

import com.umc.product.organization.application.port.in.command.ManageGisuUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateGisuCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateGisuCommand;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.organization.domain.Gisu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GisuService implements ManageGisuUseCase {

    private final LoadGisuPort loadGisuPort;

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
    public void updateActiveGisu(Long gisuId) {
        Gisu oldGisu = loadGisuPort.findActiveGisu();

        oldGisu.updateIsActive(false);

        Gisu newGisu = loadGisuPort.findById(gisuId);

        newGisu.updateIsActive(true);

    }
}
