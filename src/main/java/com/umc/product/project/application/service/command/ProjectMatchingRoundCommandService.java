package com.umc.product.project.application.service.command;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleInfo;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.project.application.port.in.command.CreateProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.command.DeleteProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.command.UpdateProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.command.dto.CreateProjectMatchingRoundCommand;
import com.umc.product.project.application.port.in.command.dto.UpdateProjectMatchingRoundCommand;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.SaveProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.ScheduleMatchingRoundDeadlinePort;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 매칭 차수의 CRUD lifecycle 만 담당한다.
 * <p>
 * 차수 종료 시점의 자동 선발(autoDecide) 처리는
 * {@link ProjectMatchingRoundFinalizationCommandService} 가 담당한다.
 * 두 책임을 분리하지 않으면 lifecycle 호출 흐름과 트리거 흐름이 한 클래스에 모여 순환 의존성이 발생한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectMatchingRoundCommandService implements
    CreateProjectMatchingRoundUseCase,
    UpdateProjectMatchingRoundUseCase,
    DeleteProjectMatchingRoundUseCase {

    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    private final SaveProjectMatchingRoundPort saveProjectMatchingRoundPort;
    private final LoadProjectApplicationPort loadProjectApplicationPort;
    private final ScheduleMatchingRoundDeadlinePort scheduleMatchingRoundDeadlinePort;

    private final GetChallengerRoleUseCase getChallengerRoleUseCase;

    @Override
    public Long create(CreateProjectMatchingRoundCommand command) {
        // 지부장 이상부터 매칭 차수에 대한 수정이 가능합니다.
        validateManageAccess(command.requesterMemberId(), command.chapterId());
        // 매칭 차수는 중복될 수 없습니다.
        validateNoOverlap(
            command.chapterId(), command.startsAt(), command.endsAt(), command.decisionDeadline());

        ProjectMatchingRound matchingRound = ProjectMatchingRound.create(
            command.name(),
            command.description(),
            command.type(),
            command.phase(),
            command.chapterId(),
            command.startsAt(),
            command.endsAt(),
            command.decisionDeadline()
        );

        ProjectMatchingRound saved = saveProjectMatchingRoundPort.save(matchingRound);
        scheduleMatchingRoundDeadlinePort.schedule(saved);
        return saved.getId();
    }

    @Override
    public void update(UpdateProjectMatchingRoundCommand command) {
        ProjectMatchingRound matchingRound = loadProjectMatchingRoundPort.getById(command.matchingRoundId());

        String name = command.name() != null ? command.name() : matchingRound.getName();
        String description = command.description() != null ? command.description() : matchingRound.getDescription();
        MatchingType type = command.type() != null ? command.type() : matchingRound.getType();
        MatchingPhase phase = command.phase() != null ? command.phase() : matchingRound.getPhase();
        Instant startsAt = command.startsAt() != null ? command.startsAt() : matchingRound.getStartsAt();
        Instant endsAt = command.endsAt() != null ? command.endsAt() : matchingRound.getEndsAt();
        Instant decisionDeadline = command.decisionDeadline() != null
            ? command.decisionDeadline()
            : matchingRound.getDecisionDeadline();

        validateManageAccess(command.requesterMemberId(), matchingRound.getChapterId());
        validateNoOverlapExceptId(
            matchingRound.getId(),
            matchingRound.getChapterId(),
            startsAt,
            endsAt,
            decisionDeadline
        );

        matchingRound.update(
            name,
            description,
            type,
            phase,
            startsAt,
            endsAt,
            decisionDeadline
        );
        scheduleMatchingRoundDeadlinePort.schedule(matchingRound);
    }

    @Override
    public void delete(Long matchingRoundId, Long requesterMemberId) {
        ProjectMatchingRound matchingRound = loadProjectMatchingRoundPort.getById(matchingRoundId);
        validateManageAccess(requesterMemberId, matchingRound.getChapterId());

        if (loadProjectApplicationPort.existsByAppliedMatchingRoundId(matchingRoundId)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_DELETE_CONFLICT);
        }

        saveProjectMatchingRoundPort.delete(matchingRound);
        scheduleMatchingRoundDeadlinePort.cancel(matchingRoundId);
    }

    private void validateNoOverlap(
        Long chapterId, Instant startsAt, Instant endsAt, Instant decisionDeadline
    ) {
        ProjectMatchingRound.validateDates(startsAt, endsAt, decisionDeadline);
        if (!loadProjectMatchingRoundPort.listOverlapping(chapterId, startsAt, decisionDeadline).isEmpty()) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_PERIOD_OVERLAPPED);
        }
    }

    private void validateNoOverlapExceptId(
        Long id, Long chapterId, Instant startsAt, Instant endsAt, Instant decisionDeadline
    ) {
        ProjectMatchingRound.validateDates(startsAt, endsAt, decisionDeadline);
        if (!loadProjectMatchingRoundPort.listOverlappingExceptId(
            id, chapterId, startsAt, decisionDeadline).isEmpty()) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_PERIOD_OVERLAPPED);
        }
    }

    private void validateManageAccess(Long memberId, Long chapterId) {
        List<ChallengerRoleInfo> roles = getChallengerRoleUseCase.findAllByMemberId(memberId);
        boolean allowed = roles.stream()
            .anyMatch(role -> role.roleType().isAtLeastCentralCore()
                || (role.roleType() == ChallengerRoleType.CHAPTER_PRESIDENT
                && Objects.equals(role.organizationId(), chapterId)));

        if (!allowed) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_ACCESS_DENIED);
        }
    }
}
