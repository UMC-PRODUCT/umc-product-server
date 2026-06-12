package com.umc.product.organization.application.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.organization.application.port.in.command.ManageUmcProductFunctionalUnitUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateUmcProductFunctionalUnitCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateUmcProductFunctionalUnitCommand;
import com.umc.product.organization.application.port.out.command.SaveUmcProductFunctionalUnitPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductFunctionalUnitPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductGenerationPort;
import com.umc.product.organization.domain.UmcProductFunctionalUnit;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UmcProductFunctionalUnitCommandService implements ManageUmcProductFunctionalUnitUseCase {

    private final LoadUmcProductGenerationPort loadUmcProductGenerationPort;
    private final LoadUmcProductFunctionalUnitPort loadUmcProductFunctionalUnitPort;
    private final SaveUmcProductFunctionalUnitPort saveUmcProductFunctionalUnitPort;
    private final UmcProductAccessPolicy umcProductAccessPolicy;

    @Override
    public Long create(CreateUmcProductFunctionalUnitCommand command) {
        validateCanManage(command.requesterMemberId());
        loadUmcProductGenerationPort.getById(command.umcProductGenerationId());
        validateParentUnit(command.parentUnitId(), command.umcProductGenerationId(), null);

        UmcProductFunctionalUnit functionalUnit = UmcProductFunctionalUnit.create(
            command.umcProductGenerationId(),
            command.parentUnitId(),
            command.type(),
            command.code(),
            command.name(),
            command.description(),
            command.sortOrder(),
            command.active()
        );
        return saveUmcProductFunctionalUnitPort.save(functionalUnit).getId();
    }

    @Override
    public void update(UpdateUmcProductFunctionalUnitCommand command) {
        validateCanManage(command.requesterMemberId());
        UmcProductFunctionalUnit functionalUnit = loadUmcProductFunctionalUnitPort.getById(command.functionalUnitId());
        validateParentUnit(command.parentUnitId(), functionalUnit.getUmcProductGenerationId(), functionalUnit.getId());

        functionalUnit.update(
            command.parentUnitId(),
            command.type(),
            command.code(),
            command.name(),
            command.description(),
            command.sortOrder(),
            command.active()
        );
        saveUmcProductFunctionalUnitPort.save(functionalUnit);
    }

    @Override
    public void delete(Long functionalUnitId, Long requesterMemberId) {
        validateCanManage(requesterMemberId);
        UmcProductFunctionalUnit functionalUnit = loadUmcProductFunctionalUnitPort.getById(functionalUnitId);
        saveUmcProductFunctionalUnitPort.delete(functionalUnit);
    }

    private void validateCanManage(Long requesterMemberId) {
        if (!umcProductAccessPolicy.canManageUmcProduct(requesterMemberId)) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_ACCESS_DENIED);
        }
    }

    private void validateParentUnit(Long parentUnitId, Long umcProductGenerationId, Long currentFunctionalUnitId) {
        if (parentUnitId == null) {
            return;
        }
        if (Objects.equals(parentUnitId, currentFunctionalUnitId)) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_FUNCTIONAL_UNIT_PARENT_INVALID);
        }
        UmcProductFunctionalUnit parentUnit = loadUmcProductFunctionalUnitPort.getById(parentUnitId);
        if (!Objects.equals(parentUnit.getUmcProductGenerationId(), umcProductGenerationId)) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_FUNCTIONAL_UNIT_NOT_FOUND);
        }
    }
}
