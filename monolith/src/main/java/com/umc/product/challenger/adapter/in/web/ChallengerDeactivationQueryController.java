package com.umc.product.challenger.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerDeactivationReasonListResponse;
import com.umc.product.challenger.application.port.in.query.ListChallengerDeactivationReasonUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/challenger/admin/deactivations")
@RequiredArgsConstructor
@Tag(name = "Challenger | 탈부 관리 Query", description = "관리자용 챌린저 탈부 조회 기능을 제공합니다.")
public class ChallengerDeactivationQueryController {

    private final ListChallengerDeactivationReasonUseCase listChallengerDeactivationReasonUseCase;

    @GetMapping("/reasons")
    @Operation(
        operationId = "CHALLENGER-104",
        summary = "탈부 사유 목록 조회",
        description = "자진 탈부와 타의 탈부에 사용할 수 있는 사유를 화면 표시 순서대로 반환합니다."
    )
    public ChallengerDeactivationReasonListResponse listReasons() {
        return ChallengerDeactivationReasonListResponse.from(
            listChallengerDeactivationReasonUseCase.listReasons()
        );
    }
}
