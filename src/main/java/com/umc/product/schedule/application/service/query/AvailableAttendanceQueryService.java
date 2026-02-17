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
import java.time.LocalDateTime;
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
 * <p>조회 가능 시간: 일정 시작 10분 전 ~ 일정 종료 시간
 * <p>출석 체크 로직: 10분 전~10분 후(출석), 10분 후~종료(지각), 종료 후(결석)
 * <p>해당 멤버의 기존 출석 기록이 있으면 함께 가져옴.
 * <p>시작 시간 오름차순.
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
        LocalDateTime now = LocalDateTime.now();
        return activeSheets.stream()
            .map(sheet -> {
                Schedule schedule = scheduleMap.get(sheet.getScheduleId());
                AttendanceRecord record = recordBySheetId.get(sheet.getId());

                // 필터링: 출석 가능한 것만
                // 1. 일정이 없으면 제외
                if (schedule == null) {
                    return null;
                }

                // 2. 출석 가능 시간 체크 - 일정 10분 전부터 일정 종료까지 조회 가능
                LocalDateTime windowStart = sheet.getWindow().getStartTime();  // 일정 10분 전
                LocalDateTime scheduleEnd = schedule.getEndsAt();              // 일정 종료 시간

                if (now.isBefore(windowStart) || now.isAfter(scheduleEnd)) {
                    return null;
                }

                // 3. 출석 프로세스 완료된 것 제외 (나의 출석 현황으로 이동)
                // 포함: PENDING (출석 전), PRESENT/PRESENT_PENDING (출석 완료), EXCUSED_PENDING (사유 제출 승인 대기)
                // 제외: LATE/LATE_PENDING (지각), ABSENT (결석), EXCUSED (인정결석 승인 완료)
                if (record != null && isAttendanceProcessCompleted(record.getStatus())) {
                    return null;
                }

                if (record != null) {
                    return AvailableAttendanceInfo.of(schedule, sheet, record);
                }
                return AvailableAttendanceInfo.of(schedule, sheet);
            })
            .filter(info -> info != null)
            .sorted(Comparator.comparing(AvailableAttendanceInfo::startTime))
            .toList();
    }

    private boolean isAttendanceProcessCompleted(AttendanceStatus status) {
        return COMPLETED_STATUSES.contains(status);
    }
}
