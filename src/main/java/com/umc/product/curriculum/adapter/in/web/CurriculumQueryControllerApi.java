package com.umc.product.curriculum.adapter.in.web;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.adapter.in.web.dto.response.CurriculumProgressResponse;
import com.umc.product.curriculum.adapter.in.web.dto.response.CurriculumWeeksResponse;
import com.umc.product.global.constant.SwaggerTag.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = Constants.CURRICULUM)
public interface CurriculumQueryControllerApi {

    @Operation(
            summary = "내 커리큘럼 진행 상황 조회",
            description = "챌린저의 커리큘럼 진행 상황을 조회합니다. " +
                    "각 주차별 워크북의 상태(기본/진행중/제출완료/통과/실패)를 반환합니다."
    )
    CurriculumProgressResponse getMyProgress();

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
