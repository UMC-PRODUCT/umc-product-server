package com.umc.product.schedule.adapter.in.web.swagger;

import com.umc.product.schedule.adapter.in.web.dto.request.UpdateAttendanceSheetRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Schedule | 출석부", description = "")
public interface AttendanceSheetControllerApi {

    @Operation(summary = "출석부 수정", description = "출석부 설정을 수정합니다")
    void updateAttendanceSheet(
        @Parameter(description = "출석부 ID") Long sheetId,
        UpdateAttendanceSheetRequest request
    );

    @Operation(summary = "출석부 비활성화", description = "출석부를 비활성화합니다")
    void deactivateAttendanceSheet(
        @Parameter(description = "출석부 ID") Long sheetId
    );

    @Operation(summary = "출석부 활성화", description = "비활성화된 출석부를 다시 활성화합니다")
    void activateAttendanceSheet(
        @Parameter(description = "출석부 ID") Long sheetId
    );
}
