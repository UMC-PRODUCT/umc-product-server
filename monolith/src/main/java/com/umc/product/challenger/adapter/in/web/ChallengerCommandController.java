package com.umc.product.challenger.adapter.in.web;

import java.util.List;

import org.springframework.validation.annotation.Validated;
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
import com.umc.product.challenger.adapter.in.web.dto.request.CreateChallengerInfoRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.DeactivateChallengerRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.EditChallengerPartRequest;
import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerInfoResponse;
import com.umc.product.challenger.application.port.in.command.ManageChallengerUseCase;
import com.umc.product.challenger.application.port.in.command.dto.DeleteChallengerCommand;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/challenger")
@RequiredArgsConstructor
@Validated
@Tag(name = "Challenger | 챌린저 Command", description = "챌린저 기본 정보와 기록을 다룹니다.")
public class ChallengerCommandController {

    private final ManageChallengerUseCase manageChallengerUseCase;
    private final ChallengerResponseAssembler assembler;

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER,
        permission = PermissionType.WRITE
    )
    @Operation(operationId = "CHALLENGER-001", summary = "챌린저 생성")
    @PostMapping
    ChallengerInfoResponse createChallenger(@Valid @RequestBody CreateChallengerInfoRequest request) {
        Long challengerId = manageChallengerUseCase.createChallenger(request.toCommand());

        return assembler.fromChallengerId(challengerId);
    }

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER,
        permission = PermissionType.WRITE
    )
    @Operation(operationId = "CHALLENGER-002", summary = "챌린저 일괄 생성", description = "여러 챌린저를 한 번에 등록합니다.")
    @PostMapping("batch")
    List<ChallengerInfoResponse> bulkCreateChallenger(
        @Valid @RequestBody List<@Valid CreateChallengerInfoRequest> requests
    ) {
        return requests.stream()
            .map(request ->
                assembler.fromChallengerId(
                    manageChallengerUseCase.createChallenger(request.toCommand())
                )
            )
            .toList();
    }

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER,
        resourceId = "#challengerId",
        permission = PermissionType.MANAGE
    )
    @Operation(
        operationId = "CHALLENGER-003", summary = "챌린저 비활성화",
        description = """
            탈부(WITHDRAW)와 제명(EXPEL)에 동일한 권한을 적용합니다.
            SUPER_ADMIN과 중앙운영사무국 총괄·부총괄은 기수와 학교에 관계없이 처리할 수 있습니다.
            중앙운영사무국 운영국원·교육국원은 대상 챌린저와 같은 기수의 모든 학교를 처리할 수 있습니다.
            교내 회장·부회장은 대상 챌린저와 같은 기수이면서 본인 역할의 소속 학교인 경우에만 처리할 수 있습니다.
            """
    )
    @PostMapping("{challengerId}/deactivate")
    void deactivateChallenger(
        @PathVariable Long challengerId,
        @Valid @RequestBody DeactivateChallengerRequest request
    ) {
        manageChallengerUseCase.deactivateChallenger(request.toCommand(challengerId));
    }

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER,
        permission = PermissionType.EDIT
    )
    @Operation(operationId = "CHALLENGER-004", summary = "챌린저 파트 변경")
    @PatchMapping("{challengerId}/part")
    ChallengerInfoResponse editChallengerInfo(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long challengerId,
        @Valid @RequestBody EditChallengerPartRequest request
    ) {
        manageChallengerUseCase.updateChallenger(request.toCommand(challengerId, memberPrincipal.getMemberId()));

        return assembler.fromChallengerId(challengerId);
    }

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER,
        permission = PermissionType.DELETE
    )
    @Operation(operationId = "CHALLENGER-005", summary = "챌린저 물리 삭제")
    @DeleteMapping("{challengerId}")
    void deleteChallenger(@PathVariable Long challengerId) {
        DeleteChallengerCommand command = new DeleteChallengerCommand(challengerId, "관리자에 의한 삭제");
        manageChallengerUseCase.deleteChallenger(command);
    }
}
