package com.umc.product.organization.application.service;

import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.command.ManageProductTeamMemberUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateProductTeamMemberCommand;
import com.umc.product.organization.application.port.in.command.dto.ProductTeamFunctionalMembershipCommand;
import com.umc.product.organization.application.port.in.command.dto.ProductTeamSquadParticipationCommand;
import com.umc.product.organization.application.port.in.command.dto.ReplaceProductTeamMemberFunctionalMembershipsCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateProductTeamMemberProfileCommand;
import com.umc.product.organization.application.port.out.command.SaveProductTeamFunctionalMembershipPort;
import com.umc.product.organization.application.port.out.command.SaveProductTeamMemberPort;
import com.umc.product.organization.application.port.out.command.SaveProductTeamSquadParticipantPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamFunctionalUnitPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamGenerationPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamMemberPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamSquadPort;
import com.umc.product.organization.domain.ProductTeamFunctionalMembership;
import com.umc.product.organization.domain.ProductTeamFunctionalUnit;
import com.umc.product.organization.domain.ProductTeamMember;
import com.umc.product.organization.domain.ProductTeamSquadParticipant;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.domain.exception.StorageErrorCode;
import com.umc.product.storage.domain.exception.StorageException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductTeamMemberCommandService implements ManageProductTeamMemberUseCase {

    private final LoadProductTeamMemberPort loadProductTeamMemberPort;
    private final SaveProductTeamMemberPort saveProductTeamMemberPort;
    private final SaveProductTeamFunctionalMembershipPort saveProductTeamFunctionalMembershipPort;
    private final SaveProductTeamSquadParticipantPort saveProductTeamSquadParticipantPort;
    private final LoadProductTeamGenerationPort loadProductTeamGenerationPort;
    private final LoadProductTeamFunctionalUnitPort loadProductTeamFunctionalUnitPort;
    private final LoadProductTeamSquadPort loadProductTeamSquadPort;
    private final GetMemberUseCase getMemberUseCase;
    private final GetFileUseCase getFileUseCase;
    private final ProductTeamAccessPolicy productTeamAccessPolicy;

    @Override
    public Long create(CreateProductTeamMemberCommand command) {
        validateCanManage(command.requesterMemberId());
        validateFunctionalMemberships(command.functionalMemberships());
        validateSquadParticipations(command.squadParticipations());
        getMemberUseCase.getById(command.memberId());
        validateMemberNotDuplicated(command.memberId());
        validateProfileImage(command.profileImageId());
        validateFunctionalTargets(command.functionalMemberships());
        validateSquadTargets(command.squadParticipations());

        ProductTeamMember member = ProductTeamMember.create(
            command.memberId(),
            command.introduction(),
            command.profileImageId()
        );
        ProductTeamMember savedMember = saveProductTeamMemberPort.save(member);
        saveProductTeamFunctionalMembershipPort.saveAll(
            toFunctionalMemberships(savedMember, command.functionalMemberships())
        );
        saveProductTeamSquadParticipantPort.saveAll(toSquadParticipants(savedMember, command.squadParticipations()));
        return savedMember.getId();
    }

    @Override
    public void updateProfile(UpdateProductTeamMemberProfileCommand command) {
        ProductTeamMember member = loadProductTeamMemberPort.getById(command.productTeamMemberId());
        if (!productTeamAccessPolicy.canManageMemberProfile(command.requesterMemberId(), member.getMemberId())) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_ACCESS_DENIED);
        }
        validateProfileImage(command.profileImageId());
        member.updateProfile(command.introduction(), command.profileImageId());
        saveProductTeamMemberPort.save(member);
    }

    @Override
    public void replaceFunctionalMemberships(ReplaceProductTeamMemberFunctionalMembershipsCommand command) {
        ProductTeamMember member = loadProductTeamMemberPort.getById(command.productTeamMemberId());
        validateCanManage(command.requesterMemberId());
        validateFunctionalMemberships(command.functionalMemberships());
        validateFunctionalTargets(command.functionalMemberships());

        saveProductTeamFunctionalMembershipPort.deleteAllByProductTeamMemberId(member.getId());
        saveProductTeamFunctionalMembershipPort.saveAll(toFunctionalMemberships(member, command.functionalMemberships()));
    }

    @Override
    public void delete(Long productTeamMemberId, Long requesterMemberId) {
        ProductTeamMember member = loadProductTeamMemberPort.getById(productTeamMemberId);
        validateCanManage(requesterMemberId);
        saveProductTeamFunctionalMembershipPort.deleteAllByProductTeamMemberId(member.getId());
        saveProductTeamSquadParticipantPort.deleteAllByProductTeamMemberId(member.getId());
        saveProductTeamMemberPort.delete(member);
    }

    private void validateCanManage(Long requesterMemberId) {
        if (!productTeamAccessPolicy.canManageProductTeam(requesterMemberId)) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_ACCESS_DENIED);
        }
    }

    private void validateMemberNotDuplicated(Long memberId) {
        if (loadProductTeamMemberPort.existsByMemberId(memberId)) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_MEMBER_ALREADY_EXISTS);
        }
    }

    private void validateFunctionalMemberships(List<ProductTeamFunctionalMembershipCommand> functionalMemberships) {
        if (functionalMemberships == null || functionalMemberships.isEmpty()) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_FUNCTIONAL_MEMBERSHIP_REQUIRED);
        }
        Set<ProductTeamFunctionalMembershipCommand> uniqueMemberships = new HashSet<>(functionalMemberships);
        if (uniqueMemberships.size() != functionalMemberships.size()) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_FUNCTIONAL_MEMBERSHIP_REQUIRED);
        }
    }

    private void validateSquadParticipations(List<ProductTeamSquadParticipationCommand> squadParticipations) {
        if (squadParticipations == null || squadParticipations.isEmpty()) {
            return;
        }
        Set<ProductTeamSquadParticipationCommand> uniqueParticipations = new HashSet<>(squadParticipations);
        if (uniqueParticipations.size() != squadParticipations.size()) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_SQUAD_PARTICIPANT_REQUIRED);
        }
    }

    private void validateFunctionalTargets(List<ProductTeamFunctionalMembershipCommand> memberships) {
        memberships.forEach(membership -> {
            loadProductTeamGenerationPort.getById(membership.productTeamGenerationId());
            ProductTeamFunctionalUnit functionalUnit = loadProductTeamFunctionalUnitPort.getById(membership.functionalUnitId());
            if (!Objects.equals(functionalUnit.getProductTeamGenerationId(), membership.productTeamGenerationId())) {
                throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_FUNCTIONAL_UNIT_NOT_FOUND);
            }
        });
    }

    private void validateSquadTargets(List<ProductTeamSquadParticipationCommand> squadParticipations) {
        if (squadParticipations == null) {
            return;
        }
        squadParticipations.forEach(participation -> loadProductTeamSquadPort.getById(participation.squadId()));
    }

    private void validateProfileImage(String profileImageId) {
        if (profileImageId != null && !profileImageId.isBlank() && !getFileUseCase.existsById(profileImageId)) {
            throw new StorageException(StorageErrorCode.FILE_NOT_FOUND);
        }
    }

    private List<ProductTeamFunctionalMembership> toFunctionalMemberships(
        ProductTeamMember member,
        List<ProductTeamFunctionalMembershipCommand> memberships
    ) {
        return memberships.stream()
            .map(membership -> ProductTeamFunctionalMembership.create(
                member,
                membership.productTeamGenerationId(),
                membership.functionalUnitId(),
                membership.role(),
                membership.position(),
                membership.responsibilityTitle(),
                membership.responsibilityDescription()
            ))
            .toList();
    }

    private List<ProductTeamSquadParticipant> toSquadParticipants(
        ProductTeamMember member,
        List<ProductTeamSquadParticipationCommand> participations
    ) {
        if (participations == null || participations.isEmpty()) {
            return List.of();
        }
        return participations.stream()
            .map(participation -> ProductTeamSquadParticipant.create(
                loadProductTeamSquadPort.getById(participation.squadId()),
                member,
                participation.role(),
                participation.position(),
                participation.responsibilityTitle(),
                participation.responsibilityDescription()
            ))
            .toList();
    }
}
