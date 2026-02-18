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
import com.umc.product.schedule.domain.enums.ScheduleTag;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
            info.checkedAt()
        );
    }

    public List<MyAttendanceHistoryResponse> toMyAttendanceHistoryResponses(List<MyAttendanceHistoryInfo> infos) {
        return infos.stream().map(this::toMyAttendanceHistoryResponse).toList();
    }

    // 관리자용 승인 대기 목록
    public List<PendingAttendanceResponse> toPendingAttendanceResponses(List<PendingAttendanceInfo> infos) {
        // N+1 문제 방지: 모든 profileImageId를 수집하여 한 번에 조회
        List<String> profileImageIds = infos.stream()
            .map(PendingAttendanceInfo::profileImageId)
            .filter(id -> id != null)
            .distinct()
            .toList();

        // 파일 ID -> 파일 링크 매핑을 한 번에 조회
        Map<String, String> profileImageLinks = profileImageIds.isEmpty()
            ? Map.of()
            : getFileUseCase.getFileLinks(profileImageIds);

        // 각 항목을 변환하면서 Map에서 파일 링크 조회
        return infos.stream()
            .map(info -> {
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
                    info.requestedAt()
                );
            })
            .toList();
    }

    // 관리자용 전체 승인 대기 목록 (일정별 그룹핑)
    public List<PendingAttendancesByScheduleResponse> toPendingAttendancesByScheduleResponses(
        List<PendingAttendancesByScheduleInfo> infos
    ) {
        // N+1 방지: 모든 승인 대기 정보에서 profileImageId 수집
        List<String> allProfileImageIds = infos.stream()
            .flatMap(scheduleInfo -> scheduleInfo.pendingAttendances().stream())
            .map(PendingAttendanceInfo::profileImageId)
            .filter(id -> id != null)
            .distinct()
            .toList();

        // 파일 ID -> 파일 링크 매핑을 한 번에 조회
        Map<String, String> profileImageLinks = allProfileImageIds.isEmpty()
            ? Map.of()
            : getFileUseCase.getFileLinks(allProfileImageIds);

        // 각 일정별 그룹을 변환
        return infos.stream()
            .map(scheduleInfo -> {
                // 각 일정의 승인 대기 목록 변환
                List<PendingAttendanceResponse> pendingResponses = scheduleInfo.pendingAttendances().stream()
                    .map(info -> {
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
                            info.requestedAt()
                        );
                    })
                    .toList();

                return new PendingAttendancesByScheduleResponse(
                    scheduleInfo.scheduleId(),
                    scheduleInfo.scheduleName(),
                    pendingResponses
                );
            })
            .toList();
    }
}
