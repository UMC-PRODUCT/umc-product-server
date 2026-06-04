package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.response.productteam.ProductTeamGenerationListResponse;
import com.umc.product.organization.adapter.in.web.dto.response.productteam.ProductTeamGenerationResponse;
import com.umc.product.organization.application.port.in.query.GetProductTeamGenerationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Public
@RestController
@RequestMapping("/api/v1/product-team/generations")
@RequiredArgsConstructor
public class ProductTeamGenerationQueryController {

    private final GetProductTeamGenerationUseCase getProductTeamGenerationUseCase;

    @GetMapping
    public ProductTeamGenerationListResponse list() {
        return ProductTeamGenerationListResponse.from(getProductTeamGenerationUseCase.listAll());
    }

    @GetMapping("/{productTeamGenerationId}")
    public ProductTeamGenerationResponse get(@PathVariable Long productTeamGenerationId) {
        return ProductTeamGenerationResponse.from(getProductTeamGenerationUseCase.getById(productTeamGenerationId));
    }
}
