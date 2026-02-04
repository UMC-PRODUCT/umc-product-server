package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.organization.adapter.in.web.dto.response.GisuListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = Constants.ORGANIZATION)
public interface AdminGisuQueryControllerApi {

    @Operation(summary = "기수 목록 조회 ", description = "전체 기수 목록을 최신순(generation 내림차순)으로 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = GisuListResponse.class))
            )
    })
    GisuListResponse getGisuList();
}
