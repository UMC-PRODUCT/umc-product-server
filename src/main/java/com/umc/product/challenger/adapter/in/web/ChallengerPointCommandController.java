package com.umc.product.challenger.adapter.in.web;

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

    @Operation(summary = "챌린저 상벌점 부여")
    @PostMapping("{challengerId}/points")
    ChallengerInfoResponse grantChallengerPoints(
        @PathVariable Long challengerId,
        @RequestBody GrantChallengerPointRequest request
    ) {
        manageChallengerUseCase.grantChallengerPoint(request.toCommand(challengerId));

        return assembler.fromChallengerId(challengerId);
    }

    @Operation(summary = "챌린저 상벌점 사유 수정")
    @PatchMapping("points/{challengerPointId}")
    void editChallengerPoints(
        @PathVariable Long challengerPointId,
        @RequestBody EditChallengerPointRequest request
    ) {
        manageChallengerUseCase.updateChallengerPoint(request.toCommand(challengerPointId));
    }

    @Operation(summary = "챌린저 상벌점 삭제")
    @DeleteMapping("points/{challengerPointId}")
    void deleteChallengerPoint(@PathVariable Long challengerPointId) {
        manageChallengerUseCase.deleteChallengerPoint(
            new DeleteChallengerPointRequest().toCommand(challengerPointId)
        );
    }
}
