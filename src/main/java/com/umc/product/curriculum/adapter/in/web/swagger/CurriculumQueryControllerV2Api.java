package com.umc.product.curriculum.adapter.in.web.swagger;

import com.umc.product.curriculum.adapter.in.web.dto.response.CurriculumProgressResponse;
import com.umc.product.global.security.MemberPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Curriculum | 커리큘럼 Query V2", description = "커리큘럼 조회 V2")
public interface CurriculumQueryControllerV2Api {

    @Operation(
        summary = "내 커리큘럼 진행 상황 조회 (기수 지정)",
        description = "기수 ID를 지정하여 해당 기수의 커리큘럼 진행 상황을 조회합니다. " +
            "이전 기수였던 사용자도 gisuId를 전달하면 해당 기수의 진행 상황을 조회할 수 있습니다."
    )
    CurriculumProgressResponse getMyProgress(
        MemberPrincipal memberPrincipal,
        @Parameter(description = "기수 ID", required = true) Long gisuId
    );
}
