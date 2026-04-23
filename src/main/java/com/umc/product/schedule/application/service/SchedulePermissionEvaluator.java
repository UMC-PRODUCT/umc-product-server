package com.umc.product.schedule.application.service;

import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.schedule.application.port.out.LoadScheduleParticipantPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Schedule(일정) 리소스에 대한 권한 평가
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulePermissionEvaluator implements ResourcePermissionEvaluator {

    private final LoadSchedulePort loadSchedulePort;
    private final LoadScheduleParticipantPort loadScheduleParticipantPort;

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.SCHEDULE;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {

        PermissionType permission = resourcePermission.permission();
        Long memberId = subjectAttributes.memberId();

        // WRITE (일정 생성 / 출석 요청 / 사유 제출)
        if (permission == PermissionType.WRITE) {
            // 기본적으로 활동 중인 챌린저인지 확인
            if (subjectAttributes.gisuChallengerInfos().isEmpty()) {
                return false;
            }

            // 만약 '출석 요청', '사유 제출'처럼 특정 일정(resourceId)에 대한 작업일 경우
            if (resourcePermission.resourceId() != null) {
                Long scheduleId = resourcePermission.getResourceIdAsLong();

                // 참여자 명단에 존재하는지 확인
                return loadScheduleParticipantPort
                    .findByScheduleIdAndMemberId(scheduleId, memberId)
                    .isPresent();
            }

            return true; // 일정 생성 등 resourceId가 없는 WRITE는 통과
        }

        // READ (일정 조회)
        if (permission == PermissionType.READ) {
            return !subjectAttributes.gisuChallengerInfos().isEmpty();
        }

        // EDIT (일정 수정), DELETE (일정 삭제)
        // 생성자 본인이나 최고 운영 관리자만 가능
        if (permission == PermissionType.EDIT || permission == PermissionType.DELETE) {
            log.debug("memberId {}: ", memberId);
            // 최고 운영 관리자 권한이 있다면 즉시 통과
            boolean isAdmin = subjectAttributes.roleAttributes().stream()
                .anyMatch(role -> role.roleType().isSuperAdmin());
            if (isAdmin) {
                return true;
            }

            // 관리자가 아니라면 일정 생성자 본인인지 확인
            if (resourcePermission.resourceId() != null) {
                Schedule schedule = loadSchedulePort.findById(resourcePermission.getResourceIdAsLong())
                    .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));
                return schedule.getAuthorMemberId().equals(memberId);
            }

            return false;
        }

        // APPROVE(출석 승인/거절)
        // [운영진용] 일정 출석 현황 조회
        if (permission == PermissionType.APPROVE) {
            return subjectAttributes.roleAttributes().stream()
                .anyMatch(
                    role -> role.roleType().isAtLeastCentralCore() || role.roleType().isAtLeastCentralMember()
                        || role.roleType().isAtLeastSchoolCore() || role.roleType().isAtLeastSchoolAdmin()
                );
        }
        return false;
    }
}
