package com.umc.product.organization.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.response.CursorResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupMemberResponse;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupNameResponse;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupSummaryResponse;
import com.umc.product.organization.adapter.in.web.swagger.StudyGroupQueryControllerApi;
import com.umc.product.organization.application.port.in.query.GetStudyGroupUseCase;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListInfo.StudyGroupInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/study-groups")
@RequiredArgsConstructor
public class StudyGroupQueryController implements StudyGroupQueryControllerApi {

    private final GetStudyGroupUseCase getStudyGroupUseCase;

    /**
     * 내 스터디 그룹 목록 조회 - 유저의 schoolId/part 기반 자동 조회
     */
    @Override
    @GetMapping
    public CursorResponse<StudyGroupSummaryResponse> getStudyGroups(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "20") int size) {

        List<StudyGroupInfo> content = getStudyGroupUseCase.getMyStudyGroups(
            memberPrincipal.getMemberId(), cursor, size
        );

        return CursorResponse.of(
            content,
            size,
            StudyGroupListInfo.StudyGroupInfo::groupId,
            StudyGroupSummaryResponse::from
        );
    }

    /**
     * 권한에 따라 스터디 그룹 이름 목록 조회 - 토글/드롭다운 용도
     */
    @Override
    @GetMapping("/names")
    public StudyGroupNameResponse getStudyGroupNames(
        @CurrentMember MemberPrincipal memberPrincipal) {
        return StudyGroupNameResponse.from(
            getStudyGroupUseCase.getStudyGroupNames(memberPrincipal.getMemberId())
        );
    }

    /**
     * 스터디 그룹 스터디원 목록 조회
     */
    @CheckAccess(resourceType = ResourceType.STUDY_GROUP, permission = PermissionType.READ)
    @Override
    @GetMapping("/{groupId}/members")
    public List<StudyGroupMemberResponse> getStudyGroupMembers(@PathVariable Long groupId) {
        return getStudyGroupUseCase.getStudyGroupMembers(groupId).stream()
            .map(StudyGroupMemberResponse::from)
            .toList();
    }
}
