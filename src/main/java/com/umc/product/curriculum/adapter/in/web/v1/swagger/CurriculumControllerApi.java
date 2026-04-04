package com.umc.product.curriculum.adapter.in.web.v1.swagger;

import com.umc.product.curriculum.adapter.in.web.v1.dto.request.SubmitChallengerWorkbookRequest;
import com.umc.product.curriculum.adapter.in.web.v1.dto.request.SubmitWorkbookRequest;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Curriculum | 챌린저용 워크북 Command", description = "미션 제출, ")
public interface CurriculumControllerApi {

    @Operation(
        summary = "deprecated: 원본 워크북 ID로 미션 제출",
        description = """
            ⚠️ Deprecated since `v1.3.x`\s\s

            `OriginalWorkbookId`를 이용해서 워크북에 대한 미션을 제출합니다.

            `POST /api/v1/workbooks/{challengerWorkbookId}/submission`을 사용해주세요.\s\s

            챌린저가 원본 워크북에 링크(깃허브, 노션 등)를 제출합니다.\s\s\s\s

            - 제출 시 새 챌린저 워크북이 생성됩니다.\s\s
            - 같은 챌린저가 같은 원본 워크북에 중복 제출할 수 없습니다.\s\s
            - 제출 시 상태는 SUBMITTED가 됩니다.
            """,
        deprecated = true
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "제출 성공"),
        @ApiResponse(responseCode = "400", description = "중복 제출이거나 제출 내용이 유효하지 않음"),
        @ApiResponse(responseCode = "404", description = "원본 워크북을 찾을 수 없음")
    })
    void submitWorkbook(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestBody(description = "워크북 제출 요청") SubmitWorkbookRequest request
    );

    @Operation(
        summary = "챌린저 워크북 ID로 미션 제출",
        description = """
            챌린저가 배포된 챌린저 워크북에 링크(깃허브, 노션 등)를 제출합니다.

            `ChallengerWorkbookId`로 미션을 제출합니다.
            추후 챌린저 워크북 생성 API가 구현되면, 해당 API를 먼저 호출하여 챌린저 워크북을 생성한 후, 미션을 제출하도록 하여야 합니다.

            - 이미 배포된 챌린저 워크북의 ID를 경로에 포함합니다.
            - 본인의 워크북만 제출할 수 있습니다.
            - 제출 시 상태는 PENDING → SUBMITTED로 변경됩니다.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "제출 성공"),
        @ApiResponse(responseCode = "400", description = "워크북 상태가 유효하지 않거나 제출 내용이 필요함"),
        @ApiResponse(responseCode = "403", description = "해당 워크북에 대한 접근 권한이 없음"),
        @ApiResponse(responseCode = "404", description = "챌린저 워크북을 찾을 수 없음")
    })
    void submitChallengerWorkbook(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "챌린저 워크북 ID", required = true) Long challengerWorkbookId,
        @RequestBody(description = "워크북 제출 요청") SubmitChallengerWorkbookRequest request
    );
}
