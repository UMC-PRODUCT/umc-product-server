package com.umc.product.organization.adapter.in.web.swagger;

import com.umc.product.organization.adapter.in.web.dto.response.SchoolLinkResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Organization | 학교 Query", description = "")
public interface SchoolQueryControllerApi {

    @Operation(summary = "학교 링크 조회 ", description = "학교의 공식 링크 정보를 조회합니다")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = SchoolLinkResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "학교를 찾을 수 없음")
    })
    SchoolLinkResponse getSchoolLink(
        @Parameter(description = "학교 ID", required = true) Long schoolId
    );
}
