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

    /**
     * @deprecated {@code GET /api/v2/curriculums/{gisuId}?part={part}} 사용 권장.
     *             v2는 gisuId를 직접 지정하여 특정 기수의 커리큘럼 조회 가능.
     * @since 1.3.0
     * @see CurriculumQueryV2ControllerApi
     */
    @Deprecated(since = "1.3.0", forRemoval = true)
    @Operation(
        summary = "파트별 커리큘럼 조회",
        description = "현재 활성화된 기수의 파트별 커리큘럼과 워크북 목록을 조회합니다.\n\n" +
            "⚠️ **Deprecated (v1.3.0)**: 2026-04-01 제거 예정.\n\n" +
            "`GET /api/v2/curriculums/{gisuId}?part={part}` 사용을 권장합니다.",
        deprecated = true
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

    /**
     * @deprecated {@code GET /api/v2/curriculums/challengers/me/progress?gisuId={gisuId}} 사용 권장.
     *             v1은 현재 활성 기수 기준으로만 조회되어 이전 기수 사용자 조회 불가.
     * @since 1.3.0
     */
    @Deprecated(since = "1.3.0", forRemoval = true)
    @Operation(
        summary = "내 커리큘럼 진행 상황 조회",
        description = "챌린저의 커리큘럼 진행 상황을 조회합니다. " +
            "각 주차별 워크북의 상태(기본/진행중/제출완료/통과/실패)를 반환합니다.\n\n" +
            "⚠️ **Deprecated (v1.3.0)**: 2026-04-01 제거 예정.\n\n" +
            "현재 활성 기수 기준으로만 조회되어 이전 기수 사용자는 조회가 불가합니다. " +
            "`GET /api/v2/curriculums/challengers/me/progress?gisuId={gisuId}` 사용을 권장합니다.",
        deprecated = true
    )
    CurriculumProgressResponse getMyProgress(MemberPrincipal memberPrincipal);

    /**
     * @deprecated {@code GET /api/v2/curriculums/{gisuId}?part={part}} 사용 권장.
     *             v2 응답의 workbooks 필드에 weekNo, title이 포함되어 있습니다.
     * @since 1.3.0
     * @see CurriculumQueryV2ControllerApi
     */
    @Deprecated(since = "1.3.0", forRemoval = true)
    @Operation(
        summary = "파트별 커리큘럼 주차 목록 조회",
        description = "파트를 기준으로 활성 기수의 커리큘럼 주차 목록을 조회합니다. " +
            "각 주차의 번호와 제목만 반환합니다.\n\n" +
            "⚠️ **Deprecated (v1.3.0)**: 2026-04-01 제거 예정.\n\n" +
            "`GET /api/v2/curriculums/{gisuId}?part={part}` 응답의 `workbooks` 필드에 주차 정보가 포함되어 있습니다. " +
            "특정 주차만 필요한 경우 `week={weekNo}` 파라미터를 추가하세요.",
        deprecated = true
    )
    CurriculumWeeksResponse getWeeksByPart(
        @Parameter(description = "파트", required = true, example = "SPRINGBOOT")
        ChallengerPart part
    );
}
