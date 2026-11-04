package com.umc.product.test.application.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.project.application.port.in.command.ResyncMatchingRoundScheduleUseCase;
import com.umc.product.test.application.port.in.command.DeleteSeedProjectDataUseCase;
import com.umc.product.test.application.port.in.command.dto.DeleteSeedProjectDataCommand;
import com.umc.product.test.application.port.in.command.dto.DeleteSeedProjectDataResult;
import com.umc.product.test.application.port.out.DeleteSeedProjectDataPort;
import com.umc.product.test.application.port.out.dto.ProjectDataDeletionCounts;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ProjectSeedDataCleanupService implements DeleteSeedProjectDataUseCase {

    private final GetGisuUseCase getGisuUseCase;
    private final DeleteSeedProjectDataPort deleteSeedProjectDataPort;
    private final ResyncMatchingRoundScheduleUseCase resyncMatchingRoundScheduleUseCase;
    private final TransactionTemplate transactionTemplate;

    public ProjectSeedDataCleanupService(
        GetGisuUseCase getGisuUseCase,
        DeleteSeedProjectDataPort deleteSeedProjectDataPort,
        ResyncMatchingRoundScheduleUseCase resyncMatchingRoundScheduleUseCase,
        PlatformTransactionManager transactionManager
    ) {
        this.getGisuUseCase = getGisuUseCase;
        this.deleteSeedProjectDataPort = deleteSeedProjectDataPort;
        this.resyncMatchingRoundScheduleUseCase = resyncMatchingRoundScheduleUseCase;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public DeleteSeedProjectDataResult delete(DeleteSeedProjectDataCommand command) {
        Long gisuId = command.gisuId() != null ? command.gisuId() : getGisuUseCase.getActiveGisuId();
        getGisuUseCase.getById(gisuId);

        log.warn("seed project data cleanup start: gisuId={}", gisuId);
        ProjectDataDeletionCounts counts = transactionTemplate.execute(
            status -> deleteSeedProjectDataPort.deleteByGisuId(gisuId));
        counts.deletedProjectMatchingRoundIds().forEach(resyncMatchingRoundScheduleUseCase::resync);
        DeleteSeedProjectDataResult result = DeleteSeedProjectDataResult.from(gisuId, counts);
        log.warn(
            "seed project data cleanup completed: gisuId={}, projects={}, applications={}, members={}, rounds={}",
            gisuId,
            result.deletedProjects(),
            result.deletedProjectApplications(),
            result.deletedProjectMembers(),
            result.deletedProjectMatchingRounds()
        );
        return result;
    }
}
