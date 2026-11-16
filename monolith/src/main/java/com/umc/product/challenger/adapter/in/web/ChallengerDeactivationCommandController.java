package com.umc.product.challenger.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.challenger.adapter.in.web.dto.request.RegisterChallengerDeactivationRequest;
import com.umc.product.challenger.adapter.in.web.dto.response.RegisterChallengerDeactivationResponse;
import com.umc.product.challenger.application.port.in.command.RegisterChallengerDeactivationUseCase;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/challenger/admin")
@RequiredArgsConstructor
@Validated
@Tag(name = "Challenger | 탈부 관리 Command", description = "관리자용 챌린저 탈부 등록 기능을 제공합니다.")
public class ChallengerDeactivationCommandController {

    private final RegisterChallengerDeactivationUseCase registerChallengerDeactivationUseCase;

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER,
        resourceId = "#challengerId",
        permission = PermissionType.MANAGE,
        message = "해당 챌린저의 탈부를 등록할 권한이 없어요. 대상 기수와 소속 범위를 확인해주세요."
    )
    @PostMapping("/{challengerId}/deactivations")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        operationId = "CHALLENGER-006",
        summary = "챌린저 탈부 등록",
        description = "탈부 정보와 선택한 근거 자료를 저장하고 챌린저 상태를 변경합니다."
    )
    public RegisterChallengerDeactivationResponse register(
        @PathVariable Long challengerId,
        @Valid @RequestBody RegisterChallengerDeactivationRequest request,
        @CurrentMember MemberPrincipal principal
    ) {
        Long deactivationId = registerChallengerDeactivationUseCase.register(
            request.toCommand(challengerId, principal.getMemberId())
        );
        return RegisterChallengerDeactivationResponse.from(deactivationId);
    }
}
