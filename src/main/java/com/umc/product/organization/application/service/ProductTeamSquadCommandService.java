package com.umc.product.organization.application.service;

import com.umc.product.organization.application.port.in.command.ManageProductTeamSquadUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateProductTeamSquadCommand;
import com.umc.product.organization.application.port.in.command.dto.ProductTeamSquadParticipantCommand;
import com.umc.product.organization.application.port.in.command.dto.ReplaceProductTeamSquadParticipantsCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateProductTeamSquadCommand;
import com.umc.product.organization.application.port.out.command.SaveProductTeamSquadParticipantPort;
import com.umc.product.organization.application.port.out.command.SaveProductTeamSquadPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamMemberPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamSquadPort;
import com.umc.product.organization.domain.ProductTeamMember;
import com.umc.product.organization.domain.ProductTeamSquad;
import com.umc.product.organization.domain.ProductTeamSquadParticipant;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
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
public class ProductTeamSquadCommandService implements ManageProductTeamSquadUseCase {

    private final LoadProductTeamSquadPort loadProductTeamSquadPort;
    private final SaveProductTeamSquadPort saveProductTeamSquadPort;
    private final LoadProductTeamMemberPort loadProductTeamMemberPort;
    private final SaveProductTeamSquadParticipantPort saveProductTeamSquadParticipantPort;
    private final ProductTeamAccessPolicy productTeamAccessPolicy;

    @Override
    public Long create(CreateProductTeamSquadCommand command) {
        validateCanManage(command.requesterMemberId());
        ProductTeamSquad squad = ProductTeamSquad.create(
            command.code(),
            command.name(),
            command.description(),
            command.startAt(),
            command.endAt(),
            command.sortOrder(),
            command.active()
        );
        return saveProductTeamSquadPort.save(squad).getId();
    }

    @Override
    public void update(UpdateProductTeamSquadCommand command) {
        validateCanManage(command.requesterMemberId());
        ProductTeamSquad squad = loadProductTeamSquadPort.getById(command.squadId());
        squad.update(
            command.code(),
            command.name(),
            command.description(),
            command.startAt(),
            command.endAt(),
            command.sortOrder(),
            command.active()
        );
        saveProductTeamSquadPort.save(squad);
    }

    @Override
    public void delete(Long squadId, Long requesterMemberId) {
        validateCanManage(requesterMemberId);
        ProductTeamSquad squad = loadProductTeamSquadPort.getById(squadId);
        saveProductTeamSquadParticipantPort.deleteAllBySquadId(squad.getId());
        saveProductTeamSquadPort.delete(squad);
    }

    @Override
    public void replaceParticipants(ReplaceProductTeamSquadParticipantsCommand command) {
        validateCanManage(command.requesterMemberId());
        ProductTeamSquad squad = loadProductTeamSquadPort.getById(command.squadId());
        List<ProductTeamSquadParticipantCommand> participants =
            Objects.requireNonNullElse(command.participants(), List.of());
        validateParticipants(participants);

        saveProductTeamSquadParticipantPort.deleteAllBySquadId(squad.getId());
        saveProductTeamSquadParticipantPort.saveAll(participants.stream()
            .map(participant -> ProductTeamSquadParticipant.create(
                squad,
                loadProductTeamMemberPort.getById(participant.productTeamMemberId()),
                participant.role(),
                participant.position(),
                participant.responsibilityTitle(),
                participant.responsibilityDescription()
            ))
            .toList());
    }

    private void validateCanManage(Long requesterMemberId) {
        if (!productTeamAccessPolicy.canManageProductTeam(requesterMemberId)) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_ACCESS_DENIED);
        }
    }

    private void validateParticipants(List<ProductTeamSquadParticipantCommand> participants) {
        Set<ProductTeamSquadParticipantCommand> uniqueParticipants = new HashSet<>(participants);
        if (uniqueParticipants.size() != participants.size()) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_SQUAD_PARTICIPANT_REQUIRED);
        }
    }
}
