package com.umc.product.organization.adapter.in.web;

<<<<<<< HEAD
import com.umc.product.global.response.PageResponse;
import com.umc.product.organization.adapter.in.web.dto.response.GisuNameListResponse;
import com.umc.product.organization.adapter.in.web.dto.response.GisuPageResponse;
import com.umc.product.organization.adapter.in.web.dto.response.GisuResponse;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
=======
import com.umc.product.organization.adapter.in.web.dto.response.GisuListResponse;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import lombok.RequiredArgsConstructor;
>>>>>>> 5447cb8f1af6a362cee69dfbc502fd0ba238cd48
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
<<<<<<< HEAD
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
=======
    public GisuListResponse getGisuList() {
        return GisuListResponse.from(getGisuUseCase.getList());
>>>>>>> 5447cb8f1af6a362cee69dfbc502fd0ba238cd48
    }
}
