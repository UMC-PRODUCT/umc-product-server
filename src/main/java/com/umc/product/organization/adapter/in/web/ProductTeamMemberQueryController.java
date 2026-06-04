package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.response.PageResponse;
import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.response.productteam.ProductTeamMemberPageResponse;
import com.umc.product.organization.adapter.in.web.dto.response.productteam.ProductTeamMemberResponse;
import com.umc.product.organization.application.port.in.query.GetProductTeamMemberUseCase;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamMemberSearchCondition;
import com.umc.product.organization.domain.enums.ProductTeamPart;
import com.umc.product.organization.domain.enums.ProductTeamPosition;
import com.umc.product.organization.domain.enums.ProductTeamRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Public
@RestController
@RequestMapping("/api/v1/product-team/members")
@RequiredArgsConstructor
public class ProductTeamMemberQueryController {

    private final GetProductTeamMemberUseCase getProductTeamMemberUseCase;

    @GetMapping
    public ProductTeamMemberPageResponse search(
        @RequestParam(required = false) Long productTeamGenerationId,
        @RequestParam(required = false) ProductTeamPart part,
        @RequestParam(required = false) ProductTeamRole role,
        @RequestParam(required = false) ProductTeamPosition position,
        Pageable pageable
    ) {
        ProductTeamMemberSearchCondition condition = ProductTeamMemberSearchCondition.of(
            productTeamGenerationId,
            part,
            role,
            position
        );
        PageResponse<ProductTeamMemberResponse> pageResponse = PageResponse.of(
            getProductTeamMemberUseCase.search(condition, pageable),
            ProductTeamMemberResponse::from
        );
        return ProductTeamMemberPageResponse.from(pageResponse);
    }

    @GetMapping("/{productTeamMemberId}")
    public ProductTeamMemberResponse get(@PathVariable Long productTeamMemberId) {
        return ProductTeamMemberResponse.from(getProductTeamMemberUseCase.getById(productTeamMemberId));
    }
}
