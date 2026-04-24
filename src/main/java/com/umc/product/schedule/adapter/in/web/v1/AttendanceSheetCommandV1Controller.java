package com.umc.product.schedule.adapter.in.web.v1;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.schedule.adapter.in.web.v1.dto.request.UpdateAttendanceSheetRequest;
import com.umc.product.schedule.adapter.in.web.v1.swagger.AttendanceSheetControllerV1Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Deprecated
public class AttendanceSheetCommandV1Controller implements AttendanceSheetControllerV1Api {

    @Override
    @PatchMapping("/attendance-sheets/{sheetId}")
    @CheckAccess(
        resourceType = ResourceType.ATTENDANCE_SHEET,
        resourceId = "#sheetId",
        permission = PermissionType.APPROVE
    )
    public void updateAttendanceSheet(
        @PathVariable Long sheetId,
        @RequestBody UpdateAttendanceSheetRequest request
    ) {
        throw new NotImplementedException();
    }

    @Override
    @DeleteMapping("/attendance-sheets/{sheetId}")
    @CheckAccess(
        resourceType = ResourceType.ATTENDANCE_SHEET,
        resourceId = "#sheetId",
        permission = PermissionType.APPROVE
    )
    public void deactivateAttendanceSheet(@PathVariable Long sheetId) {
        throw new NotImplementedException();
    }

    @Override
    @PostMapping("/attendance-sheets/{sheetId}/activate")
    @CheckAccess(
        resourceType = ResourceType.ATTENDANCE_SHEET,
        resourceId = "#sheetId",
        permission = PermissionType.APPROVE
    )
    public void activateAttendanceSheet(@PathVariable Long sheetId) {
        throw new NotImplementedException();
    }
}
