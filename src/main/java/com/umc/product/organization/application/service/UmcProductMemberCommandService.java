package com.umc.product.organization.application.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.command.ManageUmcProductMemberUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateUmcProductMemberCommand;
import com.umc.product.organization.application.port.in.command.dto.ReplaceUmcProductMemberFunctionalMembershipsCommand;
import com.umc.product.organization.application.port.in.command.dto.UmcProductFunctionalMembershipCommand;
import com.umc.product.organization.application.port.in.command.dto.UmcProductSquadParticipationCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateUmcProductMemberProfileCommand;
import com.umc.product.organization.application.port.out.command.SaveUmcProductFunctionalMembershipPort;
import com.umc.product.organization.application.port.out.command.SaveUmcProductMemberPort;
import com.umc.product.organization.application.port.out.command.SaveUmcProductSquadParticipantPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductFunctionalUnitPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductGenerationPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductMemberPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductSquadPort;
import com.umc.product.organization.domain.UmcProductFunctionalMembership;
import com.umc.product.organization.domain.UmcProductFunctionalUnit;
import com.umc.product.organization.domain.UmcProductGeneration;
import com.umc.product.organization.domain.UmcProductMember;
import com.umc.product.organization.domain.UmcProductSquad;
import com.umc.product.organization.domain.UmcProductSquadParticipant;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.domain.exception.StorageErrorCode;
import com.umc.product.storage.domain.exception.StorageException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UmcProductMemberCommandService implements ManageUmcProductMemberUseCase {

    private final LoadUmcProductMemberPort loadUmcProductMemberPort;
    private final SaveUmcProductMemberPort saveUmcProductMemberPort;
    private final SaveUmcProductFunctionalMembershipPort saveUmcProductFunctionalMembershipPort;
    private final SaveUmcProductSquadParticipantPort saveUmcProductSquadParticipantPort;
    private final LoadUmcProductGenerationPort loadUmcProductGenerationPort;
    private final LoadUmcProductFunctionalUnitPort loadUmcProductFunctionalUnitPort;
    private final LoadUmcProductSquadPort loadUmcProductSquadPort;
    private final GetMemberUseCase getMemberUseCase;
    private final GetFileUseCase getFileUseCase;
    private final UmcProductAccessPolicy umcProductAccessPolicy;

    @Audited(
        domain = Domain.ORGANIZATION,
        action = AuditAction.CREATE,
        targetType = "UmcProductMember",
        targetId = "#result",
        description = "'UMC Product 멤버가 생성되었습니다.'"
    )
    @Override
    public Long create(CreateUmcProductMemberCommand command) {
        validateCanManage(command.requesterMemberId());
        validateFunctionalMemberships(command.functionalMemberships());
        validateSquadParticipations(command.squadParticipations());
        getMemberUseCase.getById(command.memberId());
        validateMemberNotDuplicated(command.memberId());
        validateProfileImage(command.profileImageId());
        validateFunctionalTargets(command.functionalMemberships());
        validateSquadTargets(command.squadParticipations());

        UmcProductMember member = UmcProductMember.create(
            command.memberId(),
            command.introduction(),
            command.profileImageId()
        );
        UmcProductMember savedMember = saveUmcProductMemberPort.save(member);
        saveUmcProductFunctionalMembershipPort.saveAll(
            toFunctionalMemberships(savedMember, command.functionalMemberships())
        );
        saveUmcProductSquadParticipantPort.saveAll(toSquadParticipants(savedMember, command.squadParticipations()));
        return savedMember.getId();
    }

    @Audited(
        domain = Domain.ORGANIZATION,
        action = AuditAction.UPDATE,
        targetType = "UmcProductMember",
        targetId = "#command.umcProductMemberId()",
        description = "'UMC Product 멤버 프로필이 수정되었습니다.'"
    )
    @Override
    public void updateProfile(UpdateUmcProductMemberProfileCommand command) {
        UmcProductMember member = loadUmcProductMemberPort.getById(command.umcProductMemberId());
        if (!umcProductAccessPolicy.canManageMemberProfile(command.requesterMemberId(), member.getMemberId())) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_ACCESS_DENIED);
        }
        validateProfileImage(command.profileImageId());
        member.updateProfile(command.introduction(), command.profileImageId());
        saveUmcProductMemberPort.save(member);
    }

    @Override
    public void replaceFunctionalMemberships(ReplaceUmcProductMemberFunctionalMembershipsCommand command) {
        UmcProductMember member = loadUmcProductMemberPort.getById(command.umcProductMemberId());
        validateCanManage(command.requesterMemberId());
        validateFunctionalMemberships(command.functionalMemberships());
        validateFunctionalTargets(command.functionalMemberships());

        saveUmcProductFunctionalMembershipPort.deleteAllByUmcProductMemberId(member.getId());
        saveUmcProductFunctionalMembershipPort.saveAll(toFunctionalMemberships(member, command.functionalMemberships()));
    }

    @Override
    public void delete(Long umcProductMemberId, Long requesterMemberId) {
        UmcProductMember member = loadUmcProductMemberPort.getById(umcProductMemberId);
        validateCanManage(requesterMemberId);
        saveUmcProductFunctionalMembershipPort.deleteAllByUmcProductMemberId(member.getId());
        saveUmcProductSquadParticipantPort.deleteAllByUmcProductMemberId(member.getId());
        saveUmcProductMemberPort.delete(member);
    }

    private void validateCanManage(Long requesterMemberId) {
        if (!umcProductAccessPolicy.canManageUmcProduct(requesterMemberId)) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_ACCESS_DENIED);
        }
    }

    private void validateMemberNotDuplicated(Long memberId) {
        if (loadUmcProductMemberPort.existsByMemberId(memberId)) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_MEMBER_ALREADY_EXISTS);
        }
    }

    private void validateFunctionalMemberships(List<UmcProductFunctionalMembershipCommand> functionalMemberships) {
        if (functionalMemberships == null || functionalMemberships.isEmpty()) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_FUNCTIONAL_MEMBERSHIP_REQUIRED);
        }
        Set<UmcProductFunctionalMembershipCommand> uniqueMemberships = new HashSet<>(functionalMemberships);
        if (uniqueMemberships.size() != functionalMemberships.size()) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_FUNCTIONAL_MEMBERSHIP_REQUIRED);
        }
    }

    private void validateSquadParticipations(List<UmcProductSquadParticipationCommand> squadParticipations) {
        if (squadParticipations == null || squadParticipations.isEmpty()) {
            return;
        }
        Set<UmcProductSquadParticipationCommand> uniqueParticipations = new HashSet<>(squadParticipations);
        if (uniqueParticipations.size() != squadParticipations.size()) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_SQUAD_PARTICIPANT_REQUIRED);
        }
    }

    private void validateFunctionalTargets(List<UmcProductFunctionalMembershipCommand> memberships) {
        Set<Long> generationIds = memberships.stream()
            .map(UmcProductFunctionalMembershipCommand::umcProductGenerationId)
            .collect(Collectors.toSet());
        Map<Long, UmcProductGeneration> generationMap = loadUmcProductGenerationPort.listByIds(generationIds).stream()
            .collect(Collectors.toMap(UmcProductGeneration::getId, Function.identity()));
        if (generationMap.size() != generationIds.size()) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_GENERATION_NOT_FOUND);
        }

        Set<Long> functionalUnitIds = memberships.stream()
            .map(UmcProductFunctionalMembershipCommand::functionalUnitId)
            .collect(Collectors.toSet());
        Map<Long, UmcProductFunctionalUnit> functionalUnitMap = loadUmcProductFunctionalUnitPort
            .listByIds(functionalUnitIds)
            .stream()
            .collect(Collectors.toMap(UmcProductFunctionalUnit::getId, Function.identity()));
        if (functionalUnitMap.size() != functionalUnitIds.size()) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_FUNCTIONAL_UNIT_NOT_FOUND);
        }

        memberships.forEach(membership -> {
            UmcProductFunctionalUnit functionalUnit = functionalUnitMap.get(membership.functionalUnitId());
            if (!Objects.equals(functionalUnit.getUmcProductGenerationId(), membership.umcProductGenerationId())) {
                throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_FUNCTIONAL_UNIT_NOT_FOUND);
            }
        });
    }

    private void validateSquadTargets(List<UmcProductSquadParticipationCommand> squadParticipations) {
        if (squadParticipations == null || squadParticipations.isEmpty()) {
            return;
        }
        getSquadMap(squadParticipations);
    }

    private void validateProfileImage(String profileImageId) {
        if (profileImageId != null && !profileImageId.isBlank() && !getFileUseCase.existsById(profileImageId)) {
            throw new StorageException(StorageErrorCode.FILE_NOT_FOUND);
        }
    }

    private List<UmcProductFunctionalMembership> toFunctionalMemberships(
        UmcProductMember member,
        List<UmcProductFunctionalMembershipCommand> memberships
    ) {
        return memberships.stream()
            .map(membership -> UmcProductFunctionalMembership.create(
                member,
                membership.umcProductGenerationId(),
                membership.functionalUnitId(),
                membership.role(),
                membership.position(),
                membership.responsibilityTitle(),
                membership.responsibilityDescription()
            ))
            .toList();
    }

    private List<UmcProductSquadParticipant> toSquadParticipants(
        UmcProductMember member,
        List<UmcProductSquadParticipationCommand> participations
    ) {
        if (participations == null || participations.isEmpty()) {
            return List.of();
        }
        Map<Long, UmcProductSquad> squadMap = getSquadMap(participations);
        return participations.stream()
            .map(participation -> UmcProductSquadParticipant.create(
                squadMap.get(participation.squadId()),
                member,
                participation.role(),
                participation.position(),
                participation.responsibilityTitle(),
                participation.responsibilityDescription()
            ))
            .toList();
    }

    private Map<Long, UmcProductSquad> getSquadMap(List<UmcProductSquadParticipationCommand> participations) {
        Set<Long> squadIds = participations.stream()
            .map(UmcProductSquadParticipationCommand::squadId)
            .collect(Collectors.toSet());
        Map<Long, UmcProductSquad> squadMap = loadUmcProductSquadPort.listByIds(squadIds).stream()
            .collect(Collectors.toMap(UmcProductSquad::getId, Function.identity()));
        if (squadMap.size() != squadIds.size()) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_SQUAD_NOT_FOUND);
        }
        return squadMap;
    }
}
