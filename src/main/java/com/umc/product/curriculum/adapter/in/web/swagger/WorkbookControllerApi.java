package com.umc.product.curriculum.adapter.in.web.swagger;

import com.umc.product.curriculum.adapter.in.web.dto.request.SubmitWorkbookRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Curriculum | 워크북 Command", description = "")
public interface WorkbookControllerApi {

    @Operation(
        summary = "워크북 제출",
        description = """
            챌린저가 워크북에 링크(깃허브, 노션 등)를 제출합니다.

            - PENDING 상태의 워크북만 제출 가능
            - 제출 시 상태가 SUBMITTED로 변경됨
            - 한 번 제출하면 수정 불가
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "제출 성공"),
        @ApiResponse(responseCode = "400", description = "워크북 상태가 유효하지 않음 (PENDING 상태만 제출 가능)"),
        @ApiResponse(responseCode = "404", description = "챌린저 워크북을 찾을 수 없음")
    })
    void submitWorkbook(
        @Parameter(description = "챌린저 워크북 ID", required = true) Long challengerWorkbookId,
        @RequestBody(description = "워크북 제출 요청") SubmitWorkbookRequest request
    );
}
