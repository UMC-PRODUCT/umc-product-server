package com.umc.product.schedule.application.service;

import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.dto.AttendanceRecordPermissionContext;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * AttendanceRecord(출석 기록) 리소스에 대한 권한 평가
 * <p>
 * - READ: 운영진 + 총괄 + 해당 출석 기록의 본인
 * - APPROVE: 중앙 총괄단(해당 기수) 또는 (일정 작성자 본인 AND 해당 기수 운영진)만 가능
 */
@Component
@RequiredArgsConstructor
public class AttendanceRecordPermissionEvaluator implements ResourcePermissionEvaluator {

    private final LoadAttendanceRecordPort loadAttendanceRecordPort;
    private final SchedulePermissionHelper permissionHelper;

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.ATTENDANCE_RECORD;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        Long recordId = resourcePermission.getResourceIdAsLong();

        // 단일 JOIN 쿼리로 record → sheet → schedule 정보 조회
        AttendanceRecordPermissionContext context = loadAttendanceRecordPort.findPermissionContext(recordId)
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.ATTENDANCE_RECORD_NOT_FOUND));

        Long memberId = subjectAttributes.memberId();

        return switch (resourcePermission.permission()) {
            case READ -> canRead(memberId, context);
            case APPROVE -> permissionHelper.canManageAttendanceByChallengerId(
                memberId, context.authorChallengerId(), context.gisuId());
            default -> false;
        };
    }

    /**
     * 조회 권한 확인: 본인 출석 기록 OR 출석 관리 권한 보유
     */
    private boolean canRead(Long memberId, AttendanceRecordPermissionContext context) {
        // 본인 출석 기록이면 OK
        if (Objects.equals(memberId, context.recordMemberId())) {
            return true;
        }

        // 운영진/총괄이면 OK
        return permissionHelper.canManageAttendanceByChallengerId(
            memberId, context.authorChallengerId(), context.gisuId());
    }
}
