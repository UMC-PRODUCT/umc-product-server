package com.umc.product.organization.adapter.in.web;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.response.PageResponse;
import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.response.umcproduct.UmcProductMemberPageResponse;
import com.umc.product.organization.adapter.in.web.dto.response.umcproduct.UmcProductMemberResponse;
import com.umc.product.organization.application.port.in.query.GetUmcProductMemberUseCase;
import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductMemberSearchCondition;
import com.umc.product.organization.domain.enums.UmcProductFunctionalRole;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;
import com.umc.product.organization.domain.enums.UmcProductPosition;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Public
@RestController
@RequestMapping("/api/v1/umc-product/members")
@RequiredArgsConstructor
@Tag(name = "Organization | UMC Product 멤버 Query", description = "UMC Product 멤버 목록 및 상세 조회")
public class UmcProductMemberQueryController {

    private final GetUmcProductMemberUseCase getUmcProductMemberUseCase;

    @GetMapping
    @Operation(
        operationId = "UMC-PRODUCT-MEMBER-101",
        summary = "[UMC-PRODUCT-MEMBER-101] UMC Product 멤버 검색",
        description = "기수, 기능 조직, 역할, 포지션, 스쿼드 조건으로 UMC Product 멤버를 페이지 조회합니다. 멤버 기본 정보와 기능 조직 소속, 스쿼드 참여 정보를 함께 반환합니다."
    )
    public UmcProductMemberPageResponse search(
        @RequestParam(required = false) Long umcProductGenerationId,
        @RequestParam(required = false) Long functionalUnitId,
        @RequestParam(required = false) UmcProductFunctionalUnitType functionalUnitType,
        @RequestParam(required = false) UmcProductFunctionalRole role,
        @RequestParam(required = false) UmcProductPosition position,
        @RequestParam(required = false) Long squadId,
        Pageable pageable
    ) {
        UmcProductMemberSearchCondition condition = UmcProductMemberSearchCondition.of(
            umcProductGenerationId,
            functionalUnitId,
            functionalUnitType,
            role,
            position,
            squadId
        );
        PageResponse<UmcProductMemberResponse> pageResponse = PageResponse.of(
            getUmcProductMemberUseCase.search(condition, pageable),
            UmcProductMemberResponse::from
        );
        return UmcProductMemberPageResponse.from(pageResponse);
    }

    @GetMapping("/{umcProductMemberId}")
    @Operation(
        operationId = "UMC-PRODUCT-MEMBER-102",
        summary = "[UMC-PRODUCT-MEMBER-102] UMC Product 멤버 상세 조회",
        description = "UMC Product 멤버 ID로 단건 상세 정보를 조회합니다. 멤버 기본 정보, Product 전용 프로필, 기능 조직 소속, 스쿼드 참여 정보를 반환합니다."
    )
    public UmcProductMemberResponse get(@PathVariable Long umcProductMemberId) {
        return UmcProductMemberResponse.from(getUmcProductMemberUseCase.getById(umcProductMemberId));
    }
}
