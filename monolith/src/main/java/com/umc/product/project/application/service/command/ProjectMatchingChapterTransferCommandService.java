package com.umc.product.project.application.service.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.authorization.application.port.in.query.CheckChallengerAuthorityUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;
import com.umc.product.project.application.port.in.command.ReleaseMatchingChapterTransferUseCase;
import com.umc.product.project.application.port.in.command.TransferMatchingChapterUseCase;
import com.umc.product.project.application.port.in.command.dto.ReleaseMatchingChapterTransferCommand;
import com.umc.product.project.application.port.in.command.dto.TransferMatchingChapterCommand;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingChapterTransferInfo;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingChapterTransferPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationPort;
import com.umc.product.project.application.port.out.SaveProjectMatchingChapterTransferPort;
import com.umc.product.project.application.service.policy.ProjectMatchingChapterResolver;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectMatchingChapterTransfer;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 프로젝트 매칭 지부 이동 등록·해제.
 * <p>
 * 권한: 슈퍼어드민, 해당 기수 총괄단, 원지부 지부장, 이동지부 지부장. 한 명이 등록하면 즉시 효력이 생긴다.
 * <p>
 * 전제 조건(등록·해제 공통): 해당 기수에 팀원이 아니고 APPROVED 지원서가 없어야 한다.
 * 통과하면 해당 기수의 DRAFT/SUBMITTED 지원서와, 아직 자동 선발이 실행되지 않은 차수의 REJECTED 지원서를 "매칭 지부 이동" 사유로
 * 자동 철회한다. REJECTED 는 결정 마감 전 PM 이 합격으로 되돌리거나 마감 시 자동 선발이 합격시킬 수 있어서, 이전 지부 팀에 배정되는 일을
 * 막기 위해 함께 철회한다. 철회는 차수 OPEN 여부와 무관하게 수행한다 (본인 철회 경로와 달리 운영 조치이므로).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectMatchingChapterTransferCommandService
    implements TransferMatchingChapterUseCase, ReleaseMatchingChapterTransferUseCase {

    static final String AUTO_CANCEL_REASON = "매칭 지부 이동";

    private static final List<ProjectApplicationStatus> CANCELLABLE_STATUSES = List.of(
        ProjectApplicationStatus.DRAFT, ProjectApplicationStatus.SUBMITTED, ProjectApplicationStatus.REJECTED
    );

    private final LoadProjectMatchingChapterTransferPort loadTransferPort;
    private final SaveProjectMatchingChapterTransferPort saveTransferPort;
    private final LoadProjectApplicationPort loadProjectApplicationPort;
    private final SaveProjectApplicationPort saveProjectApplicationPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final ProjectMatchingChapterResolver matchingChapterResolver;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetChapterUseCase getChapterUseCase;
    private final CheckChallengerAuthorityUseCase checkChallengerAuthorityUseCase;

    @Audited(
        domain = Domain.PROJECT,
        action = AuditAction.CREATE,
        targetType = "ProjectMatchingChapterTransfer",
        targetId = "#command.gisuId()",
        description = "'매칭 지부 이동을 ' + #command.targets().size() + '명 등록했습니다.'"
    )
    @Override
    public List<ProjectMatchingChapterTransferInfo> transfer(TransferMatchingChapterCommand command) {
        validateNoDuplicateTargets(command.targets());

        Long gisuId = command.gisuId();
        Long requesterId = command.requesterMemberId();
        Set<Long> gisuChapterIds = getChapterUseCase.listByGisuId(gisuId).stream()
            .map(ChapterInfo::id)
            .collect(Collectors.toSet());
        boolean gisuWideAuthority = hasGisuWideAuthority(requesterId, gisuId);

        List<ProjectMatchingChapterTransferInfo> results = new ArrayList<>();
        for (TransferMatchingChapterCommand.Target target : command.targets()) {
            results.add(transferOne(requesterId, gisuId, gisuChapterIds, gisuWideAuthority, target));
        }
        return results;
    }

    @Audited(
        domain = Domain.PROJECT,
        action = AuditAction.UPDATE,
        targetType = "ProjectMatchingChapterTransfer",
        targetId = "#command.transferId()",
        description = "'매칭 지부 이동을 해제했습니다.'"
    )
    @Override
    public ProjectMatchingChapterTransferInfo release(ReleaseMatchingChapterTransferCommand command) {
        ProjectMatchingChapterTransfer transfer = loadTransferPort.getById(command.transferId());
        if (!transfer.isActive()) {
            throw new ProjectDomainException(ProjectErrorCode.CHAPTER_TRANSFER_ALREADY_RELEASED);
        }

        Long requesterId = command.requesterMemberId();
        validateAccess(
            requesterId,
            transfer.getGisuId(),
            hasGisuWideAuthority(requesterId, transfer.getGisuId()),
            transfer.getFromChapterId(),
            transfer.getToChapterId()
        );
        validateNotMatched(transfer.getMemberId(), transfer.getGisuId());

        int cancelled = cancelInProgressApplications(transfer.getMemberId(), transfer.getGisuId(), requesterId);
        transfer.release(requesterId, command.reason());
        saveTransferPort.save(transfer);

        return ProjectMatchingChapterTransferInfo.of(transfer, cancelled);
    }

    private ProjectMatchingChapterTransferInfo transferOne(
        Long requesterId,
        Long gisuId,
        Set<Long> gisuChapterIds,
        boolean gisuWideAuthority,
        TransferMatchingChapterCommand.Target target
    ) {
        Long memberId = target.memberId();
        Long toChapterId = target.toChapterId();

        if (!gisuChapterIds.contains(toChapterId)) {
            throw new ProjectDomainException(ProjectErrorCode.CHAPTER_TRANSFER_CHAPTER_NOT_IN_GISU,
                "memberId=" + memberId + " 의 이동지부가 해당 기수의 지부가 아니에요.");
        }

        Long fromChapterId = matchingChapterResolver.findOriginChapter(memberId, gisuId)
            .map(ChapterInfo::id)
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.CHAPTER_TRANSFER_ORIGIN_NOT_FOUND,
                "memberId=" + memberId + " 의 원지부를 확인할 수 없어요."));

        validateAccess(requesterId, gisuId, gisuWideAuthority, fromChapterId, toChapterId);

        ChallengerInfo challenger = getChallengerUseCase.getActiveByMemberIdAndGisuId(memberId, gisuId);
        if (challenger.part() == null || challenger.part() == ChallengerPart.PLAN) {
            throw new ProjectDomainException(ProjectErrorCode.CHAPTER_TRANSFER_PART_NOT_ALLOWED,
                "memberId=" + memberId + " 는 매칭 지부를 이동할 수 없는 파트예요.");
        }

        if (matchingChapterResolver.findActiveTransfer(memberId, gisuId).isPresent()) {
            throw new ProjectDomainException(ProjectErrorCode.CHAPTER_TRANSFER_ALREADY_ACTIVE,
                "memberId=" + memberId + " 는 이미 매칭 지부를 이동했어요.");
        }

        validateNotMatched(memberId, gisuId);

        ProjectMatchingChapterTransfer transfer = ProjectMatchingChapterTransfer.create(
            gisuId, memberId, fromChapterId, toChapterId, target.reason(), requesterId
        );
        int cancelled = cancelInProgressApplications(memberId, gisuId, requesterId);
        ProjectMatchingChapterTransfer saved = saveTransferPort.save(transfer);

        return ProjectMatchingChapterTransferInfo.of(saved, cancelled);
    }

    private void validateNoDuplicateTargets(List<TransferMatchingChapterCommand.Target> targets) {
        Set<Long> seen = new HashSet<>();
        for (TransferMatchingChapterCommand.Target target : targets) {
            if (!seen.add(target.memberId())) {
                throw new ProjectDomainException(ProjectErrorCode.CHAPTER_TRANSFER_DUPLICATE_TARGET,
                    "memberId=" + target.memberId() + " 가 요청에 중복으로 들어 있어요.");
            }
        }
    }

    private boolean hasGisuWideAuthority(Long requesterId, Long gisuId) {
        return checkChallengerAuthorityUseCase.isSuperAdmin(requesterId)
            || checkChallengerAuthorityUseCase.isCentralCoreInGisu(requesterId, gisuId);
    }

    private void validateAccess(
        Long requesterId, Long gisuId, boolean gisuWideAuthority, Long fromChapterId, Long toChapterId
    ) {
        if (gisuWideAuthority
            || checkChallengerAuthorityUseCase.isChapterPresidentInGisu(requesterId, gisuId, fromChapterId)
            || checkChallengerAuthorityUseCase.isChapterPresidentInGisu(requesterId, gisuId, toChapterId)) {
            return;
        }
        throw new ProjectDomainException(ProjectErrorCode.CHAPTER_TRANSFER_ACCESS_DENIED);
    }

    /**
     * 이미 매칭이 확정된 챌린저(팀원 또는 APPROVED 지원서 보유)는 지부를 바꿀 수 없다.
     */
    private void validateNotMatched(Long memberId, Long gisuId) {
        boolean matched = loadProjectMemberPort.existsByGisuAndMember(gisuId, memberId)
            || !loadProjectApplicationPort.listByApplicantMemberIdAndGisuIdAndStatusIn(
                memberId, gisuId, List.of(ProjectApplicationStatus.APPROVED)).isEmpty();
        if (matched) {
            throw new ProjectDomainException(ProjectErrorCode.CHAPTER_TRANSFER_ALREADY_MATCHED,
                "memberId=" + memberId + " 는 이미 팀에 소속됐거나 합격한 지원서가 있어요.");
        }
    }

    private int cancelInProgressApplications(Long memberId, Long gisuId, Long requesterId) {
        List<ProjectApplication> applications = loadProjectApplicationPort
            .listByApplicantMemberIdAndGisuIdAndStatusIn(memberId, gisuId, CANCELLABLE_STATUSES).stream()
            .filter(ProjectMatchingChapterTransferCommandService::isStillInMatching)
            .toList();
        if (applications.isEmpty()) {
            return 0;
        }
        applications.forEach(application -> application.cancelForChapterTransfer(requesterId, AUTO_CANCEL_REASON));
        saveProjectApplicationPort.saveAll(applications);
        return applications.size();
    }

    /**
     * 철회 대상인지 판단한다. DRAFT/SUBMITTED 는 항상, REJECTED 는 자동 선발이 아직 실행되지 않은 차수일 때만 대상이다.
     * 이미 마감 처리된 차수의 불합격은 결과가 확정된 이력이므로 그대로 둔다.
     */
    private static boolean isStillInMatching(ProjectApplication application) {
        if (application.getStatus() != ProjectApplicationStatus.REJECTED) {
            return true;
        }
        return !application.getAppliedMatchingRound().isAutoDecisionExecuted();
    }
}
