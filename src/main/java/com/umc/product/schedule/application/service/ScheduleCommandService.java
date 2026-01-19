package com.umc.product.schedule.application.service;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.schedule.application.port.in.command.CreateScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleCommand;
import com.umc.product.schedule.application.port.out.SaveAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.SaveAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.SaveSchedulePort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleCommandService implements CreateScheduleUseCase {

    private final SaveSchedulePort saveSchedulePort;
    private final SaveAttendanceSheetPort saveAttendanceSheetPort;
    private final SaveAttendanceRecordPort saveAttendanceRecordPort;

    // 외부 도메인 UseCase
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetGisuUseCase getGisuUseCase;


    @Override
    public Long create(CreateScheduleCommand command) {
        // 1. 현재 활성 기수 조회
        Long currentGisuId = getGisuUseCase.getActiveGisuId();

        // 2. 작성자의 Challenger 조회 (Member + 현재 기수 기반)
        Long authorChallengerId = getChallengerUseCase.getMemberGisuChallengerInfo(command.authorMemberId(),
                currentGisuId).challengerId();

        // TODO: 3. 참여자 Member 검증 (필요하다고 판단 시 추후 추가, 검색해서 들어가는 거라서 필요 없을 수도)

        // 4. Schedule 생성 및 저장
        Schedule schedule = command.toEntity(authorChallengerId);
        Schedule savedSchedule = saveSchedulePort.save(schedule);

        // 5. 참여자가 있을 시, 출석 관련 엔티티 생성
        if (command.hasParticipants()) {
            createAttendanceForParticipants(savedSchedule, command.participantMemberIds());
        }

        return null;
    }

    private void createAttendanceForParticipants(
            Schedule schedule,
            List<Long> participantMemberIds
    ) {
        // AttendanceSheet 생성
        AttendanceSheet sheet = AttendanceSheet.builder()
                .scheduleId(schedule.getId())
                .window(AttendanceWindow.ofDefault(schedule.getStartsAt()))
                .requiresApproval(false)
                .build();

        AttendanceSheet savedSheet = saveAttendanceSheetPort.save(sheet);

        // 각 참여자 Member AttendanceRecord 생성
        List<AttendanceRecord> records = participantMemberIds.stream()
                .map(memberId -> AttendanceRecord.builder()
                        .attendanceSheetId(savedSheet.getId())
                        .memberId(memberId)
                        .status(AttendanceStatus.PENDING)
                        .build())
                .toList();

        saveAttendanceRecordPort.saveAllRecords(records);
    }
}
