package com.umc.product.challenger.adapter.in.web;

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
import java.util.List;
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
@Tag(name = "Challenger | 챌린저 Command", description = "챌린저 정보를 조회하고, 기록 조회. 검색은 따로 구분되어 있습니다")
public class ChallengerCommandController {

    private final ManageChallengerUseCase manageChallengerUseCase;
    private final ChallengerResponseAssembler assembler;

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER,
        permission = PermissionType.WRITE
    )
    @Operation(summary = "[CHALLENGER-001] 챌린저 생성")
    @PostMapping
    ChallengerInfoResponse createChallenger(@RequestBody CreateChallengerInfoRequest request) {
        Long challengerId = manageChallengerUseCase.createChallenger(request.toCommand());

        return assembler.fromChallengerId(challengerId);
    }

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER,
        permission = PermissionType.WRITE
    )
    @Operation(summary = "[CHALLENGER-002] 챌린저 batch 생성", description = "한 번에 여러 건의 챌린저를 등록합니다. 기존에 `bulk`로 되어 있는 엔드포인트를 수정하였습니다.")
    @PostMapping("batch")
    List<ChallengerInfoResponse> bulkCreateChallenger(
        @RequestBody List<CreateChallengerInfoRequest> requests
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
        permission = PermissionType.DELETE
    )
    @Operation(summary = "[CHALLENGER-003] 챌린저 비활성화 (제명/탈부 처리)")
    @PostMapping("{challengerId}/deactivate")
    void deactivateChallenger(
        @PathVariable Long challengerId,
        @RequestBody DeactivateChallengerRequest request
    ) {
        manageChallengerUseCase.deactivateChallenger(request.toCommand(challengerId));
    }

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER,
        permission = PermissionType.EDIT
    )
    @Operation(summary = "[CHALLENGER-004] 챌린저 파트 변경")
    @PatchMapping("{challengerId}/part")
    ChallengerInfoResponse editChallengerInfo(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long challengerId,
        @RequestBody EditChallengerPartRequest request
    ) {
        manageChallengerUseCase.updateChallenger(request.toCommand(challengerId, memberPrincipal.getMemberId()));

        return assembler.fromChallengerId(challengerId);
    }

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER,
        permission = PermissionType.DELETE
    )
    @Operation(summary = "[CHALLENGER-005] [주의] 챌린저 삭제 (Hard Delete)")
    @DeleteMapping("{challengerId}")
    void deleteChallenger(@PathVariable Long challengerId) {
        DeleteChallengerCommand command = new DeleteChallengerCommand(challengerId, "관리자에 의한 삭제");
        manageChallengerUseCase.deleteChallenger(command);
    }
}
