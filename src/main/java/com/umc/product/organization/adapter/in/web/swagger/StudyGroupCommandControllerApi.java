package com.umc.product.organization.adapter.in.web.swagger;

import org.springframework.web.bind.annotation.PathVariable;

import com.umc.product.organization.adapter.in.web.dto.request.CreateStudyGroupRequest;
import com.umc.product.organization.adapter.in.web.dto.request.UpdateStudyGroupRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Organization | 스터디 그룹 Command", description = "")
public interface StudyGroupCommandControllerApi {

    @Operation(operationId = "STUDY-GROUP-001", summary = "스터디 그룹 생성", description = """
        스터디가 진행될 단일 part를 지정합니다. 인프라 스터디는 part=INFRA로 생성합니다.

        스터디원과 담당 파트장 ID는 모두 챌린저 ID가 아닌 회원 ID(memberId)입니다.
        스터디원은 해당 기수의 ACTIVE 챌린저이며 지정한 파트를 수강 중이어야 합니다.
        INFRA 스터디원은 infra=true인 챌린저여야 합니다.
        같은 기수의 동일 파트에서 다른 스터디에 중복 참여할 수 없습니다. 담당 파트장은 이 제한을 적용하지 않습니다.
        """)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "기수를 찾을 수 없음")
    })
    void create(CreateStudyGroupRequest request);

    @Operation(operationId = "STUDY-GROUP-002", summary = "스터디 그룹 수정", description = """
        스터디 이름과 part를 수정합니다. 전달하지 않은 필드는 변경되지 않습니다.

        파트 변경 시 같은 기수의 변경할 파트 스터디에 이미 속한 멤버가 있으면 409 에러가 발생합니다.
        스터디원 및 파트장 수정은 별도의 API 사용 바랍니다.
        """)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "스터디 그룹을 찾을 수 없음")
    })
    void update(
        @Parameter(description = "스터디 그룹 ID", required = true) Long studyGroupId,
        UpdateStudyGroupRequest request);

    @Operation(operationId = "STUDY-GROUP-003", summary = "스터디 그룹에 스터디원 추가", description = """
        회원 ID(memberId)로 스터디원을 추가합니다.
        해당 기수의 ACTIVE 챌린저와 수강 파트를 검사합니다.
        """)
    void addMember(
        @PathVariable Long studyGroupId,
        @PathVariable Long memberId
    );

    @Operation(operationId = "STUDY-GROUP-004", summary = "스터디 그룹에 담당 파트장 추가",
        description = "담당 파트장의 회원 ID(memberId)로 스터디 그룹에 파트장을 추가합니다.")
    void addMentor(
        @PathVariable Long studyGroupId,
        @PathVariable Long mentorId
    );

    @Operation(operationId = "STUDY-GROUP-005", summary = "스터디 그룹에 스터디원 제거", description = "스터디 그룹에서 스터디원을 제거합니다.")
    void deleteMember(
        @PathVariable Long studyGroupId,
        @PathVariable Long memberId
    );

    @Operation(operationId = "STUDY-GROUP-006", summary = "스터디 그룹에 담당 파트장 제거", description = "스터디 그룹에서 파트장을 제거합니다.")
    void deleteMentor(
        @PathVariable Long studyGroupId,
        @PathVariable Long mentorId
    );

    @Operation(operationId = "STUDY-GROUP-007", summary = "스터디 그룹 삭제", description = "스터디 그룹을 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "스터디 그룹을 찾을 수 없음")
    })
    void delete(@Parameter(description = "스터디 그룹 ID", required = true) Long studyGroupId);
}
