package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.response.productteam.ProductTeamOrganizationChartResponse;
import com.umc.product.organization.application.port.in.query.GetProductTeamOrganizationChartUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Public
@RestController
@RequestMapping("/api/v1/product-team/organization-chart")
@RequiredArgsConstructor
public class ProductTeamOrganizationChartQueryController {

    private final GetProductTeamOrganizationChartUseCase getProductTeamOrganizationChartUseCase;

    @GetMapping
    public ProductTeamOrganizationChartResponse get(@RequestParam Long productTeamGenerationId) {
        return ProductTeamOrganizationChartResponse.from(
            getProductTeamOrganizationChartUseCase.getByGenerationId(productTeamGenerationId)
        );
    }
}
