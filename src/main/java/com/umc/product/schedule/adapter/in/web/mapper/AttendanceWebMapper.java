package com.umc.product.schedule.adapter.in.web.mapper;

import com.umc.product.schedule.adapter.in.web.dto.response.AttendanceRecordResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.AvailableAttendanceResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.MyAttendanceHistoryResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.PendingAttendanceResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.PendingAttendancesByScheduleResponse;
import com.umc.product.schedule.application.port.in.query.dto.AttendanceRecordInfo;
import com.umc.product.schedule.application.port.in.query.dto.AvailableAttendanceInfo;
import com.umc.product.schedule.application.port.in.query.dto.MyAttendanceHistoryInfo;
import com.umc.product.schedule.application.port.in.query.dto.PendingAttendanceInfo;
import com.umc.product.schedule.application.port.in.query.dto.PendingAttendancesByScheduleInfo;
import com.umc.product.schedule.domain.ScheduleConstants;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendanceWebMapper {

    private final GetFileUseCase getFileUseCase;

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
            info.tags().stream().map(ScheduleTag::name).toList(),
            info.scheduledDate(),
            info.startTime(),
            info.endTime(),
            info.status().name(),
            info.statusDisplay(),
            info.sheetId(),
            info.locationName(),
            info.locationVerified(),
            info.memo(),
            info.checkedAt() != null
                ? info.checkedAt().atZone(ScheduleConstants.KST).toLocalDateTime()
                : null
        );
    }

    public List<MyAttendanceHistoryResponse> toMyAttendanceHistoryResponses(List<MyAttendanceHistoryInfo> infos) {
        return infos.stream().map(this::toMyAttendanceHistoryResponse).toList();
    }

    // 관리자용 승인 대기 목록
    public List<PendingAttendanceResponse> toPendingAttendanceResponses(List<PendingAttendanceInfo> infos) {
        Map<String, String> profileImageLinks = buildProfileImageLinks(infos);
        return infos.stream()
            .map(info -> toPendingAttendanceResponse(info, profileImageLinks))
            .toList();
    }

    // 관리자용 전체 승인 대기 목록 (일정별 그룹핑)
    public List<PendingAttendancesByScheduleResponse> toPendingAttendancesByScheduleResponses(
        List<PendingAttendancesByScheduleInfo> infos
    ) {
        List<PendingAttendanceInfo> allPendingInfos = infos.stream()
            .flatMap(scheduleInfo -> scheduleInfo.pendingAttendances().stream())
            .toList();
        Map<String, String> profileImageLinks = buildProfileImageLinks(allPendingInfos);

        return infos.stream()
            .map(scheduleInfo -> new PendingAttendancesByScheduleResponse(
                scheduleInfo.scheduleId(),
                scheduleInfo.scheduleName(),
                scheduleInfo.pendingAttendances().stream()
                    .map(info -> toPendingAttendanceResponse(info, profileImageLinks))
                    .toList()
            ))
            .toList();
    }

    private Map<String, String> buildProfileImageLinks(List<PendingAttendanceInfo> infos) {
        List<String> profileImageIds = infos.stream()
            .map(PendingAttendanceInfo::profileImageId)
            .filter(id -> id != null)
            .distinct()
            .toList();

        return profileImageIds.isEmpty()
            ? Map.of()
            : getFileUseCase.getFileLinks(profileImageIds);
    }

    // TODO: DTO에 정적 팩토리 메소드로 변경할 것
    private PendingAttendanceResponse toPendingAttendanceResponse(
        PendingAttendanceInfo info, Map<String, String> profileImageLinks
    ) {
        String profileImageLink = info.profileImageId() != null
            ? profileImageLinks.get(info.profileImageId())
            : null;

        return new PendingAttendanceResponse(
            info.attendanceId(),
            info.memberId(),
            info.memberName(),
            info.nickname(),
            profileImageLink,
            info.schoolName(),
            info.status().name(),
            info.reason(),
            info.requestedAt() != null
                ? info.requestedAt()
                : null
        );
    }
}
