package com.umc.product.challenger.adapter.in.web;

import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerInfoResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Challenger 조회 관련 기능을 담당하는 컨트롤러 입니다.
 */
@RestController
@RequestMapping("/api/v1/challenger")
@RequiredArgsConstructor
@Tag(name = "Challenger | 챌린저 Query", description = "챌린저 정보를 조회하고, 기록 조회. 검색은 따로 구분되어 있습니다")
public class ChallengerQueryController {

    private final ChallengerResponseAssembler assembler;

    @Operation(summary = "특정 회원의 모든 챌린저 정보 조회")
    @GetMapping("member/{memberId}")
    List<ChallengerInfoResponse> getChallengerInfos(@PathVariable Long memberId) {
        return assembler.fromMemberId(memberId);
    }

    @Operation(summary = "내 챌린저 기록 조회")
    @GetMapping("member/me")
    List<ChallengerInfoResponse> getMyChallengerInfos(@CurrentMember MemberPrincipal memberPrincipal) {
        return assembler.fromMemberId(memberPrincipal.getMemberId());
    }

    @Operation(summary = "챌린저 정보 조회")
    @GetMapping("{challengerId}")
    ChallengerInfoResponse getChallengerInfo(@PathVariable Long challengerId) {
        return assembler.fromChallengerId(challengerId);
    }
}
