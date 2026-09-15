package com.umc.product.test.application.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.test.application.port.in.command.DeleteSeedProjectDataUseCase;
import com.umc.product.test.application.port.in.command.dto.DeleteSeedProjectDataCommand;
import com.umc.product.test.application.port.in.command.dto.DeleteSeedProjectDataResult;
import com.umc.product.test.application.port.out.DeleteSeedProjectDataPort;
import com.umc.product.test.application.port.out.dto.ProjectDataDeletionCounts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Transactional
public class ProjectSeedDataCleanupService implements DeleteSeedProjectDataUseCase {

    private final GetGisuUseCase getGisuUseCase;
    private final DeleteSeedProjectDataPort deleteSeedProjectDataPort;

    @Override
    public DeleteSeedProjectDataResult delete(DeleteSeedProjectDataCommand command) {
        Long gisuId = command.gisuId() != null ? command.gisuId() : getGisuUseCase.getActiveGisuId();
        getGisuUseCase.getById(gisuId);

        log.warn("seed project data cleanup start: gisuId={}", gisuId);
        ProjectDataDeletionCounts counts = deleteSeedProjectDataPort.deleteByGisuId(gisuId);
        DeleteSeedProjectDataResult result = DeleteSeedProjectDataResult.from(gisuId, counts);
        log.warn(
            "seed project data cleanup completed: gisuId={}, projects={}, applications={}, members={}",
            gisuId,
            result.deletedProjects(),
            result.deletedProjectApplications(),
            result.deletedProjectMembers()
        );
        return result;
    }
}
