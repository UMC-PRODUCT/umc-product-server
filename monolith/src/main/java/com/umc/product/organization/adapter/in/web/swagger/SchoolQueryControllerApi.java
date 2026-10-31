package com.umc.product.organization.adapter.in.web.swagger;

import com.umc.product.organization.adapter.in.web.dto.response.school.SchoolDetailResponse;
import com.umc.product.organization.adapter.in.web.dto.response.school.SchoolNameListResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Organization | 학교 Query", description = "")
public interface SchoolQueryControllerApi {

    @Operation(operationId = "SCHOOL-101", summary = "학교 전체 목록 조회", description = "전체 학교 목록을 이름순으로 조회합니다. 학교 ID와 이름만 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = SchoolNameListResponse.class))
        )
    })
    SchoolNameListResponse getAllSchools();

    @Operation(operationId = "SCHOOL-102", summary = "학교 상세 조회", description = "학교 상세 정보를 조회합니다")
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
