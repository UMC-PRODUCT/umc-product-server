package com.umc.product.organization.application.service;

import com.umc.product.organization.application.port.in.command.ManageUmcProductSquadUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateUmcProductSquadCommand;
import com.umc.product.organization.application.port.in.command.dto.UmcProductSquadParticipantCommand;
import com.umc.product.organization.application.port.in.command.dto.ReplaceUmcProductSquadParticipantsCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateUmcProductSquadCommand;
import com.umc.product.organization.application.port.out.command.SaveUmcProductSquadParticipantPort;
import com.umc.product.organization.application.port.out.command.SaveUmcProductSquadPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductMemberPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductSquadPort;
import com.umc.product.organization.domain.UmcProductMember;
import com.umc.product.organization.domain.UmcProductSquad;
import com.umc.product.organization.domain.UmcProductSquadParticipant;
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
public class UmcProductSquadCommandService implements ManageUmcProductSquadUseCase {

    private final LoadUmcProductSquadPort loadUmcProductSquadPort;
    private final SaveUmcProductSquadPort saveUmcProductSquadPort;
    private final LoadUmcProductMemberPort loadUmcProductMemberPort;
    private final SaveUmcProductSquadParticipantPort saveUmcProductSquadParticipantPort;
    private final UmcProductAccessPolicy umcProductAccessPolicy;

    @Override
    public Long create(CreateUmcProductSquadCommand command) {
        validateCanManage(command.requesterMemberId());
        UmcProductSquad squad = UmcProductSquad.create(
            command.code(),
            command.name(),
            command.description(),
            command.startAt(),
            command.endAt(),
            command.sortOrder(),
            command.active()
        );
        return saveUmcProductSquadPort.save(squad).getId();
    }

    @Override
    public void update(UpdateUmcProductSquadCommand command) {
        validateCanManage(command.requesterMemberId());
        UmcProductSquad squad = loadUmcProductSquadPort.getById(command.squadId());
        squad.update(
            command.code(),
            command.name(),
            command.description(),
            command.startAt(),
            command.endAt(),
            command.sortOrder(),
            command.active()
        );
        saveUmcProductSquadPort.save(squad);
    }

    @Override
    public void delete(Long squadId, Long requesterMemberId) {
        validateCanManage(requesterMemberId);
        UmcProductSquad squad = loadUmcProductSquadPort.getById(squadId);
        saveUmcProductSquadParticipantPort.deleteAllBySquadId(squad.getId());
        saveUmcProductSquadPort.delete(squad);
    }

    @Override
    public void replaceParticipants(ReplaceUmcProductSquadParticipantsCommand command) {
        validateCanManage(command.requesterMemberId());
        UmcProductSquad squad = loadUmcProductSquadPort.getById(command.squadId());
        List<UmcProductSquadParticipantCommand> participants =
            Objects.requireNonNullElse(command.participants(), List.of());
        validateParticipants(participants);

        saveUmcProductSquadParticipantPort.deleteAllBySquadId(squad.getId());
        saveUmcProductSquadParticipantPort.saveAll(participants.stream()
            .map(participant -> UmcProductSquadParticipant.create(
                squad,
                loadUmcProductMemberPort.getById(participant.umcProductMemberId()),
                participant.role(),
                participant.position(),
                participant.responsibilityTitle(),
                participant.responsibilityDescription()
            ))
            .toList());
    }

    private void validateCanManage(Long requesterMemberId) {
        if (!umcProductAccessPolicy.canManageUmcProduct(requesterMemberId)) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_ACCESS_DENIED);
        }
    }

    private void validateParticipants(List<UmcProductSquadParticipantCommand> participants) {
        Set<UmcProductSquadParticipantCommand> uniqueParticipants = new HashSet<>(participants);
        if (uniqueParticipants.size() != participants.size()) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_SQUAD_PARTICIPANT_REQUIRED);
        }
    }
}
