package com.umc.product.organization.adapter.in.web;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.response.CursorResponse;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupListResponse.Summary;
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
 * 스터디 그룹 조회 API (단계별 drill-down)
 *
 * <h2>권한별 API 접근 흐름</h2>
 *
 * <h3>1. 중앙 운영진</h3>
 * 
 * <pre>
 * GET /schools?gisuId=9
 *   → 학교 목록 (서울대, 연세대, 고려대, ...)
 *   → 학교 선택
 *
 * GET /schools/{schoolId}/parts?gisuId=9
 *   → 파트 목록 (WEB, SERVER, iOS, ...)
 *   → 파트 선택
 *
 * GET /?schoolId=1&part=WEB&cursor=&size=20
 *   → 스터디 그룹 목록 (무한스크롤, 현재 활성화된 기수 기반)
 *   → 그룹 선택
 *
 * GET /{groupId}
 *   → 스터디 그룹 상세 + 멤버 목록
 * </pre>
 *
 * <h3>2. 중앙 파트장</h3>
 * 
 * <pre>
 * GET /schools?gisuId=9
 *   → 학교 목록
 *   → 학교 선택
 *
 * GET /schools/{schoolId}/parts?gisuId=9
 *   → 본인 파트만 반환 (ABAC 필터링)
 *   → 파트 선택 (자동)
 *
 * GET /?schoolId=1&part={본인파트}&cursor=&size=20
 *   → 스터디 그룹 목록 (무한스크롤, 현재 활성화된 기수 기반)
 *
 * GET /{groupId}
 *   → 스터디 그룹 상세
 * </pre>
 *
 * <h3>3. 회장</h3>
 * 
 * <pre>
 * GET /schools/{본인학교ID}/parts?gisuId=9
 *   → 파트 목록 (모든 파트)
 *   → 파트 선택
 *
 * GET /?schoolId={본인학교}&part=WEB&cursor=&size=20
 *   → 스터디 그룹 목록 (무한스크롤, 현재 활성화된 기수 기반)
 *
 * GET /{groupId}
 *   → 스터디 그룹 상세
 * </pre>
 *
 * <h3>4. 파트장</h3>
 * 
 * <pre>
 * GET /?schoolId={본인학교}&part={본인파트}&cursor=&size=20
 *   → 스터디 그룹 목록 (무한스크롤, 현재 활성화된 기수 기반)
 *
 * GET /{groupId}
 *   → 스터디 그룹 상세
 * </pre>
 */
@Tag(name = "StudyGroup Query", description = "스터디 그룹 조회 API (단계별 drill-down)")
public interface StudyGroupQueryControllerApi {

        @Operation(summary = "1단계: 학교 목록 조회", description = "스터디 그룹이 있는 학교 목록을 조회합니다. 중앙 운영진, 중앙 파트장용.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = StudyGroupSchoolsResponse.class)))
        })
        StudyGroupSchoolsResponse getSchools(
                        @Parameter(description = "기수 ID", required = true) Long gisuId);

        @Operation(summary = "2단계: 파트 목록 조회", description = "특정 학교의 파트별 스터디 그룹 요약을 조회합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = StudyGroupPartsResponse.class))),
                        @ApiResponse(responseCode = "404", description = "학교를 찾을 수 없음")
        })
        StudyGroupPartsResponse getParts(
                        @Parameter(description = "학교 ID", required = true) Long schoolId,
                        @Parameter(description = "기수 ID", required = true) Long gisuId);

        @Operation(summary = "3단계: 스터디 그룹 목록 조회", description = "특정 학교, 파트의 스터디 그룹 목록을 조회합니다. cursor 기반 무한스크롤. 현재 활성화된 기수 기반으로 조회합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "조회 성공")
        })
        CursorResponse<Summary> getStudyGroups(
                        @Parameter(description = "학교 ID", required = true) Long schoolId,
                        @Parameter(description = "파트", required = true) ChallengerPart part,
                        @Parameter(description = "페이지 커서 (첫 페이지는 null)") Long cursor,
                        @Parameter(description = "페이지 크기 (기본 20, 최대 100)") int size);

        @Operation(summary = "4단계: 스터디 그룹 상세 조회", description = "스터디 그룹의 상세 정보와 멤버 목록을 조회합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = StudyGroupResponse.class))),
                        @ApiResponse(responseCode = "404", description = "스터디 그룹을 찾을 수 없음")
        })
        StudyGroupResponse getStudyGroupDetail(
                        @Parameter(description = "스터디 그룹 ID", required = true) Long groupId);
}
