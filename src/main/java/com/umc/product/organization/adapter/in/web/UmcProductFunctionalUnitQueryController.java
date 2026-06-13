package com.umc.product.organization.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.response.umcproduct.UmcProductFunctionalUnitListResponse;
import com.umc.product.organization.application.port.in.query.GetUmcProductFunctionalUnitUseCase;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Public
@RestController
@RequestMapping("/api/v1/umc-product/functional-units")
@RequiredArgsConstructor
@Tag(name = "Organization | UMC Product 기능 조직 Query", description = "UMC Product 기능 조직 목록 조회")
public class UmcProductFunctionalUnitQueryController {

    private final GetUmcProductFunctionalUnitUseCase getUmcProductFunctionalUnitUseCase;

    @GetMapping
    @Operation(
        operationId = "UMC-PRODUCT-FUNCTIONAL-UNIT-101",
        summary = "[UMC-PRODUCT-FUNCTIONAL-UNIT-101] UMC Product 기능 조직 목록 조회",
        description = "기수별 UMC Product 기능 조직 목록을 조회합니다. type 값을 전달하면 기능 조직 유형별로 필터링할 수 있습니다."
    )
    public UmcProductFunctionalUnitListResponse list(
        @RequestParam Long umcProductGenerationId,
        @RequestParam(required = false) UmcProductFunctionalUnitType type
    ) {
        return UmcProductFunctionalUnitListResponse.from(
            getUmcProductFunctionalUnitUseCase.listByGeneration(umcProductGenerationId, type)
        );
    }
}
