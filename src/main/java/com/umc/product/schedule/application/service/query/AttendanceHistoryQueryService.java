package com.umc.product.schedule.application.service.query;

import com.umc.product.schedule.application.port.in.query.GetMyAttendanceHistoryUseCase;
import com.umc.product.schedule.application.port.in.query.dto.MyAttendanceHistoryInfo;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        List<AttendanceRecord> records = loadAttendanceRecordPort.findByMemberId(memberId);

        if (records.isEmpty()) {
            return List.of();
        }

        // 출석부 ID 목록 추출 및 조회
        List<Long> sheetIds = records.stream()
                .map(AttendanceRecord::getAttendanceSheetId)
                .distinct()
                .toList();

        Map<Long, AttendanceSheet> sheetByIdMap = sheetIds.stream()
                .map(id -> loadAttendanceSheetPort.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(AttendanceSheet::getId, Function.identity()));

        // 일정 ID 목록 추출 및 조회
        List<Long> scheduleIds = sheetByIdMap.values().stream()
                .map(AttendanceSheet::getScheduleId)
                .distinct()
                .toList();

        Map<Long, Schedule> scheduleMap = scheduleIds.stream()
                .map(id -> loadSchedulePort.findById(id).orElse(null))
                .filter(Objects::nonNull)
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

                    return MyAttendanceHistoryInfo.of(schedule, record);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(MyAttendanceHistoryInfo::scheduledAt).reversed())
                .toList();
    }
}
