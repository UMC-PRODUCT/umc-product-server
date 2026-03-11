package com.umc.product.curriculum.adapter.in.web.swagger;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.adapter.in.web.dto.response.CurriculumProgressResponse;
import com.umc.product.curriculum.adapter.in.web.dto.response.CurriculumResponse;
import com.umc.product.curriculum.adapter.in.web.dto.response.CurriculumWeeksResponse;
import com.umc.product.global.security.MemberPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Curriculum | 커리큘럼 Query", description = "커리큘럼 조회 (본인/파트별)")
public interface CurriculumQueryControllerApi {

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
    CurriculumResponse getCurriculum(
        @Parameter(description = "파트", required = true) ChallengerPart part
    );

    @Operation(
        summary = "내 커리큘럼 진행 상황 조회",
        description = "챌린저의 커리큘럼 진행 상황을 조회합니다. " +
            "각 주차별 워크북의 상태(기본/진행중/제출완료/통과/실패)를 반환합니다."
    )
    CurriculumProgressResponse getMyProgress(MemberPrincipal memberPrincipal);

    @Operation(
        summary = "파트별 커리큘럼 주차 목록 조회",
        description = "파트를 기준으로 활성 기수의 커리큘럼 주차 목록을 조회합니다. " +
            "각 주차의 번호와 제목만 반환합니다."
    )
    CurriculumWeeksResponse getWeeksByPart(
        @Parameter(description = "파트", required = true, example = "SPRINGBOOT")
        ChallengerPart part
    );
}
