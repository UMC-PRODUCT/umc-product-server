package com.umc.product.curriculum.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = Constants.ADMIN)
public interface AdminWorkbookControllerApi {

    @Operation(
            summary = "[정식] 워크북 배포",
            description = """
                    개별 워크북을 배포합니다.

                    배포된 워크북만 앱에서 조회됩니다.
                    - 배포 전: releasedAt = null
                    - 배포 후: releasedAt = 현재 시간
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "배포 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "워크북을 찾을 수 없음"
            )
    })
    void releaseWorkbook(
            @Parameter(description = "워크북 ID", required = true) Long workbookId
    );
}
