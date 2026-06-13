package com.umc.product.organization.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.response.umcproduct.UmcProductSquadListResponse;
import com.umc.product.organization.application.port.in.query.GetUmcProductSquadUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Public
@RestController
@RequestMapping("/api/v1/umc-product/squads")
@RequiredArgsConstructor
@Tag(name = "Organization | UMC PRODUCT 스쿼드 Query", description = "UMC PRODUCT 스쿼드 목록 조회")
public class UmcProductSquadQueryController {

    private final GetUmcProductSquadUseCase getUmcProductSquadUseCase;

    @GetMapping
    @Operation(
        operationId = "UMC-PRODUCT-SQUAD-101",
        summary = "UMC PRODUCT 스쿼드 목록 조회",
        description = "UMC PRODUCT 스쿼드 목록을 조회합니다. 기수 ID를 전달하면 해당 기수 운영 기간과 겹치는 스쿼드를 조회하고, active 값으로 활성 여부를 필터링할 수 있습니다."
    )
    public UmcProductSquadListResponse list(
        @RequestParam(required = false) Long umcProductGenerationId,
        @RequestParam(required = false) Boolean active
    ) {
        return UmcProductSquadListResponse.from(getUmcProductSquadUseCase.list(umcProductGenerationId, active));
    }
}
