package com.umc.product.schedule.application.service;

import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.dto.AttendanceSheetPermissionContext;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * AttendanceSheet(출석부) 리소스에 대한 권한 평가
 * <p>
 * - APPROVE: 중앙 총괄단(해당 기수) 또는 (일정 작성자 본인 AND 해당 기수 운영진)만 가능
 */
@Component
@RequiredArgsConstructor
public class AttendanceSheetPermissionEvaluator implements ResourcePermissionEvaluator {

    private final LoadAttendanceSheetPort loadAttendanceSheetPort;
    private final SchedulePermissionHelper permissionHelper;

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.ATTENDANCE_SHEET;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        Long sheetId = resourcePermission.getResourceIdAsLong();

        // 단일 JOIN 쿼리로 sheet → schedule 정보 조회
        AttendanceSheetPermissionContext context = loadAttendanceSheetPort.findPermissionContext(sheetId)
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.ATTENDANCE_SHEET_NOT_FOUND));

        return switch (resourcePermission.permission()) {
            case APPROVE -> permissionHelper.canManageAttendanceByChallengerId(
                subjectAttributes.memberId(), context.authorChallengerId(), context.gisuId());
            default -> false;
        };
    }
}
