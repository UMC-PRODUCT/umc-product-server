package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = Constants.ORGANIZATION)
public interface GisuControllerApi {

    @Operation(summary = "활성 기수 변경 ", description = "해당 기수를 활성 상태로 변경합니다. 기존 활성 기수는 비활성화됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "활성화 성공"),
            @ApiResponse(responseCode = "404", description = "기수를 찾을 수 없음")
    })
    void updateActiveGisu(@Parameter(description = "기수 ID", required = true) Long gisuId);
}
