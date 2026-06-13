package com.umc.product.organization.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.response.umcproduct.UmcProductSquadListResponse;
import com.umc.product.organization.application.port.in.query.GetUmcProductSquadUseCase;

import lombok.RequiredArgsConstructor;

@Public
@RestController
@RequestMapping("/api/v1/umc-product/squads")
@RequiredArgsConstructor
public class UmcProductSquadQueryController {

    private final GetUmcProductSquadUseCase getUmcProductSquadUseCase;

    @GetMapping
    public UmcProductSquadListResponse list(
        @RequestParam(required = false) Long umcProductGenerationId,
        @RequestParam(required = false) Boolean active
    ) {
        return UmcProductSquadListResponse.from(getUmcProductSquadUseCase.list(umcProductGenerationId, active));
    }
}
