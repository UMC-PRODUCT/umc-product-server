package com.umc.product.community.adapter.in.web;

import com.umc.product.community.adapter.in.web.dto.response.TrophyResponse;
import com.umc.product.community.application.port.in.trophy.Query.GetTrophyListUseCase;
import com.umc.product.community.application.port.in.trophy.Query.TrophySearchQuery;
import com.umc.product.global.constant.SwaggerTag.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trophies")
@RequiredArgsConstructor
@Tag(name = Constants.COMMUNITY)
public class TrophyQueryController {

    private final GetTrophyListUseCase getTrophyListUseCase;

    @GetMapping
    @Operation(summary = "상장 목록 조회", description = "주차, 학교, 파트로 상장 목록을 조회합니다.")
    public List<TrophyResponse> getTrophies(
            @RequestParam(required = false) Integer week,
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String part
    ) {
        TrophySearchQuery query = new TrophySearchQuery(week, school, part);
        return getTrophyListUseCase.getTrophies(query)
                .stream()
                .map(TrophyResponse::from)
                .toList();
    }
}
