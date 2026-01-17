package com.umc.product.organization.adapter.in.web;

import com.umc.product.organization.adapter.in.web.dto.request.SchoolListRequest;
import com.umc.product.organization.adapter.in.web.dto.response.SchoolDetailResponse;
import com.umc.product.organization.adapter.in.web.dto.response.SchoolPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

@Tag(name = "School", description = "학교 관리 API (관리자)")
public interface SchoolQueryControllerApi {

    @Operation(summary = "학교 목록 조회 By 박박지현", description = "학교 목록을 페이징하여 조회합니다. 키워드 검색 및 지부 필터링이 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SchoolPageResponse.class))
            )
    })
    SchoolPageResponse getSchools(
            @ParameterObject SchoolListRequest request,
            @ParameterObject Pageable pageable
    );

    @Operation(summary = "학교 상세 조회 By 박박지현", description = "학교 상세 정보를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SchoolDetailResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "학교를 찾을 수 없음")
    })
    SchoolDetailResponse getSchoolDetail(
            @Parameter(description = "학교 ID", required = true) Long schoolId
    );
}
