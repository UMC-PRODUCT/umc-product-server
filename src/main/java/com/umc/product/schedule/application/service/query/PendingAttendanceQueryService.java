package com.umc.product.schedule.application.service.query;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.schedule.application.port.in.query.GetAllPendingAttendancesUseCase;
import com.umc.product.schedule.application.port.in.query.GetPendingAttendancesUseCase;
import com.umc.product.schedule.application.port.in.query.dto.PendingAttendanceInfo;
import com.umc.product.schedule.application.port.in.query.dto.PendingAttendancesByScheduleInfo;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자용 - 승인 대기 중인(PENDING) 출석 기록 목록을 조회
 * <p>일정 ID로 출석부를 찾고, 해당 출석부의 PENDING 상태 기록을 멤버 정보와 함께 반환.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PendingAttendanceQueryService implements GetPendingAttendancesUseCase, GetAllPendingAttendancesUseCase {

    private final LoadAttendanceSheetPort loadAttendanceSheetPort;
    private final LoadAttendanceRecordPort loadAttendanceRecordPort;
    private final LoadSchedulePort loadSchedulePort;

    @Override
    public List<PendingAttendanceInfo> getPendingList(Long scheduleId) {
        AttendanceSheet sheet = loadAttendanceSheetPort.findByScheduleId(scheduleId)
            .orElseThrow(
                () -> new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.ATTENDANCE_SHEET_NOT_FOUND));

        return loadAttendanceRecordPort.findPendingWithMemberInfo(sheet.getId());
    }

    @Override
    public List<PendingAttendancesByScheduleInfo> getAllPendingList(Long gisuId) {
        // 1. 해당 기수의 활성 출석부 목록 조회
        List<AttendanceSheet> activeSheets = loadAttendanceSheetPort.findActiveSheetsByGisuId(gisuId);

        if (activeSheets.isEmpty()) {
            return List.of();
        }

        // 2. 출석부 ID 목록 추출
        List<Long> sheetIds = activeSheets.stream()
            .map(AttendanceSheet::getId)
            .toList();

        // 3. 모든 출석부의 승인 대기 기록 일괄 조회 (N+1 방지)
        // PendingAttendanceInfo에 scheduleId가 포함되어 있음
        List<PendingAttendanceInfo> allPendingRecords =
            loadAttendanceRecordPort.findPendingWithMemberInfoBySheetIds(sheetIds);

        if (allPendingRecords.isEmpty()) {
            return List.of();
        }

        // 4. scheduleId별로 그룹핑
        Map<Long, List<PendingAttendanceInfo>> groupedBySchedule = allPendingRecords.stream()
            .collect(Collectors.groupingBy(PendingAttendanceInfo::scheduleId));

        // 5. scheduleId로 일정 정보 일괄 조회 (일정명 포함)
        List<Long> scheduleIds = new ArrayList<>(groupedBySchedule.keySet());
        Map<Long, Schedule> scheduleMap = loadSchedulePort.findAllByIds(scheduleIds).stream()
            .collect(Collectors.toMap(Schedule::getId, schedule -> schedule));

        // 6. 결과 조합
        return groupedBySchedule.entrySet().stream()
            .map(entry -> {
                Long scheduleId = entry.getKey();
                List<PendingAttendanceInfo> pendingList = entry.getValue();
                Schedule schedule = scheduleMap.get(scheduleId);

                String scheduleName = schedule != null ? schedule.getName() : "알 수 없는 일정";

                return PendingAttendancesByScheduleInfo.of(scheduleId, scheduleName, pendingList);
            })
            .toList();
    }
}
