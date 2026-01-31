package com.umc.product.challenger.adapter.in.web;

import com.umc.product.challenger.adapter.in.web.dto.request.CreateChallengerInfoRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.DeactivateChallengerRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.DeleteChallengerPointRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.EditChallengerPartRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.EditChallengerPointRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.GrantChallengerPointRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.SearchChallengerRequest;
import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerInfoResponse;
import com.umc.product.challenger.application.port.in.command.ManageChallengerUseCase;
import com.umc.product.challenger.application.port.in.command.dto.DeleteChallengerCommand;
import com.umc.product.challenger.application.port.in.command.dto.UpdateChallengerCommand;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.global.response.PageResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Challenger 관련 기능을 담당하는 컨트롤러 입니다.
 */
@RestController
@RequestMapping("/api/v1/challenger")
@RequiredArgsConstructor
@Tag(name = Constants.CHALLENGER)
public class ChallengerController {

    private final GetChallengerUseCase getChallengerUseCase;
    private final ManageChallengerUseCase manageChallengerUseCase;

    @Operation(summary = "챌린저 정보 조회")
    @GetMapping("{challengerId}")
    ChallengerInfoResponse getChallengerInfo(
            @PathVariable Long challengerId
    ) {
        ChallengerInfo info = getChallengerUseCase.getChallengerPublicInfo(challengerId);
        return ChallengerInfoResponse.from(info);
    }

    @Operation(summary = "[주의] 챌린저 삭제 (Hard Delete)")
    @DeleteMapping("{challengerId}")
    void deleteChallenger(@PathVariable Long challengerId) {
        DeleteChallengerCommand command = new DeleteChallengerCommand(challengerId, "관리자에 의한 삭제");
        manageChallengerUseCase.deleteChallenger(command);
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
        ChallengerInfo info = getChallengerUseCase.getChallengerPublicInfo(challengerId);
        return ChallengerInfoResponse.from(info);
    }

    @Operation(summary = "챌린저 상벌점 부여")
    @PostMapping("{challengerId}/points")
    ChallengerInfoResponse grantChallengerPoints(
            @PathVariable Long challengerId,
            @RequestBody GrantChallengerPointRequest request
    ) {
        manageChallengerUseCase.grantChallengerPoint(request.toCommand(challengerId));
        ChallengerInfo info = getChallengerUseCase.getChallengerPublicInfo(challengerId);
        return ChallengerInfoResponse.from(info);
    }

    @Operation(summary = "챌린저 상벌점 사유 수정")
    @PatchMapping("points/{challengerPointId}")
    void editChallengerPoints(
            @PathVariable Long challengerPointId,
            @RequestBody EditChallengerPointRequest request
    ) {
        manageChallengerUseCase.updateChallengerPoint(request.toCommand(challengerPointId));
    }

    @Operation(summary = "챌린저 상벌점 삭제")
    @DeleteMapping("points/{challengerPointId}")
    void deleteChallengerPoint(@PathVariable Long challengerPointId) {
        manageChallengerUseCase.deleteChallengerPoint(
                new DeleteChallengerPointRequest().toCommand(challengerPointId)
        );
    }

    @Operation(summary = "챌린저 검색 (챌린저 ID, 닉네임, 기수별)")
    @GetMapping("search")
    PageResponse<ChallengerInfoResponse> searchChallenger(
            @ParameterObject Pageable pageable,
            @ParameterObject SearchChallengerRequest searchRequest
    ) {
        // TODO: SearchChallengerUseCase 구현 필요
        throw new NotImplementedException();
    }

    @Operation(summary = "챌린저 생성 (합격 처리와 통합 필요)")
    @PostMapping
    ChallengerInfoResponse createChallenger(@RequestBody CreateChallengerInfoRequest request) {
        Long challengerId = manageChallengerUseCase.createChallenger(request.toCommand());
        ChallengerInfo info = getChallengerUseCase.getChallengerPublicInfo(challengerId);
        return ChallengerInfoResponse.from(info);
    }

    @Operation(summary = "챌린저 Bulk 생성")
    @PostMapping("bulk")
    List<ChallengerInfoResponse> bulkCreateChallenger(
            @RequestBody List<CreateChallengerInfoRequest> requests
    ) {
        return requests.stream()
                .map(request -> {
                    Long challengerId = manageChallengerUseCase.createChallenger(request.toCommand());
                    ChallengerInfo info = getChallengerUseCase.getChallengerPublicInfo(challengerId);
                    return ChallengerInfoResponse.from(info);
                })
                .toList();
    }
}
