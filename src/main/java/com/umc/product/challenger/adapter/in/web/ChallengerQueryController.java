package com.umc.product.challenger.adapter.in.web;

import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerInfoResponse;
import com.umc.product.challenger.application.port.in.command.ManageChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Challenger 관련 기능을 담당하는 컨트롤러 입니다.
 */
@RestController
@RequestMapping("/api/v1/challenger")
@RequiredArgsConstructor
@Tag(name = "Challenger | 챌린저 Query", description = "챌린저 정보를 조회하고, 기록 조회. 검색은 따로 구분되어 있습니다")
public class ChallengerQueryController {

    private final GetChallengerUseCase getChallengerUseCase;
    private final ManageChallengerUseCase manageChallengerUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final GetGisuUseCase getGisuUseCase;

    @Operation(summary = "특정 회원의 모든 챌린저 정보 조회")
    @GetMapping("member/{memberId}")
    List<ChallengerInfoResponse> getChallengerInfos(@PathVariable Long memberId) {
        return getChallengerInfoResponsesForMember(memberId);
    }

    @Operation(summary = "내 챌린저 기록 조회")
    @GetMapping("member/me")
    List<ChallengerInfoResponse> getMyChallengerInfos(@CurrentMember MemberPrincipal memberPrincipal) {
        return getChallengerInfoResponsesForMember(memberPrincipal.getMemberId());
    }

    @Operation(summary = "챌린저 정보 조회")
    @GetMapping("{challengerId}")
    ChallengerInfoResponse getChallengerInfo(
        @PathVariable Long challengerId
    ) {
        return getChallengerInfoResponse(challengerId);
    }

    // Private Methods

    private List<ChallengerInfoResponse> getChallengerInfoResponsesForMember(Long memberId) {
        List<ChallengerInfo> challengerInfos = getChallengerUseCase.getMemberChallengerList(memberId);
        MemberInfo memberInfo = getMemberUseCase.getById(memberId);

        return challengerInfos.stream()
            .map(challengerInfo -> {
                GisuInfo gisuInfo = getGisuUseCase.getById(challengerInfo.gisuId());
                return ChallengerInfoResponse.from(challengerInfo, memberInfo, gisuInfo);
            })
            .toList();
    }

    /**
     * 챌린저 ID를 기반으로 회원 및 기수 정보를 포함하여 응답 객체를 생성해주는 헬퍼 메소드
     */
    private ChallengerInfoResponse getChallengerInfoResponse(Long challengerId) {
        ChallengerInfo challengerInfo = getChallengerUseCase.getChallengerPublicInfo(challengerId);
        MemberInfo memberInfo = getMemberUseCase.getById(challengerInfo.memberId());
        GisuInfo gisuInfo = getGisuUseCase.getById(challengerInfo.gisuId());

        return ChallengerInfoResponse.from(challengerInfo, memberInfo, gisuInfo);
    }
}
