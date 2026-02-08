package com.umc.product.schedule.application.service.query;

import com.umc.product.schedule.application.port.in.query.GetScheduleListUseCase;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleWithStatsInfo;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.vo.AttendanceStats;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleWithStatsQueryService implements GetScheduleListUseCase {

    private final LoadSchedulePort loadSchedulePort;
    private final LoadAttendanceSheetPort loadAttendanceSheetPort;
    private final LoadAttendanceRecordPort loadAttendanceRecordPort;

    @Override
    public List<ScheduleWithStatsInfo> getAll() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 모든 일정 조회
        List<Schedule> schedules = loadSchedulePort.findAll();

        if (schedules.isEmpty()) {
            return List.of();
        }

        // 2. 일정 ID 목록 추출
        List<Long> scheduleIds = schedules.stream()
            .map(Schedule::getId)
            .toList();

        // 3. 해당 일정들의 출석부 조회
        List<AttendanceSheet> sheets = loadAttendanceSheetPort.findByScheduleIds(scheduleIds);
        Map<Long, AttendanceSheet> sheetByScheduleId = sheets.stream()
            .collect(Collectors.toMap(AttendanceSheet::getScheduleId, Function.identity()));

        // 4. 출석부 ID 목록 추출
        List<Long> sheetIds = sheets.stream()
            .map(AttendanceSheet::getId)
            .toList();

        // 5. 출석 기록 조회 및 출석부별로 그룹화
        Map<Long, List<AttendanceRecord>> recordsBySheetId = sheetIds.isEmpty()
            ? Map.of()
            : loadAttendanceRecordPort.findByAttendanceSheetIds(sheetIds).stream()
                .collect(Collectors.groupingBy(AttendanceRecord::getAttendanceSheetId));

        // 6. 각 일정에 대해 통계 계산 및 Info 생성
        List<ScheduleWithStatsInfo> result = schedules.stream()
            .map(schedule -> {
                AttendanceSheet sheet = sheetByScheduleId.get(schedule.getId());
                AttendanceStats stats = calculateStats(sheet, recordsBySheetId);
                return ScheduleWithStatsInfo.of(schedule, stats, now);
            })
            .toList();

        // 7. "예정" 상태 제외 및 정렬: 진행 중 → 종료됨 (종료됨은 최근 것부터)
        return result.stream()
            .filter(info -> !"예정".equals(info.status()))
            .sorted(scheduleComparator(now))
            .toList();
    }

    private AttendanceStats calculateStats(AttendanceSheet sheet, Map<Long, List<AttendanceRecord>> recordsBySheetId) {
        if (sheet == null) {
            return new AttendanceStats(0, 0, 0);
        }

        List<AttendanceRecord> records = recordsBySheetId.getOrDefault(sheet.getId(), List.of());

        int totalCount = records.size();
        int presentCount = (int) records.stream()
            .filter(r -> r.getStatus() == AttendanceStatus.PRESENT
                || r.getStatus() == AttendanceStatus.LATE
                || r.getStatus() == AttendanceStatus.EXCUSED)
            .count();
        int pendingCount = (int) records.stream()
            .filter(r -> r.getStatus().isPending())
            .count();

        return new AttendanceStats(totalCount, presentCount, pendingCount);
    }

    private Comparator<ScheduleWithStatsInfo> scheduleComparator(LocalDateTime now) {
        return (a, b) -> {
            int orderA = getStatusOrder(a.status());
            int orderB = getStatusOrder(b.status());

            if (orderA != orderB) {
                return Integer.compare(orderA, orderB);
            }

            // 같은 상태일 경우: 진행 중은 시작 시간 오름차순, 종료됨은 종료 시간 내림차순
            if ("종료됨".equals(a.status())) {
                return b.endsAt().compareTo(a.endsAt());
            }
            return a.startsAt().compareTo(b.startsAt());
        };
    }

    private int getStatusOrder(String status) {
        return switch (status) {
            case "진행 중" -> 0;
            case "예정" -> 1;
            case "종료됨" -> 2;
            default -> 3;
        };
    }
}
