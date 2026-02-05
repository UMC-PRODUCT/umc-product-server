package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.global.response.CursorResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupListResponse.Summary;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupNameResponse;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupPartsResponse;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupResponse;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupSchoolsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 스터디 그룹 조회 API
 *
 * <p>모든 조회는 현재 활성화된 기수(isActive=true) 기준으로 동작합니다.</p>
 *
 * <h2>API 흐름</h2>
 * <pre>
 * GET /
 *   → 내 학교/파트 기반 스터디 그룹 목록 (무한스크롤)
 *   → 그룹 선택
 *
 * GET /{groupId}
 *   → 스터디 그룹 상세 + 멤버 목록
 * </pre>
 */
@Tag(name = Constants.ORGANIZATION)
public interface StudyGroupQueryControllerApi {

        /**
         * @deprecated getStudyGroups로 대체
         */
        @Deprecated
        @Operation(summary = "[Deprecated] 학교 목록 조회", description = "스터디 그룹이 있는 학교 목록을 조회합니다.", deprecated = true)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = StudyGroupSchoolsResponse.class)))
        })
        StudyGroupSchoolsResponse getSchools();

        /**
         * @deprecated getStudyGroups로 대체
         */
        @Deprecated
        @Operation(summary = "[Deprecated] 파트 목록 조회", description = "특정 학교의 파트별 스터디 그룹 요약을 조회합니다.", deprecated = true)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = StudyGroupPartsResponse.class))),
                        @ApiResponse(responseCode = "404", description = "학교를 찾을 수 없음")
        })
        StudyGroupPartsResponse getParts(
                        @Parameter(description = "학교 ID", required = true) Long schoolId);

        @Operation(summary = "내 스터디 그룹 목록 조회", description = "로그인한 유저의 학교/파트 기반으로 스터디 그룹 목록을 조회합니다. cursor 기반 무한스크롤.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "조회 성공")
        })
        CursorResponse<Summary> getStudyGroups(
                        @Parameter(hidden = true) MemberPrincipal memberPrincipal,
                        @Parameter(description = "페이지 커서 (첫 페이지는 null)") Long cursor,
                        @Parameter(description = "페이지 크기 (기본 20, 최대 100)") int size);

<<<<<<< HEAD
        @Operation(summary = "권한에 따라 스터디 그룹 이름 목록 조회", description = "로그인한 유저의 학교/파트 기반으로 스터디 그룹의 ID와 이름 목록을 조회합니다. 토글/드롭다운 용도.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = StudyGroupNameResponse.class)))
        })
        StudyGroupNameResponse getStudyGroupNames(
                        @Parameter(hidden = true) MemberPrincipal memberPrincipal);

=======
>>>>>>> 5447cb8f1af6a362cee69dfbc502fd0ba238cd48
        @Operation(summary = "스터디 그룹 상세 조회", description = "스터디 그룹의 상세 정보와 멤버 목록을 조회합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = StudyGroupResponse.class))),
                        @ApiResponse(responseCode = "404", description = "스터디 그룹을 찾을 수 없음")
        })
        StudyGroupResponse getStudyGroupDetail(
                        @Parameter(description = "스터디 그룹 ID", required = true) Long groupId);
}
