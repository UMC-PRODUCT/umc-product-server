package com.umc.product.curriculum.adapter.in.web.v1.swagger;

import com.umc.product.curriculum.adapter.in.web.v1.dto.request.ReviewWorkbookRequest;
import com.umc.product.curriculum.adapter.in.web.v1.dto.request.SelectBestWorkbookRequest;
import com.umc.product.curriculum.adapter.in.web.v1.dto.response.WorkbookSubmissionDetailResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Curriculum | 운영진용 워크북 Command", description = "워크북 배포, 챌린저 미션 검토 및 베스트 워크북 선정 등")
public interface AdminCurriculumControllerApi {

    @Operation(
        summary = "(중앙 파트장용) 원본 워크북 배포",
        description = """
            개별 워크북을 배포합니다.

            배포된 워크북만 앱에서 조회됩니다.
            - 배포 전: releasedAt = null
            - 배포 후: releasedAt = 현재 시간
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "배포 성공"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "워크북을 찾을 수 없음"
        )
    })
    void releaseWorkbook(
        @Parameter(description = "워크북 ID", required = true) Long workbookId
    );

    @Operation(
        summary = "(파트장용) 챌린저 워크북 검토",
        description = """
            제출된 챌린저 워크북을 검토합니다.

            - PASS: 통과
            - FAIL: 반려

            피드백은 선택 사항입니다.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "검토 완료"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "워크북 상태가 유효하지 않음 (SUBMITTED 상태만 검토 가능)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "챌린저 워크북을 찾을 수 없음"
        )
    })
    void reviewWorkbook(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "챌린저 워크북 ID", required = true) Long challengerWorkbookId,
        @RequestBody(description = "워크북 검토 요청") ReviewWorkbookRequest request
    );

    @Operation(
        summary = "(파트장용) 베스트 워크북 선정",
        description = """
            제출된 워크북 또는 통과된 워크북을 베스트로 선정합니다.

            - SUBMITTED 또는 PASS 상태인 워크북만 베스트 선정 가능
            - 베스트 선정 이유는 선택 사항입니다.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "베스트 선정 완료"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "워크북 상태가 유효하지 않음 (SUBMITTED 또는 PASS 상태만 베스트 선정 가능)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "챌린저 워크북을 찾을 수 없음"
        )
    })
    void selectBestWorkbook(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "챌린저 워크북 ID", required = true) Long challengerWorkbookId,
        @RequestBody(description = "베스트 워크북 선정 요청") SelectBestWorkbookRequest request
    );

    @Operation(
        summary = "(파트장용) 챌린저 워크북 제출 URL 조회",
        description = "챌린저가 제출한 워크북의 제출 URL을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = WorkbookSubmissionDetailResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "챌린저 워크북을 찾을 수 없음")
    })
    WorkbookSubmissionDetailResponse getSubmissionDetail(
        @Parameter(description = "챌린저 워크북 ID", required = true) Long challengerWorkbookId
    );
}
