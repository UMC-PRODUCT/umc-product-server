package com.umc.product.curriculum.adapter.in.web.swagger;

import com.umc.product.curriculum.adapter.in.web.dto.request.SubmitWorkbookRequest;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
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
            챌린저가 원본 워크북에 링크(깃허브, 노션 등)를 제출합니다.

            - 제출 시 새 챌린저 워크북이 생성됩니다.
            - 같은 챌린저가 같은 원본 워크북에 중복 제출할 수 없습니다.
            - 제출 시 상태는 SUBMITTED가 됩니다.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "제출 성공"),
        @ApiResponse(responseCode = "400", description = "중복 제출이거나 제출 내용이 유효하지 않음"),
        @ApiResponse(responseCode = "404", description = "원본 워크북을 찾을 수 없음")
    })
    void submitWorkbook(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "원본 워크북 ID", required = true) Long originalWorkbookId,
        @RequestBody(description = "워크북 제출 요청") SubmitWorkbookRequest request
    );
}
