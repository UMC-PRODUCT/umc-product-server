package com.umc.product.authorization.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.authorization.adapter.in.web.dto.request.BatchResourcePermissionRequest;
import com.umc.product.authorization.adapter.in.web.dto.response.BatchResourcePermissionResponse;
import com.umc.product.authorization.adapter.in.web.dto.response.ResourcePermissionResponse;
import com.umc.product.authorization.application.port.in.query.ResourcePermissionUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ResourcePermissionInfo;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Authorization | 접근 권한 확인", description = "현재 사용자의 리소스별 권한을 확인합니다.")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/authorization")
public class ResourcePermissionController {

    private final ResourcePermissionUseCase resourcePermissionUseCase;

    @GetMapping("/resource-permission")
    @Operation(
        operationId = "PERMISSION-001",
        summary = "리소스 권한 조회",
        description = "현재 사용자의 리소스 권한을 조회합니다. permissionType을 지정하면 해당 권한만 평가합니다."
    )
    ResourcePermissionResponse getResourcePermission(
        @RequestParam ResourceType resourceType,
        @RequestParam(required = false) Long resourceId,
        @RequestParam(required = false) PermissionType permissionType,
        @CurrentMember MemberPrincipal principal
    ) {

        ResourcePermissionInfo permission = resourcePermissionUseCase.hasPermission(
            principal.getMemberId(),
            resourceType,
            resourceId,
            permissionType
        );

        return ResourcePermissionResponse.from(permission);
    }

    @PostMapping("/resource-permissions/batch")
    @Operation(
        operationId = "PERMISSION-002",
        summary = "리소스 권한 배치 조회",
        description = "여러 리소스의 권한을 한 번에 조회합니다. 유효하지 않은 query가 있으면 요청 전체가 실패합니다."
    )
    BatchResourcePermissionResponse batchGetResourcePermission(
        @RequestBody @Valid BatchResourcePermissionRequest request,
        @CurrentMember MemberPrincipal principal
    ) {
        List<ResourcePermissionInfo> permissions = resourcePermissionUseCase.batchHasPermission(
            principal.getMemberId(),
            request.toQueries()
        );

        return BatchResourcePermissionResponse.from(permissions);
    }
}
