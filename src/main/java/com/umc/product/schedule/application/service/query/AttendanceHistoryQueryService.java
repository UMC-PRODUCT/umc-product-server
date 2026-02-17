package com.umc.product.schedule.application.service.query;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.schedule.application.port.in.query.GetChallengerAttendanceHistoryUseCase;
import com.umc.product.schedule.application.port.in.query.GetMyAttendanceHistoryUseCase;
import com.umc.product.schedule.application.port.in.query.dto.MyAttendanceHistoryInfo;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 개인 출석 이력을 조회
 * <p>
 * 조회 흐름:
 * 1. gisuId로 해당 기수의 출석부 목록 조회
 * 2. memberId의 출석 기록 중 해당 기수 출석부에 속한 것만 필터링
 * 3. 일정 정보와 결합하여 최신순 반환
 * <p>
 * N+1 방지를 위해 ID 목록을 추출하여 일괄 조회
 * <p>
 * ⚠️ 확정된 상태만 반환: PRESENT, LATE, ABSENT, EXCUSED
 * - PENDING (출석 전) 제외
 * - *_PENDING (승인 대기) 제외
 * - EXCUSED 포함 - 관리자가 직접 상태 변경 시 사용 가능
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceHistoryQueryService implements GetMyAttendanceHistoryUseCase, GetChallengerAttendanceHistoryUseCase {

    private static final Set<AttendanceStatus> CONFIRMED_STATUSES = Set.of(
        AttendanceStatus.PRESENT,
        AttendanceStatus.LATE,
        AttendanceStatus.ABSENT,
        AttendanceStatus.EXCUSED  // 관리자 직접 변경 시 사용 가능
    );

    private final LoadSchedulePort loadSchedulePort;
    private final LoadAttendanceSheetPort loadAttendanceSheetPort;
    private final LoadAttendanceRecordPort loadAttendanceRecordPort;
    private final GetChallengerUseCase getChallengerUseCase;

    @Override
    public List<MyAttendanceHistoryInfo> getHistory(Long memberId, Long gisuId) {
        // 1. 해당 기수의 출석부 목록 조회
        List<AttendanceSheet> sheets = loadAttendanceSheetPort.findByGisuId(gisuId);
        if (sheets.isEmpty()) {
            return List.of();
        }

        Set<Long> sheetIds = sheets.stream()
            .map(AttendanceSheet::getId)
            .collect(Collectors.toSet());

        Map<Long, AttendanceSheet> sheetByIdMap = sheets.stream()
            .collect(Collectors.toMap(AttendanceSheet::getId, Function.identity()));

        // 2. 해당 멤버의 출석 기록 중 해당 기수 출석부에 속한 것만 필터링
        // ⚠️ 확정된 상태만 포함 (PENDING, *_PENDING 제외)
        List<AttendanceRecord> records = loadAttendanceRecordPort.findByMemberId(memberId).stream()
            .filter(record -> sheetIds.contains(record.getAttendanceSheetId()))
            .filter(record -> CONFIRMED_STATUSES.contains(record.getStatus()))
            .toList();

        if (records.isEmpty()) {
            return List.of();
        }

        // 3. 일정 ID 목록 추출 및 일괄 조회
        List<Long> scheduleIds = sheetByIdMap.values().stream()
            .map(AttendanceSheet::getScheduleId)
            .distinct()
            .toList();

        Map<Long, Schedule> scheduleMap = loadSchedulePort.findAllByIds(scheduleIds).stream()
            .collect(Collectors.toMap(Schedule::getId, Function.identity()));

        // 4. 결과 생성 (최신순 정렬)
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

                return MyAttendanceHistoryInfo.of(schedule, sheet, record);
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(MyAttendanceHistoryInfo::scheduledAt).reversed())
            .toList();
    }

    @Override
    public List<MyAttendanceHistoryInfo> getHistoryByChallengerId(Long challengerId) {
        // 1. 챌린저 정보 조회
        ChallengerInfo challengerInfo = getChallengerUseCase.getChallengerPublicInfo(challengerId);

        // 2. memberId와 gisuId를 이용하여 기존 메서드 재사용
        return getHistory(challengerInfo.memberId(), challengerInfo.gisuId());
    }
}
