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
 * 매칭 차수의 결과 공개를 처리하는 서비스.
 * <p>
 * 지원서의 최종 합불 반영, 공개 대기 멤버의 활성화와 차수의 공개 완료 기록을 동일한 트랜잭션에서 처리합니다.
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
    public void publish(Long matchingRoundId) {
        ProjectMatchingRound round;
        try {
            round = loadProjectMatchingRoundPort.getByIdForUpdate(matchingRoundId);
        } catch (ProjectDomainException e) {
            if (e.getBaseCode() == ProjectErrorCode.PROJECT_MATCHING_ROUND_NOT_FOUND) {
                // 삭제된 차수는 처리하지 않는다.
                return;
            }
            throw e;
        }
        Instant now = Instant.now();
        if (!round.canPublishAt(now)) {
            return;
        }

        Set<Long> approvedApplicationIds = new HashSet<>();
        // 프로젝트와 배정 행을 잠가 중단·완료 처리와 결과 공개의 경합을 방지한다.
        for (ProjectMember member : loadProjectMemberPort.listPendingByMatchingRoundId(matchingRoundId)) {
            ProjectStatus projectStatus = member.getProject().getStatus();
            if (projectStatus == ProjectStatus.IN_PROGRESS) {
                member.publish(now);
                if (member.getApplication() != null) {
                    approvedApplicationIds.add(member.getApplication().getId());
                }
            } else {
                member.withdraw("결과 공개 전 프로젝트 종료 또는 비공개 전환", null);
            }
        }

        // 선발을 다시 계산하지 않고, 실제 공개하는 배정에 연결된 지원서만 합격 처리한다.
        List<ProjectApplication> applications = loadProjectApplicationPort.listByMatchingRoundId(matchingRoundId).stream()
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

        // 지원서 합불과 멤버 활성화를 반영한 후 공개 완료 시각을 기록한다.
        round.publishResult(now);
    }
}
