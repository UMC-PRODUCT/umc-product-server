package com.umc.product.organization.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.response.umcproduct.UmcProductGenerationListResponse;
import com.umc.product.organization.adapter.in.web.dto.response.umcproduct.UmcProductGenerationResponse;
import com.umc.product.organization.application.port.in.query.GetUmcProductGenerationUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Public
@RestController
@RequestMapping("/api/v1/umc-product/generations")
@RequiredArgsConstructor
@Tag(name = "Organization | UMC PRODUCT 기수 Query", description = "UMC PRODUCT 기수 목록과 상세 정보를 조회합니다.")
public class UmcProductGenerationQueryController {

    private final GetUmcProductGenerationUseCase getUmcProductGenerationUseCase;

    @GetMapping
    @Operation(
        operationId = "UMC-PRODUCT-GENERATION-101",
        summary = "UMC PRODUCT 기수 목록 조회",
        description = "등록된 UMC PRODUCT 기수 목록을 조회합니다. 각 기수의 번호, 운영 기간, 활성 여부를 반환합니다."
    )
    public UmcProductGenerationListResponse list() {
        return UmcProductGenerationListResponse.from(getUmcProductGenerationUseCase.listAll());
    }

    @GetMapping("/{umcProductGenerationId}")
    @Operation(
        operationId = "UMC-PRODUCT-GENERATION-102",
        summary = "UMC PRODUCT 기수 상세 조회",
        description = "UMC PRODUCT 기수 ID로 단건 상세 정보를 조회합니다. 기수 번호, 운영 기간, 활성 여부를 반환합니다."
    )
    public UmcProductGenerationResponse get(@PathVariable Long umcProductGenerationId) {
        return UmcProductGenerationResponse.from(getUmcProductGenerationUseCase.getById(umcProductGenerationId));
    }
}
