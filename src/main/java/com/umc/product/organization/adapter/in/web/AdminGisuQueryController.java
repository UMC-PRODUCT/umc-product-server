package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.response.PageResponse;
import com.umc.product.organization.adapter.in.web.dto.response.GisuNameListResponse;
import com.umc.product.organization.adapter.in.web.dto.response.GisuPageResponse;
import com.umc.product.organization.adapter.in.web.dto.response.GisuResponse;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
    public GisuPageResponse getGisuList(Pageable pageable) {
        PageResponse<GisuResponse> pageResponse = PageResponse.of(
                getGisuUseCase.getList(pageable),
                GisuResponse::from
        );
        return GisuPageResponse.from(pageResponse);
    }

    @Override
    @GetMapping("/all")
    public GisuNameListResponse getAllGisu() {
        return GisuNameListResponse.from(getGisuUseCase.getAllGisuNames());
    }
}
