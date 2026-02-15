package com.umc.product.schedule.application.service;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * AttendanceRecord(출석 기록) 리소스에 대한 권한 평가
 * <p>
 * - READ: 운영진 + 총괄 + 해당 출석 기록의 본인 - APPROVE: 중앙 총괄단(해당 기수) 또는 (일정 작성자 본인 AND 해당 기수 운영진)만 가능
 */
@Component
@RequiredArgsConstructor
public class AttendanceRecordPermissionEvaluator implements ResourcePermissionEvaluator {

    private final LoadAttendanceRecordPort loadAttendanceRecordPort;
    private final LoadAttendanceSheetPort loadAttendanceSheetPort;
    private final LoadSchedulePort loadSchedulePort;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.ATTENDANCE_RECORD;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        Long recordId = resourcePermission.getResourceIdAsLong();

        // recordId → sheetId → gisuId, scheduleId
        AttendanceRecord record = loadAttendanceRecordPort.findById(recordId)
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.ATTENDANCE_RECORD_NOT_FOUND));

        AttendanceSheet sheet = loadAttendanceSheetPort.findById(record.getAttendanceSheetId())
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.ATTENDANCE_SHEET_NOT_FOUND));

        Schedule schedule = loadSchedulePort.findById(sheet.getScheduleId())
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        return switch (resourcePermission.permission()) {
            case READ -> canRead(subjectAttributes, record, schedule, sheet.getGisuId());
            case APPROVE -> canApprove(subjectAttributes, schedule, sheet.getGisuId());
            default -> false;
        };
    }

    /**
     * 출석 관리 권한 확인: 중앙 총괄단(해당 기수) OR (일정 작성자 본인 AND 해당 기수 운영진)
     */
    private boolean canApprove(SubjectAttributes subjectAttributes, Schedule schedule, Long gisuId) {
        Long memberId = subjectAttributes.memberId();

        // 해당 기수 중앙 총괄단이면 OK
        if (getChallengerRoleUseCase.isCentralCoreInGisu(memberId, gisuId)) {
            return true;
        }

        // 일정 작성자 본인 AND 해당 기수 운영진이면 OK
        if (isAuthor(memberId, schedule) && isStaffInGisu(memberId, gisuId)) {
            return true;
        }

        return false;
    }

    /**
     * 일정 작성자 본인인지 확인
     */
    private boolean isAuthor(Long memberId, Schedule schedule) {
        Long authorChallengerId = schedule.getAuthorChallengerId();
        Long authorMemberId = getChallengerUseCase.getChallengerPublicInfo(authorChallengerId).memberId();

        return Objects.equals(memberId, authorMemberId);
    }

    /**
     * 해당 기수에서 운영진(ChallengerRoleType이 있는 사람)인지 확인
     */
    private boolean isStaffInGisu(Long memberId, Long gisuId) {
        return !getChallengerRoleUseCase.getRolesByGisu(memberId, gisuId).isEmpty();
    }

    /**
     * 조회 권한 확인: 운영진 + 총괄 + 해당 출석 기록의 본인
     */
    private boolean canRead(SubjectAttributes subjectAttributes, AttendanceRecord record,
                            Schedule schedule, Long gisuId) {
        Long memberId = subjectAttributes.memberId();

        // 본인 출석 기록이면 OK
        if (Objects.equals(memberId, record.getMemberId())) {
            return true;
        }

        // 운영진/총괄이면 OK (canApprove와 동일한 조건)
        return canApprove(subjectAttributes, schedule, gisuId);
    }
}
