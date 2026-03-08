package com.umc.product.challenger.adapter.in.web;

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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/challenger")
@RequiredArgsConstructor
@Tag(name = "Challenger | 챌린저 상벌점 Command", description = "베스트 워크북, 아웃, 경고 등")
public class ChallengerPointCommandController {

    private final ManageChallengerUseCase manageChallengerUseCase;
    private final ChallengerResponseAssembler assembler;

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER_POINT,
        permission = PermissionType.WRITE
    )
    @Operation(summary = "챌린저 상벌점 부여", description = "회장단 이상 가능합니다.")
    @PostMapping("{challengerId}/points")
    ChallengerInfoResponse grantChallengerPoints(
        @PathVariable Long challengerId,
        @RequestBody GrantChallengerPointRequest request
    ) {
        manageChallengerUseCase.grantChallengerPoint(request.toCommand(challengerId));

        return assembler.fromChallengerId(challengerId);
    }

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER_POINT,
        permission = PermissionType.EDIT
    )
    @Operation(summary = "챌린저 상벌점 사유 수정", description = "회장단 이상 가능합니다.")
    @PatchMapping("points/{challengerPointId}")
    void editChallengerPoints(
        @PathVariable Long challengerPointId,
        @RequestBody EditChallengerPointRequest request
    ) {
        manageChallengerUseCase.updateChallengerPoint(request.toCommand(challengerPointId));
    }

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER_POINT,
        permission = PermissionType.DELETE
    )
    @Operation(summary = "챌린저 상벌점 삭제", description = "총괄단만 가능합니다.")
    @DeleteMapping("points/{challengerPointId}")
    void deleteChallengerPoint(@PathVariable Long challengerPointId) {
        manageChallengerUseCase.deleteChallengerPoint(
            new DeleteChallengerPointRequest().toCommand(challengerPointId)
        );
    }
}
