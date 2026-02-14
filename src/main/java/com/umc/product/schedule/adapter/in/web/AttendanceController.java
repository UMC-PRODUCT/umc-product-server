package com.umc.product.schedule.adapter.in.web;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.schedule.adapter.in.web.dto.request.CheckAttendanceRequest;
import com.umc.product.schedule.adapter.in.web.dto.request.SubmitReasonRequest;
import com.umc.product.schedule.adapter.in.web.dto.response.AttendanceRecordResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.AvailableAttendanceResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.MyAttendanceHistoryResponse;
import com.umc.product.schedule.adapter.in.web.dto.response.PendingAttendanceResponse;
import com.umc.product.schedule.adapter.in.web.mapper.AttendanceWebMapper;
import com.umc.product.schedule.adapter.in.web.swagger.AttendanceControllerApi;
import com.umc.product.schedule.application.port.in.command.ApproveAttendanceUseCase;
import com.umc.product.schedule.application.port.in.command.CheckAttendanceUseCase;
import com.umc.product.schedule.application.port.in.command.SubmitReasonUseCase;
import com.umc.product.schedule.application.port.in.query.GetAttendanceRecordUseCase;
import com.umc.product.schedule.application.port.in.query.GetAvailableAttendancesUseCase;
import com.umc.product.schedule.application.port.in.query.GetMyAttendanceHistoryUseCase;
import com.umc.product.schedule.application.port.in.query.GetPendingAttendancesUseCase;
import com.umc.product.schedule.domain.AttendanceRecord.AttendanceRecordId;
import jakarta.validation.Valid;
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

    private final CheckAttendanceUseCase checkAttendanceUseCase;
    private final SubmitReasonUseCase submitReasonUseCase;
    private final ApproveAttendanceUseCase approveAttendanceUseCase;
    private final GetAttendanceRecordUseCase getAttendanceRecordUseCase;
    private final GetAvailableAttendancesUseCase getAvailableAttendancesUseCase;
    private final GetMyAttendanceHistoryUseCase getMyAttendanceHistoryUseCase;
    private final GetPendingAttendancesUseCase getPendingAttendancesUseCase;
    private final GetChallengerUseCase getChallengerUseCase;

    private final AttendanceWebMapper mapper;

    @Override
    @PostMapping("/check")
    public Long checkAttendance(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestBody CheckAttendanceRequest request
    ) {
        return checkAttendanceUseCase.check(request.toCommand(memberPrincipal.getMemberId())).id();
    }

    @Override
    @PostMapping("/reason")
    public Long submitReasonAttendance(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Valid @RequestBody SubmitReasonRequest request
    ) {
        return submitReasonUseCase.submitReason(request.toCommand(memberPrincipal.getMemberId())).id();
    }

    @Override
    @GetMapping("/available")
    public List<AvailableAttendanceResponse> getAvailableAttendances(
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        Long memberId = memberPrincipal.getMemberId();
        Long gisuId = getChallengerUseCase.getLatestActiveChallengerByMemberId(memberId).gisuId();

        return mapper.toAvailableAttendanceResponses(
            getAvailableAttendancesUseCase.getAvailableList(memberId, gisuId)
        );
    }

    @Override
    @GetMapping("/history")
    public List<MyAttendanceHistoryResponse> getMyAttendanceHistory(
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        Long memberId = memberPrincipal.getMemberId();
        Long gisuId = getChallengerUseCase.getLatestActiveChallengerByMemberId(memberId).gisuId();

        return mapper.toMyAttendanceHistoryResponses(
            getMyAttendanceHistoryUseCase.getHistory(memberId, gisuId)
        );
    }

    @Override
    @GetMapping("/{recordId}")
    public AttendanceRecordResponse getAttendanceRecord(
        @PathVariable Long recordId
    ) {
        return mapper.toAttendanceRecordResponse(
            getAttendanceRecordUseCase.getById(new AttendanceRecordId(recordId))
        );
    }

    @Override
    @GetMapping("/pending/{scheduleId}")
    public List<PendingAttendanceResponse> getPendingAttendances(
        @PathVariable Long scheduleId
    ) {
        return mapper.toPendingAttendanceResponses(
            getPendingAttendancesUseCase.getPendingList(scheduleId)
        );
    }

    @Override
    @PostMapping("/{recordId}/approve")
    public void approveAttendance(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long recordId
    ) {
        approveAttendanceUseCase.approve(new AttendanceRecordId(recordId), memberPrincipal.getMemberId());
    }

    @Override
    @PostMapping("/{recordId}/reject")
    public void rejectAttendance(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long recordId
    ) {
        approveAttendanceUseCase.reject(new AttendanceRecordId(recordId), memberPrincipal.getMemberId());
    }
}
