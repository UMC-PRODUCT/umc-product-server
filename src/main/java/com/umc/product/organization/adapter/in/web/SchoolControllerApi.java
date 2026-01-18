package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.organization.adapter.in.web.dto.request.CreateSchoolRequest;
import com.umc.product.organization.adapter.in.web.dto.request.DeleteSchoolsRequest;
import com.umc.product.organization.adapter.in.web.dto.request.UpdateSchoolRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = Constants.ORGANIZATION)
public interface SchoolControllerApi {

    @Operation(summary = "학교 생성 By 박박지현", description = "새로운 학교를 등록합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "지부를 찾을 수 없음")
    })
    void createSchool(CreateSchoolRequest request);

    @Operation(summary = "학교 수정 By 박박지현", description = "학교 정보를 수정합니다. 입력된 필드만 수정됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "학교를 찾을 수 없음")
    })
    void updateSchool(
            @Parameter(description = "학교 ID", required = true) Long schoolId,
            UpdateSchoolRequest request
    );

    @Operation(summary = "학교 삭제 By 박박지현", description = "여러 학교를 일괄 삭제합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "삭제할 학교 ID가 비어있음")
    })
    void deleteSchools(DeleteSchoolsRequest request);
}
