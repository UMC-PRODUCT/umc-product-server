package com.umc.product.challenger.adapter.in.web;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.challenger.adapter.in.web.assembler.ChallengerResponseAssembler;
import com.umc.product.challenger.adapter.in.web.dto.request.DeleteChallengerPointRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.EditChallengerPointRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.GrantChallengerPointRequest;
import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerInfoResponse;
import com.umc.product.challenger.application.port.in.command.ManageChallengerUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/challenger")
@RequiredArgsConstructor
@Tag(name = "Challenger | 챌린저 상벌점 Command", description = "챌린저에게 상점과 벌점을 부여합니다.")
public class ChallengerPointCommandController {

    private final ManageChallengerUseCase manageChallengerUseCase;
    private final ChallengerResponseAssembler assembler;

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER_POINT,
        resourceId = "#challengerId",
        permission = PermissionType.WRITE,
        message = "상벌점은 중앙운영사무국 또는 해당 학교 회장단만 부여할 수 있어요. 필요한 권한이 있다면 운영진에게 문의해주세요."
    )
    @Operation(operationId = "POINT-001", summary = "챌린저 상벌점 부여", description = "회장단 이상 권한이 필요합니다.")
    @PostMapping("{challengerId}/points")
    ChallengerInfoResponse grantChallengerPoints(
        @PathVariable Long challengerId,
        @Valid @RequestBody GrantChallengerPointRequest request
    ) {
        manageChallengerUseCase.grantChallengerPoint(request.toCommand(challengerId));

        return assembler.fromChallengerId(challengerId);
    }

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER_POINT,
        resourceId = "#challengerPointId",
        permission = PermissionType.EDIT,
        message = "상벌점 사유는 중앙운영사무국 또는 해당 학교 회장단만 수정할 수 있어요. 필요한 권한이 있다면 운영진에게 문의해주세요."
    )
    @Operation(operationId = "POINT-002", summary = "챌린저 상벌점 사유 수정", description = "회장단 이상 권한이 필요합니다.")
    @PatchMapping("points/{challengerPointId}")
    void editChallengerPoints(
        @PathVariable Long challengerPointId,
        @Valid @RequestBody EditChallengerPointRequest request
    ) {
        manageChallengerUseCase.updateChallengerPoint(request.toCommand(challengerPointId));
    }

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER_POINT,
        resourceId = "#challengerPointId",
        permission = PermissionType.DELETE,
        message = "상벌점 기록은 중앙운영사무국 총괄단만 삭제할 수 있어요. 필요한 권한이 있다면 운영진에게 문의해주세요."
    )
    @Operation(operationId = "POINT-003", summary = "챌린저 상벌점 삭제", description = "총괄단 권한이 필요합니다.")
    @DeleteMapping("points/{challengerPointId}")
    void deleteChallengerPoint(@PathVariable Long challengerPointId) {
        manageChallengerUseCase.deleteChallengerPoint(
            new DeleteChallengerPointRequest().toCommand(challengerPointId)
        );
    }
}
