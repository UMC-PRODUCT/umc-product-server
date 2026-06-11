package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.response.productteam.ProductTeamFunctionalUnitListResponse;
import com.umc.product.organization.application.port.in.query.GetProductTeamFunctionalUnitUseCase;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalUnitType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Public
@RestController
@RequestMapping("/api/v1/product-team/functional-units")
@RequiredArgsConstructor
public class ProductTeamFunctionalUnitQueryController {

    private final GetProductTeamFunctionalUnitUseCase getProductTeamFunctionalUnitUseCase;

    @GetMapping
    public ProductTeamFunctionalUnitListResponse list(
        @RequestParam Long productTeamGenerationId,
        @RequestParam(required = false) ProductTeamFunctionalUnitType type
    ) {
        return ProductTeamFunctionalUnitListResponse.from(
            getProductTeamFunctionalUnitUseCase.listByGeneration(productTeamGenerationId, type)
        );
    }
}
