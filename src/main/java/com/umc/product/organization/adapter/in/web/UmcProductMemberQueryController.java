package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.response.PageResponse;
import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.response.umcproduct.UmcProductMemberPageResponse;
import com.umc.product.organization.adapter.in.web.dto.response.umcproduct.UmcProductMemberResponse;
import com.umc.product.organization.application.port.in.query.GetUmcProductMemberUseCase;
import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductMemberSearchCondition;
import com.umc.product.organization.domain.enums.UmcProductFunctionalRole;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;
import com.umc.product.organization.domain.enums.UmcProductPosition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Public
@RestController
@RequestMapping("/api/v1/umc-product/members")
@RequiredArgsConstructor
public class UmcProductMemberQueryController {

    private final GetUmcProductMemberUseCase getUmcProductMemberUseCase;

    @GetMapping
    public UmcProductMemberPageResponse search(
        @RequestParam(required = false) Long umcProductGenerationId,
        @RequestParam(required = false) Long functionalUnitId,
        @RequestParam(required = false) UmcProductFunctionalUnitType functionalUnitType,
        @RequestParam(required = false) UmcProductFunctionalRole role,
        @RequestParam(required = false) UmcProductPosition position,
        @RequestParam(required = false) Long squadId,
        Pageable pageable
    ) {
        UmcProductMemberSearchCondition condition = UmcProductMemberSearchCondition.of(
            umcProductGenerationId,
            functionalUnitId,
            functionalUnitType,
            role,
            position,
            squadId
        );
        PageResponse<UmcProductMemberResponse> pageResponse = PageResponse.of(
            getUmcProductMemberUseCase.search(condition, pageable),
            UmcProductMemberResponse::from
        );
        return UmcProductMemberPageResponse.from(pageResponse);
    }

    @GetMapping("/{umcProductMemberId}")
    public UmcProductMemberResponse get(@PathVariable Long umcProductMemberId) {
        return UmcProductMemberResponse.from(getUmcProductMemberUseCase.getById(umcProductMemberId));
    }
}
