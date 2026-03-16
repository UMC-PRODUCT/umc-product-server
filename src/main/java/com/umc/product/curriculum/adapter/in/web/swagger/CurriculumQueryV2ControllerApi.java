package com.umc.product.curriculum.adapter.in.web.swagger;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.adapter.in.web.dto.response.CurriculumResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Curriculum | 커리큘럼 Query", description = "커리큘럼 조회 (본인/파트별)")
public interface CurriculumQueryV2ControllerApi {

    @Operation(
        summary = "기수별 파트 커리큘럼 조회 V2",
        description = "지정한 기수의 파트별 커리큘럼과 워크북 목록을 조회합니다. " +
            "`week` 파라미터를 지정하면 해당 주차의 워크북만 반환합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공"
        )
    })
    CurriculumResponse getCurriculum(
        @Parameter(description = "기수 ID", required = true, in = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH) Long gisuId,
        @Parameter(description = "파트", required = true) ChallengerPart part,
        @Parameter(description = "주차 번호 (생략 시 전체 주차 반환)") Integer week
    );
}
