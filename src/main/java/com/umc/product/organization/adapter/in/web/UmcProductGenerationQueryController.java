package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.response.umcproduct.UmcProductGenerationListResponse;
import com.umc.product.organization.adapter.in.web.dto.response.umcproduct.UmcProductGenerationResponse;
import com.umc.product.organization.application.port.in.query.GetUmcProductGenerationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Public
@RestController
@RequestMapping("/api/v1/umc-product/generations")
@RequiredArgsConstructor
public class UmcProductGenerationQueryController {

    private final GetUmcProductGenerationUseCase getUmcProductGenerationUseCase;

    @GetMapping
    public UmcProductGenerationListResponse list() {
        return UmcProductGenerationListResponse.from(getUmcProductGenerationUseCase.listAll());
    }

    @GetMapping("/{umcProductGenerationId}")
    public UmcProductGenerationResponse get(@PathVariable Long umcProductGenerationId) {
        return UmcProductGenerationResponse.from(getUmcProductGenerationUseCase.getById(umcProductGenerationId));
    }
}
