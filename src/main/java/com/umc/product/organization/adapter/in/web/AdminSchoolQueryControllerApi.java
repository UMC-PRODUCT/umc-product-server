package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.organization.adapter.in.web.dto.request.SchoolListRequest;
import com.umc.product.organization.adapter.in.web.dto.response.SchoolDetailResponse;
import com.umc.product.organization.adapter.in.web.dto.response.SchoolPageResponse;
import com.umc.product.organization.adapter.in.web.dto.response.UnassignedSchoolListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

@Tag(name = Constants.ORGANIZATION)
public interface AdminSchoolQueryControllerApi {

    @Operation(summary = "학교 목록 조회 ", description = "학교 목록을 페이징하여 조회합니다. 키워드 검색 및 지부 필터링이 가능합니다.")
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

    @Operation(summary = "학교 상세 조회 ", description = "학교 상세 정보를 조회합니다")
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

    @Operation(summary = "배정 대기 중인 학교 목록 조회", description = "특정 기수에서 어떤 지부에도 속하지 않은 학교 목록을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UnassignedSchoolListResponse.class))
            )
    })
    UnassignedSchoolListResponse getUnassignedSchools(
            @Parameter(description = "기수 ID", required = true, example = "1") Long gisuId
    );
}

