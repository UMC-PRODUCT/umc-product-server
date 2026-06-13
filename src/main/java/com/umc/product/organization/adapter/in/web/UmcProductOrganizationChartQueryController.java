package com.umc.product.organization.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.response.umcproduct.UmcProductOrganizationChartResponse;
import com.umc.product.organization.application.port.in.query.GetUmcProductOrganizationChartUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Public
@RestController
@RequestMapping("/api/v1/umc-product/organization-chart")
@RequiredArgsConstructor
@Tag(name = "Organization | UMC PRODUCT 조직도 Query", description = "UMC PRODUCT 기수별 조직도 조회")
public class UmcProductOrganizationChartQueryController {

    private final GetUmcProductOrganizationChartUseCase getUmcProductOrganizationChartUseCase;

    @GetMapping
    @Operation(
        operationId = "UMC-PRODUCT-ORGANIZATION-CHART-101",
        summary = "UMC PRODUCT 조직도 조회",
        description = "기수 ID 기준으로 UMC PRODUCT 조직도를 조회합니다. 기수 정보, 해당 기수의 기능 조직 목록, 운영 기간과 겹치는 스쿼드 목록을 함께 반환합니다."
    )
    public UmcProductOrganizationChartResponse get(@RequestParam Long umcProductGenerationId) {
        return UmcProductOrganizationChartResponse.from(
            getUmcProductOrganizationChartUseCase.getByGenerationId(umcProductGenerationId)
        );
    }
}
