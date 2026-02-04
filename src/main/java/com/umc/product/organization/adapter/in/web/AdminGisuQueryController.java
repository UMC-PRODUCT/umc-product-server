package com.umc.product.organization.adapter.in.web;

import com.umc.product.organization.adapter.in.web.dto.response.GisuListResponse;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gisu")
@RequiredArgsConstructor
public class AdminGisuQueryController implements AdminGisuQueryControllerApi {

    private final GetGisuUseCase getGisuUseCase;

    @Override
    @GetMapping
    public GisuListResponse getGisuList() {
        return GisuListResponse.from(getGisuUseCase.getList());
    }
}
