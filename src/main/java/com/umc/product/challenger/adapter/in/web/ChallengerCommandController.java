package com.umc.product.challenger.adapter.in.web;

import com.umc.product.challenger.adapter.in.web.dto.request.CreateChallengerInfoRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.DeactivateChallengerRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.EditChallengerPartRequest;
import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerInfoResponse;
import com.umc.product.challenger.application.port.in.command.ManageChallengerUseCase;
import com.umc.product.challenger.application.port.in.command.dto.DeleteChallengerCommand;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/challenger")
@RequiredArgsConstructor
@Tag(name = "Challenger | 챌린저 Command", description = "챌린저 정보를 조회하고, 기록 조회. 검색은 따로 구분되어 있습니다")
public class ChallengerCommandController {

    private final GetChallengerUseCase getChallengerUseCase;
    private final ManageChallengerUseCase manageChallengerUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final GetGisuUseCase getGisuUseCase;

    @Operation(summary = "챌린저 생성")
    @PostMapping
    ChallengerInfoResponse createChallenger(@RequestBody CreateChallengerInfoRequest request) {
        Long challengerId = manageChallengerUseCase.createChallenger(request.toCommand());

        return getChallengerInfoResponse(challengerId);
    }

    @Operation(summary = "챌린저 Bulk 생성")
    @PostMapping("bulk")
    List<ChallengerInfoResponse> bulkCreateChallenger(
        @RequestBody List<CreateChallengerInfoRequest> requests
    ) {
        return requests.stream()
            .map(request ->
                getChallengerInfoResponse(
                    manageChallengerUseCase.createChallenger(request.toCommand())
                )
            )
            .toList();
    }

    @Operation(summary = "챌린저 비활성화 (제명/탈부 처리)")
    @PostMapping("{challengerId}/deactivate")
    void deactivateChallenger(
        @PathVariable Long challengerId,
        @RequestBody DeactivateChallengerRequest request
    ) {
        manageChallengerUseCase.deactivateChallenger(request.toCommand(challengerId));
    }

    @Operation(summary = "챌린저 파트 변경")
    @PatchMapping("{challengerId}/part")
    ChallengerInfoResponse editChallengerInfo(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long challengerId,
        @RequestBody EditChallengerPartRequest request
    ) {
        manageChallengerUseCase.updateChallenger(request.toCommand(challengerId, memberPrincipal.getMemberId()));

        return getChallengerInfoResponse(challengerId);
    }

    @Operation(summary = "[주의] 챌린저 삭제 (Hard Delete)")
    @DeleteMapping("{challengerId}")
    void deleteChallenger(@PathVariable Long challengerId) {
        DeleteChallengerCommand command = new DeleteChallengerCommand(challengerId, "관리자에 의한 삭제");
        manageChallengerUseCase.deleteChallenger(command);
    }

    // Private Methods

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
