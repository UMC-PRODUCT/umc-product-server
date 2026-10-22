package com.umc.product.project.application.service.command;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.project.application.port.in.command.PublishProjectMatchingResultUseCase;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationPort;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 지원서 합불, 멤버의 PENDING_PUBLICATION → ACTIVE 변경, 공개 완료 시각을 함께 저장한다.
 * 도중에 실패하면 모두 롤백한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectMatchingResultPublicationCommandService implements PublishProjectMatchingResultUseCase {

    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final LoadProjectApplicationPort loadProjectApplicationPort;
    private final SaveProjectApplicationPort saveProjectApplicationPort;

    @Override
    public boolean publish(Long matchingRoundId) {
        ProjectMatchingRound round;
        try {
            round = loadProjectMatchingRoundPort.getByIdForUpdate(matchingRoundId);
        } catch (ProjectDomainException e) {
            if (e.getBaseCode() == ProjectErrorCode.PROJECT_MATCHING_ROUND_NOT_FOUND) {
                return false;
            }
            throw e;
        }
        Instant now = Instant.now();
        if (!round.canPublishAt(now)) {
            return false;
        }

        Set<Long> approvedApplicationIds = new HashSet<>();
        for (ProjectMember member : loadProjectMemberPort.listPendingByMatchingRoundId(matchingRoundId)) {
            ProjectStatus projectStatus = member.getProject().getStatus();
            if (projectStatus == ProjectStatus.IN_PROGRESS) {
                member.publish(now);
                if (member.getApplication() != null) {
                    approvedApplicationIds.add(member.getApplication().getId());
                }
            }
        }

        // ACTIVE로 변경한 멤버의 지원서는 APPROVED, 나머지 판정 대상 지원서는 REJECTED로 변경한다.
        List<ProjectApplication> applications = loadProjectApplicationPort.listByMatchingRoundId(matchingRoundId).stream()
            .filter(application -> application.getApplicationForm().getProject().getStatus() == ProjectStatus.IN_PROGRESS)
            .filter(application -> application.getStatus() == ProjectApplicationStatus.SUBMITTED
                || application.getStatus() == ProjectApplicationStatus.APPROVED
                || application.getStatus() == ProjectApplicationStatus.REJECTED)
            .toList();
        for (ProjectApplication application : applications) {
            ProjectApplicationStatus target = approvedApplicationIds.contains(application.getId())
                ? ProjectApplicationStatus.APPROVED : ProjectApplicationStatus.REJECTED;
            if (application.getStatus() != target) {
                application.applyAutoDecision(target, round.getAutoDecisionExecutedMemberId());
            }
        }
        saveProjectApplicationPort.saveAll(applications);

        round.publishResult(now);
        return true;
    }
}
