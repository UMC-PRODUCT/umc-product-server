package com.umc.product.schedule.adapter.in.web.v1;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.schedule.adapter.in.web.v1.dto.request.CheckAttendanceRequest;
import com.umc.product.schedule.adapter.in.web.v1.dto.request.SubmitReasonRequest;
import com.umc.product.schedule.adapter.in.web.v1.dto.response.AttendanceRecordResponse;
import com.umc.product.schedule.adapter.in.web.v1.dto.response.AvailableAttendanceResponse;
import com.umc.product.schedule.adapter.in.web.v1.dto.response.MyAttendanceHistoryResponse;
import com.umc.product.schedule.adapter.in.web.v1.dto.response.PendingAttendanceResponse;
import com.umc.product.schedule.adapter.in.web.v1.dto.response.PendingAttendancesByScheduleResponse;
import com.umc.product.schedule.adapter.in.web.v1.swagger.AttendanceControllerApi;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
public class AttendanceController implements AttendanceControllerApi {

    @Override
    @PostMapping("/check")
    public Long checkAttendance(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestBody CheckAttendanceRequest request
    ) {
        throw new NotImplementedException();
    }

    @Override
    @PostMapping("/reason")
    public Long submitReasonAttendance(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestBody SubmitReasonRequest request
    ) {
        throw new NotImplementedException();
    }

    @Override
    @GetMapping("/available")
    public List<AvailableAttendanceResponse> getAvailableAttendances(
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        throw new NotImplementedException();
    }

    @Override
    @GetMapping("/history")
    public List<MyAttendanceHistoryResponse> getMyAttendanceHistory(
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        throw new NotImplementedException();
    }

    @Override
    @GetMapping("/challenger/{challengerId}/history")
    public List<MyAttendanceHistoryResponse> getChallengerAttendanceHistory(
        @PathVariable Long challengerId
    ) {
        throw new NotImplementedException();
    }

    @Override
    @GetMapping("/{recordId}")
    @CheckAccess(
        resourceType = ResourceType.ATTENDANCE_RECORD,
        resourceId = "#recordId",
        permission = PermissionType.READ
    )
    public AttendanceRecordResponse getAttendanceRecord(
        @PathVariable Long recordId
    ) {
        throw new NotImplementedException();
    }

    @Override
    @GetMapping("/pending/{scheduleId}")
    @CheckAccess(
        resourceType = ResourceType.SCHEDULE,
        resourceId = "#scheduleId",
        permission = PermissionType.APPROVE
    )
    public List<PendingAttendanceResponse> getPendingAttendances(
        @PathVariable Long scheduleId
    ) {
        throw new NotImplementedException();
    }

    @Override
    @GetMapping("/pending")
    public List<PendingAttendancesByScheduleResponse> getAllPendingAttendances(
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        throw new NotImplementedException();
    }

    @Override
    @PostMapping("/{recordId}/approve")
    @CheckAccess(
        resourceType = ResourceType.ATTENDANCE_RECORD,
        resourceId = "#recordId",
        permission = PermissionType.APPROVE
    )
    public void approveAttendance(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long recordId
    ) {
        throw new NotImplementedException();
    }

    @Override
    @PostMapping("/{recordId}/reject")
    @CheckAccess(
        resourceType = ResourceType.ATTENDANCE_RECORD,
        resourceId = "#recordId",
        permission = PermissionType.APPROVE
    )
    public void rejectAttendance(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long recordId
    ) {
        throw new NotImplementedException();
    }
}
