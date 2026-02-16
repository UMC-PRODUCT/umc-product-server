package com.umc.product.organization.adapter.in.web.swagger;

import com.umc.product.organization.adapter.in.web.dto.response.ChapterListResponse;
import com.umc.product.organization.adapter.in.web.dto.response.ChapterListResponse.ChapterItem;
import com.umc.product.organization.adapter.in.web.dto.response.ChapterWithSchoolsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Organization | 지부 Query", description = "지부 목록 및 상세 조회")
public interface ChapterQueryControllerApi {

    @Operation(summary = "지부 목록 조회", description = "전체 지부 목록을 조회합니다")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ChapterListResponse.class))
        )
    })
    ChapterListResponse getAllChapters();

    @Operation(summary = "기수별 지부 및 소속 학교 목록 조회", description = "특정 기수의 모든 지부와 각 지부에 속한 학교 목록을 조회합니다")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ChapterWithSchoolsResponse.class))
        )
    })
    ChapterWithSchoolsResponse getChaptersWithSchoolsByGisuId(
        @Parameter(description = "기수 ID", required = true, example = "1") Long gisuId
    );

    @Operation(summary = "지부 ID로 지부 조회")
    ChapterItem getChapterById(@PathVariable Long chapterId);
}
