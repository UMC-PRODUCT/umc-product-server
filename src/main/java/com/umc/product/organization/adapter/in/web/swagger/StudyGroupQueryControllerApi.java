package com.umc.product.organization.adapter.in.web.swagger;

import com.umc.product.global.response.CursorResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.organization.adapter.in.web.dto.response.studygroup.StudyGroupMemberResponse;
import com.umc.product.organization.adapter.in.web.dto.response.studygroup.StudyGroupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
@Tag(name = "Organization | 스터디 그룹 Query", description = "")
public interface StudyGroupQueryControllerApi {

    @Operation(summary = "내가 관리하는 스터디 그룹 목록 조회", description = "로그인한 유저의 학교/파트 기반으로 스터디 그룹 목록을 조회합니다. cursor 기반 무한스크롤.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CursorResponse<StudyGroupResponse> getStudyGroups(
        @Parameter(hidden = true) MemberPrincipal memberPrincipal,
        @Parameter(description = "페이지 커서 (첫 페이지는 null)") Long cursor,
        @Parameter(description = "페이지 크기 (기본 20, 최대 100)") int size);

    @Operation(summary = "스터디 그룹 스터디원 목록 조회",
        description = "스터디원 추가 화면에서 스터디 그룹 ID로 소속 스터디원(memberId, 학교명, 프로필 이미지 URL) 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = StudyGroupMemberResponse.class)))),
        @ApiResponse(responseCode = "404", description = "스터디 그룹을 찾을 수 없음")
    })
    StudyGroupResponse getStudyGroupMembers(
        @Parameter(description = "스터디 그룹 ID", required = true) Long groupId);
}
