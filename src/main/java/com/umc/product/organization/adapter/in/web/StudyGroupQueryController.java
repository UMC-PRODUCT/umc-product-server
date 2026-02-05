package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.response.CursorResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupListResponse.Summary;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupNameResponse;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupPartsResponse;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupResponse;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupSchoolsResponse;
import com.umc.product.organization.application.port.in.query.GetStudyGroupUseCase;
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
     * 스터디 그룹이 있는 학교 목록 조회 @deprecated
     */
    @Deprecated
    @Override
    @GetMapping("/schools")
    public StudyGroupSchoolsResponse getSchools() {
        return StudyGroupSchoolsResponse.from(getStudyGroupUseCase.getSchools());
    }

    /**
     * 특정 학교의 파트별 스터디 그룹 요약 조회 @deprecated
     */
    @Deprecated
    @Override
    @GetMapping("/schools/{schoolId}/parts")
    public StudyGroupPartsResponse getParts(@PathVariable Long schoolId) {
        return StudyGroupPartsResponse.from(getStudyGroupUseCase.getParts(schoolId));
    }

    /**
     * 내 스터디 그룹 목록 조회 - 유저의 schoolId/part 기반 자동 조회
     */
    @Override
    @GetMapping
    public CursorResponse<Summary> getStudyGroups(
            @CurrentMember MemberPrincipal memberPrincipal,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {

        List<StudyGroupInfo> content = getStudyGroupUseCase.getMyStudyGroups(
                memberPrincipal.getMemberId(), cursor, size
        );

        return CursorResponse.of(
                content,
                size,
                StudyGroupInfo::groupId,
                Summary::from
        );
    }

    /**
     * 스터디 그룹 이름 목록 조회 - 토글/드롭다운 용도
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
     * 스터디 그룹 상세 조회
     */
    @Override
    @GetMapping("/{groupId}")
    public StudyGroupResponse getStudyGroupDetail(@PathVariable Long groupId) {
        return StudyGroupResponse.from(getStudyGroupUseCase.getStudyGroupDetail(groupId));
    }
}
