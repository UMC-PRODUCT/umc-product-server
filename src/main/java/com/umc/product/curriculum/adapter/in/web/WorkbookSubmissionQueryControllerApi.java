package com.umc.product.curriculum.adapter.in.web;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.adapter.in.web.dto.response.StudyGroupFilterResponse;
import com.umc.product.curriculum.adapter.in.web.dto.response.WorkbookSubmissionResponse;
import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.global.response.CursorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

/**
 * 워크북 제출 현황 조회 API (운영진용)
 *
 * <p>파트장이 파트원들의 워크북 제출 현황을 조회합니다.</p>
 *
 * <h2>필터링 옵션</h2>
 * <ul>
 *   <li>schoolId: 학교별 필터링 (중앙 파트장용)</li>
 *   <li>weekNo: 주차별 필터링 (필수)</li>
 *   <li>studyGroupId: 스터디 그룹별 필터링</li>
 * </ul>
 *
 * <h2>권한별 동작 (추후 구현)</h2>
 * <ul>
 *   <li>중앙 파트장: schoolId 필수, 본인 파트만 조회</li>
 *   <li>학교 파트장: schoolId 무시 (본인 학교), 본인 파트만 조회</li>
 * </ul>
 */
@Tag(name = Constants.CURRICULUM)
public interface WorkbookSubmissionQueryControllerApi {

    @Operation(
            summary = "워크북 제출 현황 조회",
            description = "학교, 주차, 스터디 그룹별로 파트원의 워크북 제출 현황을 조회합니다. 커서 기반 무한스크롤. By 박박지현"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CursorResponse<WorkbookSubmissionResponse> getWorkbookSubmissions(
            @Parameter(description = "학교 ID (중앙 파트장용, 선택)") Long schoolId,
            @Parameter(description = "주차 (필수)", required = true) Integer weekNo,
            @Parameter(description = "스터디 그룹 ID (선택)") Long studyGroupId,
            @Parameter(description = "페이지 커서 (첫 페이지는 null)") Long cursor,
            @Parameter(description = "페이지 크기 (기본 20, 최대 100)") int size
    );

    @Operation(
            summary = "필터용 스터디 그룹 목록 조회",
            description = "워크북 제출 현황 필터에 사용할 스터디 그룹 목록을 조회합니다. By 박박지현"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    List<StudyGroupFilterResponse> getStudyGroupsForFilter(
            @Parameter(description = "학교 ID", required = true) Long schoolId,
            @Parameter(description = "파트", required = true) ChallengerPart part
    );
}
