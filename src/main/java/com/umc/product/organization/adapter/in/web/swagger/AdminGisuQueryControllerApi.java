package com.umc.product.organization.adapter.in.web.swagger;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.response.ActiveGisuResponse;
import com.umc.product.organization.adapter.in.web.dto.response.GisuNameListResponse;
import com.umc.product.organization.adapter.in.web.dto.response.GisuPageResponse;
import com.umc.product.organization.adapter.in.web.dto.response.GisuResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Organization | 기수 Query", description = "")
public interface AdminGisuQueryControllerApi {

    @Public
    @GetMapping("/{gisuId}")
    GisuResponse getGisu(@PathVariable Long gisuId);

    @Operation(summary = "기수 목록 조회 ", description = "기수 목록을 최신순(generation 내림차순)으로 페이징 조회합니다")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = GisuPageResponse.class))
        )
    })
    GisuPageResponse getGisuList(Pageable pageable);

    @Operation(summary = "기수 전체 목록 조회", description = "전체 기수 목록을 최신순(generation 내림차순)으로 조회합니다. 기수 ID와 기수 번호만 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = GisuNameListResponse.class))
        )
    })
    GisuNameListResponse getAllGisu();

    @Operation(summary = "활성화된 기수 조회", description = "현재 활성화된 기수의 ID와 기수 번호를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ActiveGisuResponse.class))
        )
    })
    ActiveGisuResponse getActiveGisu();
}
