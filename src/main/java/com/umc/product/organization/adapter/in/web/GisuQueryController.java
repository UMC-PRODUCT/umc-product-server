package com.umc.product.organization.adapter.in.web;

import com.umc.product.organization.adapter.in.web.dto.response.GisuListResponse;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/gisu")
@RequiredArgsConstructor
public class GisuQueryController {

    private final GetGisuUseCase getGisuUseCase;

    @GetMapping
    public GisuListResponse getGisuList() {
        return GisuListResponse.from(getGisuUseCase.getList());
    }
}
