package com.umc.product.authorization.adapter.in.web;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.adapter.in.web.dto.request.CreateChallengerRoleRequest;
import com.umc.product.authorization.adapter.in.web.dto.response.CreateChallengerRoleResponse;
import com.umc.product.authorization.application.port.in.command.ManageChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.command.dto.CreateChallengerRoleCommand;
import com.umc.product.authorization.application.port.in.command.dto.DeleteChallengerRoleCommand;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Authorization | 운영진 권한 관리", description = "운영진 역할 기록을 만들고 삭제합니다.")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/authorization/challenger-role")
public class ChallengerRoleController {

    private final ManageChallengerRoleUseCase manageChallengerRoleUseCase;

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER_ROLE,
        permission = PermissionType.WRITE
    )
    @PostMapping
    @Operation(operationId = "STAFF-001", summary = "운영진 기록 생성", description = "운영진 기록을 생성합니다. 총괄단 권한이 필요합니다.")
    public CreateChallengerRoleResponse createChallengerRole(
        @RequestBody CreateChallengerRoleRequest request) {
        Long createdId =
            manageChallengerRoleUseCase.createChallengerRole(CreateChallengerRoleCommand.from(request));

        return CreateChallengerRoleResponse.builder()
            .challengerRoleId(createdId)
            .build();
    }

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER_ROLE,
        permission = PermissionType.DELETE
    )
    @Operation(operationId = "STAFF-002", summary = "운영진 기록 삭제", description = "부여된 운영진 기록을 삭제합니다.")
    @DeleteMapping("{challengerRoleId}")
    public void deleteChallengerRole(
        @PathVariable Long challengerRoleId
    ) {
        manageChallengerRoleUseCase.deleteChallengerRole(
            DeleteChallengerRoleCommand.builder()
                .challengerRoleId(challengerRoleId)
                .build()
        );
    }

}
