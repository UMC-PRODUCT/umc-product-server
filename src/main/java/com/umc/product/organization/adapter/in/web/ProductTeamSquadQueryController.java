package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.response.productteam.ProductTeamSquadListResponse;
import com.umc.product.organization.application.port.in.query.GetProductTeamSquadUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Public
@RestController
@RequestMapping("/api/v1/product-team/squads")
@RequiredArgsConstructor
public class ProductTeamSquadQueryController {

    private final GetProductTeamSquadUseCase getProductTeamSquadUseCase;

    @GetMapping
    public ProductTeamSquadListResponse list(
        @RequestParam(required = false) Long productTeamGenerationId,
        @RequestParam(required = false) Boolean active
    ) {
        return ProductTeamSquadListResponse.from(getProductTeamSquadUseCase.list(productTeamGenerationId, active));
    }
}
