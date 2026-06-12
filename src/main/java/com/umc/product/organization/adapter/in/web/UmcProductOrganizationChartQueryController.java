package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.response.umcproduct.UmcProductOrganizationChartResponse;
import com.umc.product.organization.application.port.in.query.GetUmcProductOrganizationChartUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Public
@RestController
@RequestMapping("/api/v1/umc-product/organization-chart")
@RequiredArgsConstructor
public class UmcProductOrganizationChartQueryController {

    private final GetUmcProductOrganizationChartUseCase getUmcProductOrganizationChartUseCase;

    @GetMapping
    public UmcProductOrganizationChartResponse get(@RequestParam Long umcProductGenerationId) {
        return UmcProductOrganizationChartResponse.from(
            getUmcProductOrganizationChartUseCase.getByGenerationId(umcProductGenerationId)
        );
    }
}
