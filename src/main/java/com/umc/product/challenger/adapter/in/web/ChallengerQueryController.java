package com.umc.product.challenger.adapter.in.web;

import com.umc.product.challenger.adapter.in.web.assembler.ChallengerResponseAssembler;
import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Tag(name = "Challenger | 챌린저 Query", description = "챌린저 기본 정보와 기록을 다룹니다.")
public class ChallengerQueryController {

    private final ChallengerResponseAssembler assembler;

//    @Operation(summary = "사용 중단 예정 특정 회원 챌린저 정보 조회", description = "회원 정보 조회에서 같은 기록을 제공합니다.")
//    @Deprecated(since = "v1.2.5", forRemoval = true)
//    @GetMapping("member/{memberId}")
//    List<ChallengerInfoResponse> getChallengerInfos(@PathVariable Long memberId) {
//        log.warn("Deprecated API 호출: /api/v1/challenger/member/{memberId} - 회원 ID: {}", memberId);
//
//        return assembler.fromMemberId(memberId);
//    }

//    @Operation(summary = "사용 중단 예정 내 챌린저 기록 조회", description = "회원 정보 조회에서 같은 기록을 제공합니다.")
//    @Deprecated(since = "v1.2.5", forRemoval = true)
//    @GetMapping("member/me")
//    List<ChallengerInfoResponse> getMyChallengerInfos(@CurrentMember MemberPrincipal memberPrincipal) {
//        log.warn("Deprecated API 호출: /api/v1/challenger/member/me - 회원 ID: {}", memberPrincipal.getMemberId());
//
//        return assembler.fromMemberId(memberPrincipal.getMemberId());
//    }

    @Operation(operationId = "CHALLENGER-101", summary = "챌린저 정보 조회")
    @GetMapping("{challengerId}")
    ChallengerInfoResponse getChallengerInfo(@PathVariable Long challengerId) {
        return assembler.fromChallengerId(challengerId);
    }
}
