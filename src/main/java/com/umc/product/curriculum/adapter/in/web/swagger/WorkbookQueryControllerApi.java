package com.umc.product.curriculum.adapter.in.web.swagger;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.adapter.in.web.dto.response.AvailableWeeksResponse;
import com.umc.product.curriculum.adapter.in.web.dto.response.StudyGroupFilterResponse;
import com.umc.product.curriculum.adapter.in.web.dto.response.WorkbookSubmissionResponse;
import com.umc.product.global.response.CursorResponse;
import com.umc.product.global.security.MemberPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

/**
 * 워크북 제출 현황 조회 API (학교 운영진 전용)
 *
 * <p>학교 운영진(회장/부회장/파트장/기타 운영진)이 파트원들의 워크북 제출 현황을 조회합니다.</p>
 *
 * <h2>필터링 옵션</h2>
 * <ul>
 *   <li>weekNo: 주차별 필터링 (필수)</li>
 *   <li>studyGroupId: 스터디 그룹별 필터링</li>
 * </ul>
 *
 * <h2>권한별 동작</h2>
 * <ul>
 *   <li>학교 회장/부회장: 본인 학교의 모든 파트 조회 가능</li>
 *   <li>학교 파트장/기타 운영진: 본인 학교의 담당 파트만 조회 가능</li>
 * </ul>
 */
@Tag(name = "Curriculum | 워크북 Query", description = "")
public interface WorkbookQueryControllerApi {

    @Operation(
        summary = "워크북 제출 현황 조회",
        description = "주차, 스터디 그룹별로 파트원의 워크북 제출 현황을 조회합니다. 학교 운영진(회장/부회장/파트장/기타 운영진)만 접근 가능합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (학교 운영진이 아닌 경우)")
    })
    CursorResponse<WorkbookSubmissionResponse> getWorkbookSubmissions(
        @Parameter(hidden = true) MemberPrincipal memberPrincipal,
        @Parameter(description = "주차 (필수)", required = true) Integer weekNo,
        @Parameter(description = "스터디 그룹 ID (선택)") Long studyGroupId,
        @Parameter(description = "페이지 커서 (첫 페이지는 null)") Long cursor,
        @Parameter(description = "페이지 크기 (기본 20, 최대 100)") int size
    );

    @Operation(
        summary = "배포된 주차 번호 목록 조회",
        description = "워크북 제출 현황 필터에 사용할 배포된 주차 번호 목록을 조회합니다. "
            + "회장/부회장은 모든 파트의 주차를 조회하고, 파트장/운영진은 자신의 파트 주차만 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (학교 운영진이 아닌 경우)")
    })
    AvailableWeeksResponse getAvailableWeeks(
        @Parameter(hidden = true) MemberPrincipal memberPrincipal
    );

    @Operation(
        summary = "필터용 스터디 그룹 목록 조회",
        description = "워크북 제출 현황 필터에 사용할 스터디 그룹 목록을 조회합니다. "
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    List<StudyGroupFilterResponse> getStudyGroupsForFilter(
        @Parameter(description = "학교 ID", required = true) Long schoolId,
        @Parameter(description = "파트", required = true) ChallengerPart part
    );
}
