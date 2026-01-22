package com.umc.product.schedule.application.service.query;

import com.umc.product.schedule.application.port.in.query.GetMyAttendanceHistoryUseCase;
import com.umc.product.schedule.application.port.in.query.dto.MyAttendanceHistoryInfo;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
public class AttendanceHistoryQueryService implements GetMyAttendanceHistoryUseCase {

    private final LoadSchedulePort loadSchedulePort;
    private final LoadAttendanceSheetPort loadAttendanceSheetPort;
    private final LoadAttendanceRecordPort loadAttendanceRecordPort;

    @Override
    public List<MyAttendanceHistoryInfo> getHistory(Long memberId) {
        // 해당 멤버의 모든 출석 기록 조회
        List<AttendanceRecord> records = loadAttendanceRecordPort.findByMemberId(memberId);

        if (records.isEmpty()) {
            return List.of();
        }

        // 출석부 ID 목록 추출
        List<Long> sheetIds = records.stream()
                .map(AttendanceRecord::getAttendanceSheetId)
                .distinct()
                .toList();

        // 출석부 조회
        Map<Long, AttendanceSheet> sheetByIdMap = sheetIds.stream()
                .map(id -> loadAttendanceSheetPort.findById(id).orElse(null))
                .filter(s -> s != null)
                .collect(Collectors.toMap(AttendanceSheet::getId, Function.identity()));

        // 일정 ID 목록 추출
        List<Long> scheduleIds = sheetByIdMap.values().stream()
                .map(AttendanceSheet::getScheduleId)
                .distinct()
                .toList();

        // 일정 조회
        Map<Long, Schedule> scheduleMap = scheduleIds.stream()
                .map(id -> loadSchedulePort.findById(id).orElse(null))
                .filter(s -> s != null)
                .collect(Collectors.toMap(Schedule::getId, Function.identity()));

        // 결과 생성 (최신순 정렬)
        return records.stream()
                .map(record -> {
                    AttendanceSheet sheet = sheetByIdMap.get(record.getAttendanceSheetId());
                    if (sheet == null) {
                        return null;
                    }

                    Schedule schedule = scheduleMap.get(sheet.getScheduleId());
                    if (schedule == null) {
                        return null;
                    }

                    // 주차 계산 (임시: 해당 연도의 첫 번째 월요일부터 계산)
                    int weekNo = calculateWeekNumber(schedule.getStartsAt());
                    return MyAttendanceHistoryInfo.of(schedule, record, weekNo);
                })
                .filter(info -> info != null)
                .sorted(Comparator.comparing(MyAttendanceHistoryInfo::scheduleId).reversed())
                .toList();
    }

    private int calculateWeekNumber(LocalDateTime dateTime) {
        // 간단한 주차 계산 (해당 년도의 1월 1일부터 몇 주차인지)
        LocalDateTime startOfYear = dateTime.withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
        long daysBetween = ChronoUnit.DAYS.between(startOfYear, dateTime);
        return (int) (daysBetween / 7) + 1;
    }
}
