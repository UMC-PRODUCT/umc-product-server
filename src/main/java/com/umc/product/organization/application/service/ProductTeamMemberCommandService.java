package com.umc.product.organization.application.service;

import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.command.ManageProductTeamMemberUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateProductTeamMemberCommand;
import com.umc.product.organization.application.port.in.command.dto.ProductTeamActivityCommand;
import com.umc.product.organization.application.port.in.command.dto.ReplaceProductTeamMemberActivitiesCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateProductTeamMemberProfileCommand;
import com.umc.product.organization.application.port.out.command.SaveProductTeamMemberPort;
import com.umc.product.organization.application.port.out.command.SaveProductTeamMembershipPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamGenerationPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamMemberPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamMembershipPort;
import com.umc.product.organization.domain.ProductTeamMember;
import com.umc.product.organization.domain.ProductTeamMembership;
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
    private final LoadProductTeamMembershipPort loadProductTeamMembershipPort;
    private final SaveProductTeamMembershipPort saveProductTeamMembershipPort;
    private final LoadProductTeamGenerationPort loadProductTeamGenerationPort;
    private final GetMemberUseCase getMemberUseCase;
    private final GetFileUseCase getFileUseCase;
    private final ProductTeamAccessPolicy productTeamAccessPolicy;

    @Override
    public Long create(CreateProductTeamMemberCommand command) {
        validateActivities(command.activities());
        List<Long> generationIds = generationIdsOf(command.activities());
        validateCanManageAll(command.requesterMemberId(), generationIds);
        getMemberUseCase.getById(command.memberId());
        validateMemberNotDuplicated(command.memberId());
        validateProfileImage(command.profileImageId());
        validateGenerationsExist(generationIds);

        ProductTeamMember member = ProductTeamMember.create(
            command.memberId(),
            command.introduction(),
            command.profileImageId()
        );
        ProductTeamMember savedMember = saveProductTeamMemberPort.save(member);
        saveProductTeamMembershipPort.saveAll(toMemberships(savedMember, command.activities()));
        return savedMember.getId();
    }

    @Override
    public void updateProfile(UpdateProductTeamMemberProfileCommand command) {
        ProductTeamMember member = loadProductTeamMemberPort.getById(command.productTeamMemberId());
        List<Long> generationIds = loadProductTeamMembershipPort.listGenerationIdsByProductTeamMemberId(member.getId());
        boolean isSelf = Objects.equals(command.requesterMemberId(), member.getMemberId());
        if (!isSelf && !productTeamAccessPolicy.canManageMemberProfile(command.requesterMemberId(), member.getMemberId(),
            generationIds)) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_ACCESS_DENIED);
        }
        validateProfileImage(command.profileImageId());
        member.updateProfile(command.introduction(), command.profileImageId());
        saveProductTeamMemberPort.save(member);
    }

    @Override
    public void replaceActivities(ReplaceProductTeamMemberActivitiesCommand command) {
        ProductTeamMember member = loadProductTeamMemberPort.getById(command.productTeamMemberId());
        validateActivities(command.activities());
        List<Long> generationIds = generationIdsOf(command.activities());
        validateCanManageAll(command.requesterMemberId(), generationIds);
        validateGenerationsExist(generationIds);

        saveProductTeamMembershipPort.deleteAllByProductTeamMemberId(member.getId());
        saveProductTeamMembershipPort.saveAll(toMemberships(member, command.activities()));
    }

    @Override
    public void delete(Long productTeamMemberId, Long requesterMemberId) {
        ProductTeamMember member = loadProductTeamMemberPort.getById(productTeamMemberId);
        List<Long> generationIds = loadProductTeamMembershipPort.listGenerationIdsByProductTeamMemberId(member.getId());
        validateCanManageAll(requesterMemberId, generationIds);
        saveProductTeamMembershipPort.deleteAllByProductTeamMemberId(member.getId());
        saveProductTeamMemberPort.delete(member);
    }

    private void validateCanManageAll(Long requesterMemberId, List<Long> generationIds) {
        if (!productTeamAccessPolicy.canManageAllGenerations(requesterMemberId, generationIds)) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_ACCESS_DENIED);
        }
    }

    private void validateMemberNotDuplicated(Long memberId) {
        if (loadProductTeamMemberPort.existsByMemberId(memberId)) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_MEMBER_ALREADY_EXISTS);
        }
    }

    private void validateActivities(List<ProductTeamActivityCommand> activities) {
        if (activities == null || activities.isEmpty()) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_ACTIVITY_REQUIRED);
        }
        Set<ProductTeamActivityCommand> uniqueActivities = new HashSet<>(activities);
        if (uniqueActivities.size() != activities.size()) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_ACTIVITY_REQUIRED);
        }
    }

    private List<Long> generationIdsOf(List<ProductTeamActivityCommand> activities) {
        return activities.stream()
            .map(ProductTeamActivityCommand::productTeamGenerationId)
            .distinct()
            .toList();
    }

    private void validateGenerationsExist(List<Long> generationIds) {
        generationIds.forEach(loadProductTeamGenerationPort::getById);
    }

    private void validateProfileImage(String profileImageId) {
        if (profileImageId != null && !profileImageId.isBlank() && !getFileUseCase.existsById(profileImageId)) {
            throw new StorageException(StorageErrorCode.FILE_NOT_FOUND);
        }
    }

    private List<ProductTeamMembership> toMemberships(ProductTeamMember member, List<ProductTeamActivityCommand> activities) {
        return activities.stream()
            .map(activity -> ProductTeamMembership.create(
                member,
                activity.productTeamGenerationId(),
                activity.part(),
                activity.role(),
                activity.position()
            ))
            .toList();
    }
}
