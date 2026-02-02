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

/**
 * 개인 출석 이력을 조회
 * <p>
 * 조회 흐름: 출석 기록(Record) → 출석부(Sheet) → 일정(Schedule)을 역추적. 개인 Id 받고 내 출석 기록 해당 sheetId, 거기서 일정으로 가고 조합해서 응답 생성
 * <p> 일정 정보와 출석 상태를 결합한 이력을 최신순으로 반환 일괄(batch)
 * <p> N+1 방지를 위해 ID 목록을 추출하여 조회
 */
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

        // 출석부 ID 목록 추출 및 일괄 조회
        List<Long> sheetIds = records.stream()
            .map(AttendanceRecord::getAttendanceSheetId)
            .distinct()
            .toList();

        Map<Long, AttendanceSheet> sheetByIdMap = loadAttendanceSheetPort.findAllByIds(sheetIds).stream()
            .collect(Collectors.toMap(AttendanceSheet::getId, Function.identity()));

        // 일정 ID 목록 추출 및 일괄 조회
        List<Long> scheduleIds = sheetByIdMap.values().stream()
            .map(AttendanceSheet::getScheduleId)
            .distinct()
            .toList();

        Map<Long, Schedule> scheduleMap = loadSchedulePort.findAllByIds(scheduleIds).stream()
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
