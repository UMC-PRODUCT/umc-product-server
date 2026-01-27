package com.umc.product.curriculum.adapter.in.web;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.adapter.in.web.dto.request.ManageCurriculumRequest;
import com.umc.product.curriculum.adapter.in.web.dto.response.AdminCurriculumResponse;
import com.umc.product.global.constant.SwaggerTag.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = Constants.ADMIN)
public interface AdminCurriculumControllerApi {

    @Operation(
            summary = "파트별 커리큘럼 조회",
            description = "현재 활성화된 기수의 파트별 커리큘럼과 워크북 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    AdminCurriculumResponse getCurriculum(
            @Parameter(description = "파트", required = true) ChallengerPart part
    );

    @Operation(
            summary = "커리큘럼 관리 (생성/수정/삭제)",
            description = """
                    현재 활성화된 기수의 커리큘럼과 워크북을 일괄 관리합니다.

                    ## 기본 동작
                    - 커리큘럼: 활성 기수 + part로 조회하여 없으면 생성, 있으면 title 업데이트
                    - 워크북:
                      - id가 있으면 수정
                      - id가 없으면 생성
                      - 기존에 있던 워크북이 요청에 없으면 삭제
                    - 제출된 워크북(ChallengerWorkbook이 존재)은 삭제 불가 (409 에러)

                    ## 선택 필드 (startDate, endDate, missionType, workbookUrl)
                    화면에 있는 필드만 보내도 됩니다.
                    - **신규 생성 (id 없음)**: 기본값 적용 (날짜: 2099-12-31, 미션타입: LINK)
                    - **수정 (id 있음)**: 보내지 않은 필드는 기존값 유지

                    예시) 웹에서 title, description만 수정하는 경우:
                    ```json
                    { "id": 1, "weekNo": 1, "title": "수정된 제목", "description": "수정된 설명" }
                    ```
                    → startDate, endDate, missionType은 기존값 그대로 유지됨
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "관리 성공",
                    content = @Content(schema = @Schema(implementation = Long.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "제출된 워크북이 있어 삭제 불가"
            )
    })
    void manageCurriculum(ManageCurriculumRequest request);
}
