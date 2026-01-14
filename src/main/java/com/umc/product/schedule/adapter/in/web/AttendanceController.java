//package com.umc.product.schedule.adapter.in.web;
//
//
//@RestController
//@RequestMapping("/api/v1/attendances")
//@RequiredArgsConstructor
//public class AttendanceController implements AttendanceControllerApi {
//
//    private final CheckAttendanceUseCase checkAttendanceUseCase;
//    private final ApproveAttendanceUseCase approveAttendanceUseCase;
//    private final GetAttendanceRecordUseCase getAttendanceRecordUseCase;
//    private final GetAvailableAttendancesUseCase getAvailableAttendancesUseCase;
//    private final GetMyAttendanceHistoryUseCase getMyAttendanceHistoryUseCase;
//
//    @Override
//    @PostMapping("/check")
//    public ApiResponse<Long> checkAttendance(
//            @CurrentMember Long challengerId,
//            @Valid @RequestBody CheckAttendanceRequest request
//    ) {
//        AttendanceRecordId recordId = checkAttendanceUseCase.check(request.toCommand(challengerId));
//        return ApiResponse.onSuccess(recordId.id());
//    }
//
//    @Override
//    @GetMapping("/available")
//    public ApiResponse<List<AvailableAttendanceResponse>> getAvailableAttendances(
//            @CurrentMember Long challengerId
//    ) {
//        List<AvailableAttendanceResponse> response = getAvailableAttendancesUseCase.getAvailableList(challengerId)
//                .stream()
//                .map(AvailableAttendanceResponse::from)
//                .toList();
//        return ApiResponse.onSuccess(response);
//    }
//
//    @Override
//    @GetMapping("/history")
//    public ApiResponse<List<MyAttendanceHistoryResponse>> getMyAttendanceHistory(
//            @CurrentMember Long challengerId
//    ) {
//        List<MyAttendanceHistoryResponse> response = getMyAttendanceHistoryUseCase.getHistory(challengerId)
//                .stream()
//                .map(MyAttendanceHistoryResponse::from)
//                .toList();
//        return ApiResponse.onSuccess(response);
//    }
//
//    @Override
//    @GetMapping("/{recordId}")
//    public ApiResponse<AttendanceRecordResponse> getAttendanceRecord(
//            @PathVariable Long recordId
//    ) {
//        AttendanceRecordResponse response = AttendanceRecordResponse.from(
//                getAttendanceRecordUseCase.getById(new AttendanceRecordId(recordId))
//        );
//        return ApiResponse.onSuccess(response);
//    }
//
//    @Override
//    @PostMapping("/{recordId}/approve")
//    public ApiResponse<Void> approveAttendance(
//            @CurrentMember Long confirmerId,
//            @PathVariable Long recordId
//    ) {
//        approveAttendanceUseCase.approve(new AttendanceRecordId(recordId), confirmerId);
//        return ApiResponse.onSuccess(null);
//    }
//
//    @Override
//    @PostMapping("/{recordId}/reject")
//    public ApiResponse<Void> rejectAttendance(
//            @CurrentMember Long confirmerId,
//            @PathVariable Long recordId
//    ) {
//        approveAttendanceUseCase.reject(new AttendanceRecordId(recordId), confirmerId);
//        return ApiResponse.onSuccess(null);
//    }
//}
