package com.umc.product.organization.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.organization.application.port.in.command.ManageUmcProductGenerationUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateUmcProductGenerationCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateUmcProductGenerationCommand;
import com.umc.product.organization.application.port.out.command.SaveUmcProductGenerationPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductGenerationPort;
import com.umc.product.organization.domain.UmcProductGeneration;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UmcProductGenerationCommandService implements ManageUmcProductGenerationUseCase {

    private final LoadUmcProductGenerationPort loadUmcProductGenerationPort;
    private final SaveUmcProductGenerationPort saveUmcProductGenerationPort;
    private final UmcProductAccessPolicy umcProductAccessPolicy;

    @Audited(
        domain = Domain.ORGANIZATION,
        action = AuditAction.CREATE,
        targetType = "UmcProductGeneration",
        targetId = "#result",
        description = "'UMC Product 기수를 생성했습니다.'"
    )
    @Override
    public Long create(CreateUmcProductGenerationCommand command) {
        if (!umcProductAccessPolicy.canCreateGeneration(command.requesterMemberId())) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_ACCESS_DENIED);
        }
        validateGenerationNotDuplicated(command.generation());
        UmcProductGeneration generation = UmcProductGeneration.create(
            command.generation(),
            command.startAt(),
            command.endAt(),
            command.active()
        );
        if (command.active()) {
            deactivateOldActiveGeneration();
        }
        return saveUmcProductGenerationPort.save(generation).getId();
    }

    @Audited(
        domain = Domain.ORGANIZATION,
        action = AuditAction.UPDATE,
        targetType = "UmcProductGeneration",
        targetId = "#command.umcProductGenerationId()",
        description = "'UMC Product 기수를 수정했습니다.'"
    )
    @Override
    public void update(UpdateUmcProductGenerationCommand command) {
        if (!umcProductAccessPolicy.canManageGeneration(command.requesterMemberId(), command.umcProductGenerationId())) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_ACCESS_DENIED);
        }
        UmcProductGeneration generation = loadUmcProductGenerationPort.getById(command.umcProductGenerationId());
        if (command.generation() != null && !command.generation().equals(generation.getGeneration())) {
            validateGenerationNotDuplicated(command.generation());
        }
        if (Boolean.TRUE.equals(command.active())) {
            deactivateOldActiveGeneration();
        }
        generation.update(command.generation(), command.startAt(), command.endAt(), command.active());
        saveUmcProductGenerationPort.save(generation);
    }

    @Audited(
        domain = Domain.ORGANIZATION,
        action = AuditAction.DELETE,
        targetType = "UmcProductGeneration",
        targetId = "#umcProductGenerationId",
        description = "'UMC Product 기수를 삭제했습니다.'"
    )
    @Override
    public void delete(Long umcProductGenerationId, Long requesterMemberId) {
        if (!umcProductAccessPolicy.canManageGeneration(requesterMemberId, umcProductGenerationId)) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_ACCESS_DENIED);
        }
        UmcProductGeneration generation = loadUmcProductGenerationPort.getById(umcProductGenerationId);
        saveUmcProductGenerationPort.delete(generation);
    }

    private void validateGenerationNotDuplicated(Long generation) {
        if (loadUmcProductGenerationPort.existsByGeneration(generation)) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_GENERATION_ALREADY_EXISTS);
        }
    }

    private void deactivateOldActiveGeneration() {
        loadUmcProductGenerationPort.findActiveWithLock()
            .ifPresent(UmcProductGeneration::inactive);
    }
}
