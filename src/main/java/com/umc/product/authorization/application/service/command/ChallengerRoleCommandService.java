package com.umc.product.authorization.application.service.command;

import com.umc.product.authorization.application.port.in.command.ManageChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.command.dto.CreateChallengerRoleCommand;
import com.umc.product.authorization.application.port.in.command.dto.DeleteChallengerRoleCommand;
import com.umc.product.authorization.application.port.in.command.dto.UpdateChallengerRoleCommand;
import com.umc.product.authorization.application.port.out.LoadChallengerRolePort;
import com.umc.product.authorization.application.port.out.SaveChallengerRolePort;
import com.umc.product.authorization.domain.ChallengerRole;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengerRoleCommandService implements ManageChallengerRoleUseCase {

    private final LoadChallengerRolePort loadChallengerRolePort;
    private final SaveChallengerRolePort saveChallengerRolePort;

    @Override
    public Long createChallengerRole(CreateChallengerRoleCommand command) {
        ChallengerRole challengerRole = command.toEntity();
        return saveChallengerRolePort.save(challengerRole).getId();
    }

    @Override
    public List<Long> createChallengerRoleBulk(List<CreateChallengerRoleCommand> commands) {
        List<ChallengerRole> challengerRoles = commands.stream()
            .map(CreateChallengerRoleCommand::toEntity)
            .toList();

        return saveChallengerRolePort.saveAll(challengerRoles).stream()
            .map(ChallengerRole::getId)
            .toList();
    }

    @Override
    public void updateChallengerRole(UpdateChallengerRoleCommand command) {
        ChallengerRole challengerRole = loadChallengerRolePort.getById(command.challengerRoleId());
        challengerRole.update(command.roleType(), command.organizationId(), command.responsiblePart());
        saveChallengerRolePort.save(challengerRole);
    }

    @Override
    public void deleteChallengerRole(DeleteChallengerRoleCommand command) {
        ChallengerRole challengerRole = loadChallengerRolePort.getById(command.challengerRoleId());
        saveChallengerRolePort.delete(challengerRole);
    }
}