package com.umc.product.schedule.application.service.command;

import com.umc.product.global.util.GeometryUtils;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.v2.in.command.CreateScheduleParticipantUseCase;
import com.umc.product.schedule.application.port.v2.in.command.dto.ScheduleAttendanceRequestCommand;
import com.umc.product.schedule.application.port.v2.in.query.dto.ScheduleParticipantAttendanceInfo;
import com.umc.product.schedule.application.port.v2.out.LoadScheduleParticipantPort;
import com.umc.product.schedule.application.port.v2.out.SaveScheduleParticipantPort;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.ScheduleParticipant;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleParticipantCommandService implements CreateScheduleParticipantUseCase {

    private final LoadSchedulePort loadSchedulePort;

    private final SaveScheduleParticipantPort saveScheduleParticipantPort;
    private final LoadScheduleParticipantPort loadScheduleParticipantPort;

    @Override
    public ScheduleParticipantAttendanceInfo createScheduleParticipantWithAttendance(
        ScheduleAttendanceRequestCommand command) {

        Schedule schedule = loadSchedulePort.findById(command.scheduleId())
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        // 비대면 일정이면 에러 반환
        if (schedule.getLocation() == null) {
            throw new ScheduleDomainException(ScheduleErrorCode.ONLINE_SCHEDULE_ATTENDANCE_REQUEST_IMPOSSIBLE);
        }

        ScheduleParticipant scheduleParticipant = loadScheduleParticipantPort
            .findByScheduleIdAndMemberId(command.scheduleId(), command.requesterMemberId())
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.PARTICIPANT_NOT_FOUND));

        Point location = GeometryUtils.createPoint(command.latitude(), command.longitude());

        // scheduleParticipant에 연결되는 ScheduleParticipantAttendance를 저장
        // 이미 출석 요청 기록이 있으면 에러 반환
        // 이미 종료된 일정에 요청, 출석 시작 전 요청은 에러 반환
        scheduleParticipant.createAttendance(location, command.isVerified());

        // 요청 저장
        saveScheduleParticipantPort.save(scheduleParticipant);

        // 최초 요청이기 때문에 decisionMakerMember는 null
        return ScheduleParticipantAttendanceInfo.of(scheduleParticipant.getAttendance(), null);
    }
}
