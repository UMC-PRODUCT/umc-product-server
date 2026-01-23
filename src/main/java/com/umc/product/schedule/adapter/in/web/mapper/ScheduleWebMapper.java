package com.umc.product.schedule.adapter.in.web.mapper;

import com.umc.product.schedule.adapter.in.web.dto.response.AttendanceRecordResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.AttendanceSheetResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.AvailableAttendanceResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.MyAttendanceHistoryResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.PendingAttendanceResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.ScheduleListResponse;
import com.umc.product.schedule.application.port.in.query.dto.AttendanceRecordInfo;
import com.umc.product.schedule.application.port.in.query.dto.AttendanceSheetInfo;
import com.umc.product.schedule.application.port.in.query.dto.AvailableAttendanceInfo;
import com.umc.product.schedule.application.port.in.query.dto.MyAttendanceHistoryInfo;
import com.umc.product.schedule.application.port.in.query.dto.PendingAttendanceInfo;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleWithStatsInfo;
import java.util.List;
import org.springframework.stereotype.Component;

// TODO : 주석 처리 부분 tags 로 변경
@Component
public class ScheduleWebMapper {

    // Schedule
    public ScheduleListResponse toScheduleListResponse(ScheduleWithStatsInfo info) {
        return new ScheduleListResponse(
                info.scheduleId(),
                info.name(),
//                info.type().name(),
                info.status(),
                info.startsAt(),
                info.startsAt(),
                info.endsAt(),
                info.locationName(),
                info.totalCount(),
                info.presentCount(),
                info.pendingCount(),
                info.attendanceRate()
        );
    }

    public List<ScheduleListResponse> toScheduleListResponses(List<ScheduleWithStatsInfo> infos) {
        return infos.stream().map(this::toScheduleListResponse).toList();
    }

    // AttendanceSheet
    public AttendanceSheetResponse toAttendanceSheetResponse(AttendanceSheetInfo info) {
        return new AttendanceSheetResponse(
                info.id() != null ? info.id().id() : null,
                info.scheduleId(),
                info.window().getStartTime(),
                info.window().getEndTime(),
                info.window().getLateThresholdMinutes(),
                info.requiresApproval(),
                info.active()
        );
    }

    // AttendanceRecord
    public AttendanceRecordResponse toAttendanceRecordResponse(AttendanceRecordInfo info) {
        return new AttendanceRecordResponse(
                info.id() != null ? info.id().id() : null,
                info.attendanceSheetId(),
                info.memberId(),
                info.status().name(),
                info.memo()
        );
    }

    // AvailableAttendance
    public AvailableAttendanceResponse toAvailableAttendanceResponse(AvailableAttendanceInfo info) {
        return new AvailableAttendanceResponse(
                info.scheduleId(),
                info.scheduleName(),
//                info.scheduleType().name(),
                info.startTime(),
                info.endTime(),
                info.sheetId(),
                info.recordId(),
                info.status().name(),
                info.statusDisplay()
        );
    }

    public List<AvailableAttendanceResponse> toAvailableAttendanceResponses(List<AvailableAttendanceInfo> infos) {
        return infos.stream().map(this::toAvailableAttendanceResponse).toList();
    }

    // MyAttendanceHistory
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

    // PendingAttendance
    public PendingAttendanceResponse toPendingAttendanceResponse(PendingAttendanceInfo info) {
        return new PendingAttendanceResponse(
                info.attendanceId(),
                info.challengerId(),
                info.memberName(),
                info.nickname(),
                info.schoolName(),
                info.status().name(),
                info.reason(),
                info.requestedAt()
        );
    }

    public List<PendingAttendanceResponse> toPendingAttendanceResponses(List<PendingAttendanceInfo> infos) {
        return infos.stream().map(this::toPendingAttendanceResponse).toList();
    }
}
