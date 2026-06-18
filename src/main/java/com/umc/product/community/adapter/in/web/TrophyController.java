package com.umc.product.community.adapter.in.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import com.umc.product.community.adapter.in.web.dto.request.CreateTrophyRequest;
import com.umc.product.community.adapter.in.web.dto.response.TrophyResponse;
import com.umc.product.community.application.port.in.command.trophy.CreateTrophyUseCase;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/trophies")
@RequiredArgsConstructor
@Tag(name = "Community | 명예의 전당 Command", description = "명예의 전당 항목을 만듭니다.")
public class TrophyController {

    private final CreateTrophyUseCase createTrophyUseCase;
    private final GetChallengerUseCase getChallengerUseCase;

    @PostMapping
    @Operation(operationId = "TROPHY-001", summary = "베스트 워크북 생성", description = "주차별 베스트 워크북을 생성합니다.")
    public TrophyResponse createTrophy(
        @RequestBody CreateTrophyRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        Long memberId = memberPrincipal.getMemberId();
        ChallengerInfoWithStatus challenger = getChallengerUseCase.getLatestActiveChallengerByMemberId(memberId);
        return TrophyResponse.from(createTrophyUseCase.createTrophy(request.toCommand(challenger.challengerId())));
    }
}
