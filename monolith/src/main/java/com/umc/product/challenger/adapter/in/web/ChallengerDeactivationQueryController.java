package com.umc.product.challenger.adapter.in.web;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.challenger.adapter.in.web.dto.request.SearchChallengerDeactivationRequest;
import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerDeactivationReasonListResponse;
import com.umc.product.challenger.application.port.in.query.GetChallengerDeactivationDetailUseCase;
import com.umc.product.challenger.application.port.in.query.ListChallengerDeactivationReasonUseCase;
import com.umc.product.challenger.application.port.in.query.SearchChallengerDeactivationUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationDetailInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationListItemInfo;
import com.umc.product.global.response.PageResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/challenger/admin/deactivations")
@RequiredArgsConstructor
@Tag(name = "Challenger | 탈부 관리 Query", description = "관리자용 챌린저 탈부 조회 기능을 제공합니다.")
public class ChallengerDeactivationQueryController {

    private final ListChallengerDeactivationReasonUseCase listChallengerDeactivationReasonUseCase;
    private final SearchChallengerDeactivationUseCase searchChallengerDeactivationUseCase;
    private final GetChallengerDeactivationDetailUseCase getChallengerDeactivationDetailUseCase;

    @GetMapping
    @Operation(
        operationId = "CHALLENGER-105",
        summary = "탈부 관리 목록 조회",
        description = """
            등록 전 후보와 탈부 등록 완료자를 조회합니다.
            - 등록 전은 활성 상태이며 상벌점 총점이 -20점 이하인 챌린저입니다.
            - 탈부·제명 처리된 챌린저는 점수와 관계없이 등록 완료(REGISTERED)로 표시됩니다.
            - 등록 전 후보와 상세 기록이 없는 기존 탈부·제명자는 탈부 ID, 사유, 등록 시각이 없습니다.
            - 활성 기수 중앙운영진과 SUPER_ADMIN은 과거 기수도 조회할 수 있습니다.
            - 교내 회장·부회장은 활성 기수 본교로 제한됩니다. 권한 또는 범위 위반 시 403을 반환합니다.
            - 화면의 ‘사유’ 필터(type)는 VOLUNTARY(자진 탈부) 또는 INVOLUNTARY(타의 탈부)를 받습니다. ‘사유 전체’를 선택하면 type을 생략합니다.
            - 상벌점 합계가 -20점 이하인 등록 전 후보는 타의 탈부 필터 결과에 포함됩니다.
            - 세부 사유(reason)를 지정하면 해당 사유가 등록된 결과만 조회되므로 등록 전 후보는 제외됩니다.
            - type과 reason을 함께 지정하면 두 조건을 모두 만족하는 결과만 반환합니다.
            - 정렬 항목은 name, registeredAt이며, 기본 정렬은 등록 시각 내림차순입니다.
            - registeredAt은 시간대가 포함된 시각입니다. 콘솔에서는 한국 날짜로 표시합니다.
            """
    )
    public PageResponse<ChallengerDeactivationListItemInfo> searchDeactivations(
        @ParameterObject @Valid @ModelAttribute SearchChallengerDeactivationRequest request,
        @ParameterObject @PageableDefault(size = 10) Pageable pageable,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        return PageResponse.of(searchChallengerDeactivationUseCase.searchDeactivations(
            memberPrincipal.getMemberId(), request.toQuery(), pageable
        ));
    }

    @GetMapping("/{deactivationId}")
    @Operation(
        operationId = "CHALLENGER-106",
        summary = "탈부 상세 조회",
        description = """
            탈부 목록의 deactivationId로 등록 정보, 근거 자료, 진행 기록을 조회합니다.
            - 활성 기수 중앙운영진과 SUPER_ADMIN은 과거 기록도 조회할 수 있습니다.
            - 교내 회장·부회장은 활성 기수 본교 기록만 조회할 수 있습니다. 범위 위반 시 403을 반환합니다.
            - 등록 전 후보와 상세 기록이 없는 기존 탈부·제명자는 목록에 deactivationId가 없습니다.
            - 존재하지 않는 상세 기록은 404를 반환합니다.
            - 사진과 링크는 등록 순서, 진행 기록은 최신순입니다. 사진 URL은 유효 시간이 있습니다.
            - 진행 기록의 사진과 링크 수는 해당 작업 한 번에 등록한 개수입니다.
            """
    )
    public ChallengerDeactivationDetailInfo getDetail(
        @PathVariable Long deactivationId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        return getChallengerDeactivationDetailUseCase.getDetail(memberPrincipal.getMemberId(), deactivationId);
    }

    @GetMapping("/reasons")
    @Operation(
        operationId = "CHALLENGER-104",
        summary = "탈부 사유 목록 조회",
        description = "자진 탈부와 타의 탈부에 사용할 수 있는 사유를 화면 표시 순서대로 반환합니다."
    )
    public ChallengerDeactivationReasonListResponse listReasons() {
        return ChallengerDeactivationReasonListResponse.from(
            listChallengerDeactivationReasonUseCase.listReasons()
        );
    }
}
