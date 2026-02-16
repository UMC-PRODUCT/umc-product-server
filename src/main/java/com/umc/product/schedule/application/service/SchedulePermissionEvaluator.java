package com.umc.product.schedule.application.service;

import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Schedule(일정) 리소스에 대한 권한 평가
 * <p>
 * - READ: 운영진 또는 중앙 총괄단만 가능 (참석 통계와 함께 일정 목록 조회 권한, 상세 조회는 별도 체크 없이 누구나 가능)
 * - WRITE, DELETE: 일정 작성자 본인만 가능
 * - APPROVE: 해당 기수의 중앙 총괄단 또는 (일정 작성자 본인 AND 해당 기수 운영진)만 가능
 */
@Component
@RequiredArgsConstructor
public class SchedulePermissionEvaluator implements ResourcePermissionEvaluator {

    private final LoadSchedulePort loadSchedulePort;
    private final SchedulePermissionHelper permissionHelper;

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.SCHEDULE;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        Long scheduleId = resourcePermission.getResourceIdAsLong();

        // READ 권한: 참석 통계와 함께 일정 목록 조회 가능 여부 (운영진/총괄만 가능)
        if (resourcePermission.permission() == PermissionType.READ) {
            return permissionHelper.hasAnyRole(subjectAttributes);
        }

        // 나머지 권한은 특정 일정에 대한 권한 체크
        Schedule schedule = loadSchedulePort.findById(scheduleId)
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        Long memberId = subjectAttributes.memberId();

        return switch (resourcePermission.permission()) {
            case WRITE, DELETE -> permissionHelper.isAuthor(memberId, schedule);
            case APPROVE -> {
                Long gisuId = permissionHelper.getGisuIdFromSchedule(schedule);
                yield permissionHelper.canManageAttendance(memberId, schedule, gisuId);
            }
            default -> false;
        };
    }
}
