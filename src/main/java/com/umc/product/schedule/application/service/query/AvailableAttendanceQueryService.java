package com.umc.product.schedule.application.service.query;

import com.umc.product.schedule.application.port.in.query.GetAvailableAttendancesUseCase;
import com.umc.product.schedule.application.port.in.query.dto.AvailableAttendanceInfo;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 현재 출석 체크 가능한 일정 목록을 조회
 *
 * <p>해당 기수의 활성 출석부 중 세션이 종료(schedule.endsAt)되지 않은 일정만 반환.
 * <p>시간대 구분:
 * <ul>
 *   <li>window.startTime ~ window.endTime : 출석 인정 시간 (PRESENT/LATE 판정)</li>
 *   <li>window.endTime ~ schedule.endsAt  : 지각 시간 (출석 신청 가능, 결과는 LATE)</li>
 *   <li>schedule.endsAt ~                 : 결석 시간 (출석 신청 불가, 세션 목록에서 제외)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvailableAttendanceQueryService implements GetAvailableAttendancesUseCase {

    private static final Set<AttendanceStatus> COMPLETED_STATUSES = Set.of(
        // 출석 시간대 내에서는 출석 완료(PRESENT)도 확인 가능
        // 시간대가 지나면 isWithinTimeWindow()에서 걸러짐 → "나의 출석 현황"으로 이동
        AttendanceStatus.LATE,     // 지각 확정 - 제외
        AttendanceStatus.ABSENT,   // 결석 확정 - 제외
        AttendanceStatus.EXCUSED   // 인정결석 확정 - 제외
        // 포함되는 상태:
        // - PENDING: 출석 전
        // - PRESENT: 출석 완료 (시간대 내에서 확인 가능)
        // - PRESENT_PENDING: 출석 승인 대기
        // - LATE_PENDING: 지각 승인 대기
        // - EXCUSED_PENDING: 사유 제출 승인 대기
    );

    private final LoadSchedulePort loadSchedulePort;
    private final LoadAttendanceSheetPort loadAttendanceSheetPort;
    private final LoadAttendanceRecordPort loadAttendanceRecordPort;

    @Override
    public List<AvailableAttendanceInfo> getAvailableList(Long memberId, Long gisuId) {
        // 해당 기수의 활성 출석부 조회
        List<AttendanceSheet> activeSheets = loadAttendanceSheetPort.findActiveSheetsByGisuId(gisuId);

        if (activeSheets.isEmpty()) {
            return List.of();
        }

        // 해당 출석부들의 일정 조회
        List<Long> scheduleIds = activeSheets.stream()
            .map(AttendanceSheet::getScheduleId)
            .toList();

        Map<Long, Schedule> scheduleMap = loadSchedulePort.findAllByIds(scheduleIds).stream()
            .collect(Collectors.toMap(Schedule::getId, Function.identity()));

        // 해당 멤버의 출석 기록 조회
        List<Long> sheetIds = activeSheets.stream()
            .map(AttendanceSheet::getId)
            .toList();

        List<AttendanceRecord> memberRecords = loadAttendanceRecordPort.findByMemberId(memberId);
        Map<Long, AttendanceRecord> recordBySheetId = memberRecords.stream()
            .filter(r -> sheetIds.contains(r.getAttendanceSheetId()))
            .collect(Collectors.toMap(
                AttendanceRecord::getAttendanceSheetId,
                Function.identity()));

        // 결과 생성
        Instant now = Instant.now();
        return activeSheets.stream()
            .map(sheet -> {
                Schedule schedule = scheduleMap.get(sheet.getScheduleId());
                AttendanceRecord record = recordBySheetId.get(sheet.getId());

                // 1. 일정이 없으면 제외
                if (schedule == null) {
                    return null;
                }

                // 2. 결석 시간(세션 종료) 이후 제외 → 나의 출석 현황으로 이동
                //    지각 시간(window.endTime ~ schedule.endsAt)에는 여전히 출석 신청 가능
                if (now.isAfter(schedule.getEndsAt())) {
                    return null;
                }

                // 3. 결석/인정결석 확정 상태는 제외 (더 이상 출석 신청 불필요)
                if (record != null && COMPLETED_STATUSES.contains(record.getStatus())) {
                    return null;
                }

                // 4. 출석 완료(PRESENT)는 출석 시간대(window) 이후에는 "나의 출석 현황"으로 이동
                if (record != null
                    && record.getStatus() == AttendanceStatus.PRESENT
                    && !sheet.isWithinTimeWindow(now)) {
                    return null;
                }

                // 출석 기록이 없으면 해당 일정에 추가되지 않은 참여자 → 제외
                if (record == null) {
                    return null;
                }
                return AvailableAttendanceInfo.of(schedule, sheet, record);
            })
            .filter(info -> info != null)
            .sorted(Comparator.comparing(AvailableAttendanceInfo::startTime))
            .toList();
    }
}
