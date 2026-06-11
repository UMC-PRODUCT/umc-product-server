package com.umc.product.organization.application.service;

import com.umc.product.organization.application.port.in.command.ManageProductTeamFunctionalUnitUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateProductTeamFunctionalUnitCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateProductTeamFunctionalUnitCommand;
import com.umc.product.organization.application.port.out.command.SaveProductTeamFunctionalUnitPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamFunctionalUnitPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamGenerationPort;
import com.umc.product.organization.domain.ProductTeamFunctionalUnit;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductTeamFunctionalUnitCommandService implements ManageProductTeamFunctionalUnitUseCase {

    private final LoadProductTeamGenerationPort loadProductTeamGenerationPort;
    private final LoadProductTeamFunctionalUnitPort loadProductTeamFunctionalUnitPort;
    private final SaveProductTeamFunctionalUnitPort saveProductTeamFunctionalUnitPort;
    private final ProductTeamAccessPolicy productTeamAccessPolicy;

    @Override
    public Long create(CreateProductTeamFunctionalUnitCommand command) {
        validateCanManage(command.requesterMemberId());
        loadProductTeamGenerationPort.getById(command.productTeamGenerationId());
        validateParentUnit(command.parentUnitId(), command.productTeamGenerationId());

        ProductTeamFunctionalUnit functionalUnit = ProductTeamFunctionalUnit.create(
            command.productTeamGenerationId(),
            command.parentUnitId(),
            command.type(),
            command.code(),
            command.name(),
            command.description(),
            command.sortOrder(),
            command.active()
        );
        return saveProductTeamFunctionalUnitPort.save(functionalUnit).getId();
    }

    @Override
    public void update(UpdateProductTeamFunctionalUnitCommand command) {
        validateCanManage(command.requesterMemberId());
        ProductTeamFunctionalUnit functionalUnit = loadProductTeamFunctionalUnitPort.getById(command.functionalUnitId());
        validateParentUnit(command.parentUnitId(), functionalUnit.getProductTeamGenerationId());

        functionalUnit.update(
            command.parentUnitId(),
            command.type(),
            command.code(),
            command.name(),
            command.description(),
            command.sortOrder(),
            command.active()
        );
        saveProductTeamFunctionalUnitPort.save(functionalUnit);
    }

    @Override
    public void delete(Long functionalUnitId, Long requesterMemberId) {
        validateCanManage(requesterMemberId);
        ProductTeamFunctionalUnit functionalUnit = loadProductTeamFunctionalUnitPort.getById(functionalUnitId);
        saveProductTeamFunctionalUnitPort.delete(functionalUnit);
    }

    private void validateCanManage(Long requesterMemberId) {
        if (!productTeamAccessPolicy.canManageProductTeam(requesterMemberId)) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_ACCESS_DENIED);
        }
    }

    private void validateParentUnit(Long parentUnitId, Long productTeamGenerationId) {
        if (parentUnitId == null) {
            return;
        }
        ProductTeamFunctionalUnit parentUnit = loadProductTeamFunctionalUnitPort.getById(parentUnitId);
        if (!Objects.equals(parentUnit.getProductTeamGenerationId(), productTeamGenerationId)) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_FUNCTIONAL_UNIT_NOT_FOUND);
        }
    }
}
