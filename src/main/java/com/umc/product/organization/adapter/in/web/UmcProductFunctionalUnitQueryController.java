package com.umc.product.organization.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.response.umcproduct.UmcProductFunctionalUnitListResponse;
import com.umc.product.organization.application.port.in.query.GetUmcProductFunctionalUnitUseCase;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;

import lombok.RequiredArgsConstructor;

@Public
@RestController
@RequestMapping("/api/v1/umc-product/functional-units")
@RequiredArgsConstructor
public class UmcProductFunctionalUnitQueryController {

    private final GetUmcProductFunctionalUnitUseCase getUmcProductFunctionalUnitUseCase;

    @GetMapping
    public UmcProductFunctionalUnitListResponse list(
        @RequestParam Long umcProductGenerationId,
        @RequestParam(required = false) UmcProductFunctionalUnitType type
    ) {
        return UmcProductFunctionalUnitListResponse.from(
            getUmcProductFunctionalUnitUseCase.listByGeneration(umcProductGenerationId, type)
        );
    }
}
