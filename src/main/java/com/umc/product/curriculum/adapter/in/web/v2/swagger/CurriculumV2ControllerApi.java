package com.umc.product.curriculum.adapter.in.web.v2.swagger;

import com.umc.product.global.security.MemberPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Curriculum V2 | 챌린저용 워크북 Command", description = "워크북 제출, 수정")
public interface CurriculumV2ControllerApi {

    @Operation(
        summary = "워크북 제출물 수정",
        description = """
            챌린저가 제출한 워크북의 내용을 수정합니다.

            - 본인의 제출물만 수정할 수 있습니다.
            - 수정 가능한 상태에서만 수정할 수 있습니다.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "워크북 상태가 유효하지 않거나 수정 내용이 필요함"),
        @ApiResponse(responseCode = "403", description = "해당 제출물에 대한 접근 권한이 없음"),
        @ApiResponse(responseCode = "404", description = "워크북을 찾을 수 없음")
    })
    void updateSubmission(
        @Parameter(hidden = true) MemberPrincipal memberPrincipal,
        @Parameter(description = "워크북 ID", required = true) Long challengerWorkbookId
    );
}
