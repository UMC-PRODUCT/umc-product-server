package com.umc.product.challenger.adapter.in.web;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.challenger.adapter.in.web.dto.request.SearchChallengerDeactivationRequest;
import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerDeactivationReasonListResponse;
import com.umc.product.challenger.application.port.in.query.ListChallengerDeactivationReasonUseCase;
import com.umc.product.challenger.application.port.in.query.SearchChallengerDeactivationUseCase;
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
            - 사유 필터를 지정하면 탈부 사유가 등록된 결과만 반환합니다.
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
