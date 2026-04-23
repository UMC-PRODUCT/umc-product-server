package com.umc.product.project.application.service.command;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.project.application.port.in.command.CreateDraftProjectUseCase;
import com.umc.product.project.application.port.in.command.SubmitProjectUseCase;
import com.umc.product.project.application.port.in.command.UpdateProjectUseCase;
import com.umc.product.project.application.port.in.command.dto.CreateDraftProjectCommand;
import com.umc.product.project.application.port.in.command.dto.SubmitProjectCommand;
import com.umc.product.project.application.port.in.command.dto.UpdateProjectCommand;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectCommandService implements
    CreateDraftProjectUseCase,
    UpdateProjectUseCase,
    SubmitProjectUseCase {

    private final LoadProjectPort loadProjectPort;
    private final SaveProjectPort saveProjectPort;

    // Cross-domain UseCases
    private final GetMemberUseCase getMemberUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetGisuUseCase getGisuUseCase;

    @Override
    public Long create(CreateDraftProjectCommand command) {
        throw new UnsupportedOperationException("TODO: CreateDraftProjectUseCase.create 구현 필요");
    }

    @Override
    public void update(UpdateProjectCommand command) {
        throw new UnsupportedOperationException("TODO: UpdateProjectUseCase.update 구현 필요");
    }

    @Override
    public void submit(SubmitProjectCommand command) {
        throw new UnsupportedOperationException("TODO: SubmitProjectUseCase.submit 구현 필요");
    }
}
