package com.umc.product.schedule.application.service.query;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.SchoolInfo;
import com.umc.product.schedule.application.port.in.query.GetPendingAttendancesUseCase;
import com.umc.product.schedule.application.port.in.query.dto.PendingAttendanceInfo;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PendingAttendanceQueryService implements GetPendingAttendancesUseCase {

    private final LoadAttendanceSheetPort loadAttendanceSheetPort;
    private final LoadAttendanceRecordPort loadAttendanceRecordPort;
    private final GetMemberUseCase getMemberUseCase;
    private final GetSchoolUseCase getSchoolUseCase;

    @Override
    public List<PendingAttendanceInfo> getPendingList(Long scheduleId) {
        // 해당 일정의 출석부 조회
        AttendanceSheet sheet = loadAttendanceSheetPort.findByScheduleId(scheduleId)
                .orElseThrow(
                        () -> new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.ATTENDANCE_SHEET_NOT_FOUND));

        // 승인 대기 출석 기록 조회
        List<AttendanceRecord> pendingRecords = loadAttendanceRecordPort.findPendingRecordsBySheetId(sheet.getId());

        if (pendingRecords.isEmpty()) {
            return List.of();
        }

        // 멤버 정보 조회 및 결과 생성
        return pendingRecords.stream()
                .map(record -> {
                    MemberInfo member = getMemberUseCase.getById(record.getMemberId());
                    String schoolName = "";
                    if (member.schoolId() != null) {
                        try {
                            SchoolInfo school = getSchoolUseCase.getSchoolDetail(member.schoolId());
                            schoolName = school.schoolName();
                        } catch (Exception e) {
                            schoolName = "";
                        }
                    }
                    return PendingAttendanceInfo.from(record, member.name(), member.nickname(), schoolName);
                })
                .toList();
    }
}
