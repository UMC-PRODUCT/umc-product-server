package com.umc.product.authorization.adapter.in.web;

import com.umc.product.authorization.adapter.in.web.dto.response.ResourcePermissionResponse;
import com.umc.product.authorization.application.port.in.query.ResourcePermissionUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ResourcePermissionInfo;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "리소스 접근 권한 확인", description = "운영진 권한 관련 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/authorization/resource-permission")
public class ResourcePermissionController {

    private final ResourcePermissionUseCase resourcePermissionUseCase;

    @GetMapping
    @Operation(summary = "리소스 권한 조회", description = "특정 리소스에 대해 현재 사용자가 가진 권한 목록을 조회합니다.")
    ResourcePermissionResponse getResourcePermission(
        @RequestParam ResourceType resourceType,
        @RequestParam(required = false) Long resourceId,
        @CurrentMember MemberPrincipal principal
        ) {

        ResourcePermissionInfo permission = resourcePermissionUseCase.hasPermission(principal.getMemberId(), resourceType,
            resourceId);
        return ResourcePermissionResponse.from(permission);
    }
}
