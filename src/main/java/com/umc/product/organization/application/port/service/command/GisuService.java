package com.umc.product.organization.application.port.service.command;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.organization.application.port.in.command.ManageGisuUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateGisuCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateGisuCommand;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.exception.OrganizationErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GisuService implements ManageGisuUseCase {

    private final LoadGisuPort loadGisuPort;
    private final ManageGisuPort manageGisuPort;

    @Override
    public Long register(CreateGisuCommand command) {
        if (loadGisuPort.existsByGeneration(command.number())) {
            throw new BusinessException(Domain.ORGANIZATION, OrganizationErrorCode.GISU_ALREADY_EXISTS);
        }

        Gisu gisu = Gisu.create(command.number(), command.startAt(), command.endAt(), false);

        return manageGisuPort.save(gisu).getId();
    }

    @Override
    public void updateGisu(UpdateGisuCommand command) {

    }

    @Override
    public void deleteGisu(Long gisuId) {
        Gisu gisu = loadGisuPort.findById(gisuId);
        manageGisuPort.delete(gisu);
    }

    @Override
    public void updateActiveGisu(Long gisuId) {
        Gisu oldGisu = loadGisuPort.findActiveGisu();

        oldGisu.updateIsActive(false);

        Gisu newGisu = loadGisuPort.findById(gisuId);

        newGisu.updateIsActive(true);

    }
}
