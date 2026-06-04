package com.umc.product.organization.application.service;

import com.umc.product.organization.application.port.in.command.ManageProductTeamGenerationUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateProductTeamGenerationCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateProductTeamGenerationCommand;
import com.umc.product.organization.application.port.out.command.SaveProductTeamGenerationPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamGenerationPort;
import com.umc.product.organization.domain.ProductTeamGeneration;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductTeamGenerationCommandService implements ManageProductTeamGenerationUseCase {

    private final LoadProductTeamGenerationPort loadProductTeamGenerationPort;
    private final SaveProductTeamGenerationPort saveProductTeamGenerationPort;
    private final ProductTeamAccessPolicy productTeamAccessPolicy;

    @Override
    public Long create(CreateProductTeamGenerationCommand command) {
        if (!productTeamAccessPolicy.canCreateGeneration(command.requesterMemberId())) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_ACCESS_DENIED);
        }
        validateGenerationNotDuplicated(command.generation());
        ProductTeamGeneration generation = ProductTeamGeneration.create(
            command.generation(),
            command.startAt(),
            command.endAt(),
            command.active()
        );
        if (command.active()) {
            deactivateOldActiveGeneration();
        }
        return saveProductTeamGenerationPort.save(generation).getId();
    }

    @Override
    public void update(UpdateProductTeamGenerationCommand command) {
        if (!productTeamAccessPolicy.canManageGeneration(command.requesterMemberId(), command.productTeamGenerationId())) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_ACCESS_DENIED);
        }
        ProductTeamGeneration generation = loadProductTeamGenerationPort.getById(command.productTeamGenerationId());
        if (command.generation() != null && !command.generation().equals(generation.getGeneration())) {
            validateGenerationNotDuplicated(command.generation());
        }
        if (Boolean.TRUE.equals(command.active())) {
            deactivateOldActiveGeneration();
        }
        generation.update(command.generation(), command.startAt(), command.endAt(), command.active());
        saveProductTeamGenerationPort.save(generation);
    }

    @Override
    public void delete(Long productTeamGenerationId, Long requesterMemberId) {
        if (!productTeamAccessPolicy.canManageGeneration(requesterMemberId, productTeamGenerationId)) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_ACCESS_DENIED);
        }
        ProductTeamGeneration generation = loadProductTeamGenerationPort.getById(productTeamGenerationId);
        saveProductTeamGenerationPort.delete(generation);
    }

    private void validateGenerationNotDuplicated(Long generation) {
        if (loadProductTeamGenerationPort.existsByGeneration(generation)) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_GENERATION_ALREADY_EXISTS);
        }
    }

    private void deactivateOldActiveGeneration() {
        loadProductTeamGenerationPort.findActiveWithLock()
            .ifPresent(ProductTeamGeneration::inactive);
    }
}
