package com.umc.product.challenger.adapter.in.web;

import com.umc.product.challenger.adapter.in.web.dto.request.CreateChallengerInfoRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.DeactivateChallengerRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.DeleteChallengerPointRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.EditChallengerPartRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.EditChallengerPointRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.GlobalSearchChallengerRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.GrantChallengerPointRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.SearchChallengerCursorRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.SearchChallengerRequest;
import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerInfoResponse;
import com.umc.product.challenger.adapter.in.web.dto.response.CursorSearchChallengerResponse;
import com.umc.product.challenger.adapter.in.web.dto.response.GlobalSearchChallengerResponse;
import com.umc.product.challenger.adapter.in.web.dto.response.SearchChallengerResponse;
import com.umc.product.challenger.application.port.in.command.ManageChallengerUseCase;
import com.umc.product.challenger.application.port.in.command.dto.DeleteChallengerCommand;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.SearchChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
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
    private final SearchChallengerUseCase searchChallengerUseCase;
    private final GetMemberUseCase getMemberUseCase;

    @Operation(summary = "챌린저 정보 조회")
    @GetMapping("{challengerId}")
    ChallengerInfoResponse getChallengerInfo(
        @PathVariable Long challengerId
    ) {
        ChallengerInfo challengerInfo = getChallengerUseCase.getChallengerPublicInfo(challengerId);
        MemberInfo memberInfo = getMemberUseCase.getById(challengerInfo.memberId());

        return ChallengerInfoResponse.from(challengerInfo, memberInfo);
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

    @Deprecated
    @Operation(summary = "챌린저 검색", deprecated = true,
        description = "Deprecated: cursor와 offset을 분리하기 위해 엔드포인트를 변경합니다. 해당 API는 사용하지 않습니다.")
    @GetMapping("search")
    SearchChallengerResponse searchChallenger() {
        throw new UnsupportedOperationException("이 API는 더 이상 지원되지 않습니다. cursor와 offset 기반 검색을 위한 별도의 엔드포인트를 사용하세요.");
    }

    @Operation(summary = "챌린저 검색 (Cursor 기반)")
    @GetMapping("search/cursor")
    CursorSearchChallengerResponse cursorSearchChallenger(
        @ParameterObject SearchChallengerCursorRequest searchRequest
    ) {
        return CursorSearchChallengerResponse.from(
            searchChallengerUseCase.cursorSearch(
                searchRequest.toQuery(),
                searchRequest.cursor(),
                searchRequest.getSize()
            )
        );
    }

    @Operation(summary = "챌린저 검색 (Offset 기반)")
    @GetMapping("search/offset")
    SearchChallengerResponse searchChallenger(
        @ParameterObject Pageable pageable,
        @ParameterObject SearchChallengerRequest searchRequest
    ) {
        return SearchChallengerResponse.from(
            searchChallengerUseCase.search(
                searchRequest.toQuery(),
                pageable
            )
        );
    }

    @Operation(summary = "챌린저 전체 검색 (Cursor 기반, 일정 생성용)",
        description = "전체 챌린저를 대상으로 이름 또는 닉네임을 이용해 챌린저를 검색합니다.")
    @GetMapping("search/global")
    GlobalSearchChallengerResponse globalSearchChallenger(
        @ParameterObject GlobalSearchChallengerRequest searchRequest
    ) {
        return GlobalSearchChallengerResponse.from(
            searchChallengerUseCase.globalCursorSearch(
                searchRequest.toQuery(),
                searchRequest.cursor(),
                searchRequest.getSize()
            )
        );
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
