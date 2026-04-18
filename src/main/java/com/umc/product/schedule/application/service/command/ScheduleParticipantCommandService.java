package com.umc.product.schedule.application.service.command;

import com.umc.product.global.util.GeometryUtils;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.v2.in.command.CreateScheduleParticipantUseCase;
import com.umc.product.schedule.application.port.v2.in.command.UpdateScheduleParticipantUseCase;
import com.umc.product.schedule.application.port.v2.in.command.dto.DecideAttendanceCommand;
import com.umc.product.schedule.application.port.v2.in.command.dto.ExcuseScheduleAttendanceCommand;
import com.umc.product.schedule.application.port.v2.in.command.dto.ScheduleAttendanceCommand;
import com.umc.product.schedule.application.port.v2.in.query.dto.ScheduleParticipantAttendanceInfo;
import com.umc.product.schedule.application.port.v2.out.LoadScheduleParticipantPort;
import com.umc.product.schedule.application.port.v2.out.SaveScheduleParticipantPort;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.ScheduleParticipant;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleParticipantCommandService implements
    CreateScheduleParticipantUseCase,
    UpdateScheduleParticipantUseCase {

    private final LoadSchedulePort loadSchedulePort;

    private final SaveScheduleParticipantPort saveScheduleParticipantPort;
    private final LoadScheduleParticipantPort loadScheduleParticipantPort;

    private static void checkSchedulePolicyExists(Schedule schedule) {
        if (schedule.getPolicy() == null) {
            throw new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_ATTENDANCE_POLICY_NOT_EXIST);
        }
    }

    // 출석 요청
    @Override
    public ScheduleParticipantAttendanceInfo createScheduleParticipantWithAttendance(
        ScheduleAttendanceCommand command) {

        Schedule schedule = loadSchedulePort.findById(command.scheduleId())
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        // 출석을 요하지 않는, 즉 출석 정책이 없는 일정이면 에러 반환
        checkSchedulePolicyExists(schedule);

        // ScheduleParticipant 정보가 없으면 에러 반환
        ScheduleParticipant scheduleParticipant = getScheduleParticipant(command.scheduleId(),
            command.requesterMemberId());

        // 클라이언트에서 값을 안 주면 null (비대면인 경우를 고려)
        Point location = getLocation(command.latitude(), command.longitude());

        // scheduleParticipant에 연결되는 ScheduleParticipantAttendance를 저장
        // 이미 출석 요청 기록이 있으면 에러 반환
        // 이미 종료된 일정에 요청, 출석 시작 전 요청은 에러 반환
        scheduleParticipant.createAttendance(location, command.isVerified());

        // 요청 저장
        saveScheduleParticipantPort.save(scheduleParticipant);

        // decisionMakerMember는 null
        return ScheduleParticipantAttendanceInfo.of(scheduleParticipant.getAttendance(), null);
    }

    // 사유 제출
    @Override
    public ScheduleParticipantAttendanceInfo createExcusedScheduleParticipantWithAttendance(
        ExcuseScheduleAttendanceCommand command) {

        Schedule schedule = loadSchedulePort.findById(command.scheduleId())
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        // 출석을 요하지 않는, 즉 출석 정책이 없는 일정이면 에러 반환
        checkSchedulePolicyExists(schedule);

        // ScheduleParticipant 정보가 없으면 에러 반환
        ScheduleParticipant scheduleParticipant = getScheduleParticipant(command.scheduleId(),
            command.requesterMemberId());

        // 클라이언트에서 값을 안 주면 null
        Point location = getLocation(command.latitude(), command.longitude());

        // scheduleParticipant에 연결되는 ScheduleParticipantAttendance를 저장
        // 이미 출석 요청 기록이 있으면 에러 반환
        // 이미 종료된 일정에 요청, 출석 시작 전 요청은 에러 반환
        scheduleParticipant.submitExcuse(location, command.isVerified(), command.excuseReason());

        // 요청 저장
        saveScheduleParticipantPort.save(scheduleParticipant);

        // decisionMakerMember는 null
        return ScheduleParticipantAttendanceInfo.of(scheduleParticipant.getAttendance(), null);
    }

    // 출석 요청 승인/거절
    @Override
    public List<ScheduleParticipantAttendanceInfo> decideAttendances(DecideAttendanceCommand command) {
        Schedule schedule = loadSchedulePort.findById(command.scheduleId())
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        // 출석을 요하지 않는, 즉 출석 정책이 없는 일정이면 에러 반환
        checkSchedulePolicyExists(schedule);

        //
        return null;
    }

    private ScheduleParticipant getScheduleParticipant(Long scheduleId, Long memberId) {
        return loadScheduleParticipantPort
            .findByScheduleIdAndMemberId(scheduleId, memberId)
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.PARTICIPANT_NOT_FOUND));
    }

    private Point getLocation(Double latitude, Double longitude) {
        if (latitude != null && longitude != null) {
            return GeometryUtils.createPoint(latitude, longitude);
        }
        return null;
    }
}
