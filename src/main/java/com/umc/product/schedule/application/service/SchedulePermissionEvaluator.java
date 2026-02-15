package com.umc.product.schedule.application.service;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Schedule(일정) 리소스에 대한 권한 평가
 * <p>
 * - READ: 운영진 또는 중앙 총괄단만 가능 (참석 통계와 함께 일정 목록 조회 권한, 상세 조회는 별도 체크 없이 누구나 가능) - WRITE, DELETE: 일정 작성자 본인만 가능 - APPROVE:
 * 해당 기수의 중앙 총괄단 또는 (일정 작성자 본인 AND 해당 기수 운영진)만 가능
 */
@Component
@RequiredArgsConstructor
public class SchedulePermissionEvaluator implements ResourcePermissionEvaluator {

    private final LoadSchedulePort loadSchedulePort;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.SCHEDULE;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        Long scheduleId = resourcePermission.getResourceIdAsLong();

        // READ 권한: 참석 통계와 함께 일정 목록 조회 가능 여부 (운영진/총괄만 가능)
        if (resourcePermission.permission() == PermissionType.READ) {
            return canRead(subjectAttributes);
        }

        // 나머지 권한은 특정 일정에 대한 권한 체크
        Schedule schedule = loadSchedulePort.findById(scheduleId)
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        return switch (resourcePermission.permission()) {
            case WRITE, DELETE -> isAuthor(subjectAttributes, schedule);
            case APPROVE -> canApprove(subjectAttributes, schedule);
            default -> false;
        };
    }

    /**
     * 일정 작성자 본인인지 확인
     */
    private boolean isAuthor(SubjectAttributes subjectAttributes, Schedule schedule) {
        Long authorChallengerId = schedule.getAuthorChallengerId();
        Long authorMemberId = getChallengerUseCase.getChallengerPublicInfo(authorChallengerId).memberId();

        return Objects.equals(subjectAttributes.memberId(), authorMemberId);
    }

    /**
     * 출석 관리 권한 확인: 해당 기수의 중앙 총괄단 OR (일정 작성자 본인 AND 해당 기수 운영진)
     */
    private boolean canApprove(SubjectAttributes subjectAttributes, Schedule schedule) {
        Long memberId = subjectAttributes.memberId();

        // authorChallengerId로 gisuId 조회
        Long authorChallengerId = schedule.getAuthorChallengerId();
        ChallengerInfo authorInfo = getChallengerUseCase.getChallengerPublicInfo(authorChallengerId);
        Long gisuId = authorInfo.gisuId();

        // 해당 기수 중앙 총괄단이면 OK
        if (getChallengerRoleUseCase.isCentralCoreInGisu(memberId, gisuId)) {
            return true;
        }

        // 일정 작성자 본인 AND 해당 기수 운영진이면 OK
        if (Objects.equals(memberId, authorInfo.memberId()) && isStaffInGisu(memberId, gisuId)) {
            return true;
        }

        return false;
    }

    /**
     * 해당 기수에서 운영진(ChallengerRoleType이 있는 사람)인지 확인
     */
    private boolean isStaffInGisu(Long memberId, Long gisuId) {
        return !getChallengerRoleUseCase.getRolesByGisu(memberId, gisuId).isEmpty();
    }

    /**
     * 조회 권한 확인: 운영진 또는 총괄만 가능
     * <p>
     * READ 권한은 "목록 조회 가능 여부"만 의미함. 상세 조회는 @CheckAccess 없이 누구나 가능하므로 별도 체크 불필요.
     */
    private boolean canRead(SubjectAttributes subjectAttributes) {
        return !subjectAttributes.roleAttributes().isEmpty();
    }
}
