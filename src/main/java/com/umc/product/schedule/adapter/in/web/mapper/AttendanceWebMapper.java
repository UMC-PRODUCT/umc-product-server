package com.umc.product.schedule.adapter.in.web.mapper;

import static com.umc.product.schedule.domain.ScheduleConstants.KST;

import com.umc.product.schedule.adapter.in.web.dto.response.AttendanceRecordResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.AvailableAttendanceResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.MyAttendanceHistoryResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.PendingAttendanceResponse;
import com.umc.product.schedule.application.port.in.query.dto.AttendanceRecordInfo;
import com.umc.product.schedule.application.port.in.query.dto.AvailableAttendanceInfo;
import com.umc.product.schedule.application.port.in.query.dto.MyAttendanceHistoryInfo;
import com.umc.product.schedule.application.port.in.query.dto.PendingAttendanceInfo;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AttendanceWebMapper {

    // 출석 기록 단건
    public AttendanceRecordResponse toAttendanceRecordResponse(AttendanceRecordInfo info) {
        return new AttendanceRecordResponse(
            info.id() != null ? info.id().id() : null,
            info.attendanceSheetId(),
            info.memberId(),
            info.status().name(),
            info.memo()
        );
    }

    // 내가 현재 할 수 있는 출석 목록
    public AvailableAttendanceResponse toAvailableAttendanceResponse(AvailableAttendanceInfo info) {
        return new AvailableAttendanceResponse(
            info.scheduleId(),
            info.scheduleName(),
            info.tags().stream().map(ScheduleTag::name).toList(),
            info.startTime(),
            info.endTime(),
            info.sheetId(),
            info.recordId(),
            info.status().name(),
            info.statusDisplay(),
            info.locationVerified()  // 출석 시점의 위치 인증 여부
        );
    }

    public List<AvailableAttendanceResponse> toAvailableAttendanceResponses(List<AvailableAttendanceInfo> infos) {
        return infos.stream().map(this::toAvailableAttendanceResponse).toList();
    }

    // 내 출석 히스토리
    public MyAttendanceHistoryResponse toMyAttendanceHistoryResponse(MyAttendanceHistoryInfo info) {
        return new MyAttendanceHistoryResponse(
            info.attendanceId(),
            info.scheduleId(),
            info.scheduleName(),
            info.weekDisplay(),
            info.dateDisplay(),
            info.status().name(),
            info.statusDisplay()
        );
    }

    public List<MyAttendanceHistoryResponse> toMyAttendanceHistoryResponses(List<MyAttendanceHistoryInfo> infos) {
        return infos.stream().map(this::toMyAttendanceHistoryResponse).toList();
    }

    // 관리자용 승인 대기 목록
    public PendingAttendanceResponse toPendingAttendanceResponse(PendingAttendanceInfo info) {
        return new PendingAttendanceResponse(
            info.attendanceId(),
            info.memberId(),
            info.memberName(),
            info.nickname(),
            info.schoolName(),
            info.status().name(),
            info.reason(),
            info.requestedAt() != null ? info.requestedAt().atZone(KST).toLocalDateTime() : null
        );
    }

    public List<PendingAttendanceResponse> toPendingAttendanceResponses(List<PendingAttendanceInfo> infos) {
        return infos.stream().map(this::toPendingAttendanceResponse).toList();
    }
}
