package com.umc.product.community.adapter.in.web;

import com.umc.product.community.adapter.in.web.dto.request.CreateTrophyRequest;
import com.umc.product.community.adapter.in.web.dto.response.TrophyResponse;
import com.umc.product.community.application.port.in.trophy.CreateTrophyUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trophies")
@RequiredArgsConstructor
@Tag(name = "Community | 명예의 전당 Command", description = "")
public class TrophyController {

    private final CreateTrophyUseCase createTrophyUseCase;

    @PostMapping
    @Operation(summary = "상장 생성", description = "주차별 상장을 생성합니다.")
    public TrophyResponse createTrophy(@RequestBody CreateTrophyRequest request) {
        return TrophyResponse.from(createTrophyUseCase.createTrophy(request.toCommand()));
    }
}
