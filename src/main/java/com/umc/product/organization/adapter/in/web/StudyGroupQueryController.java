package com.umc.product.organization.adapter.in.web;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.response.CursorResponse;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupListResponse.Summary;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupPartsResponse;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupResponse;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupSchoolsResponse;
import com.umc.product.organization.application.port.in.query.GetStudyGroupUseCase;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListInfo.StudyGroupInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListQuery;
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
     * 1단계: 스터디 그룹이 있는 학교 목록 조회
     */
    @Override
    @GetMapping("/schools")
    public StudyGroupSchoolsResponse getSchools() {
        return StudyGroupSchoolsResponse.from(getStudyGroupUseCase.getSchools());
    }

    /**
     * 2단계: 특정 학교의 파트별 스터디 그룹 요약 조회
     */
    @Override
    @GetMapping("/schools/{schoolId}/parts")
    public StudyGroupPartsResponse getParts(@PathVariable Long schoolId) {
        return StudyGroupPartsResponse.from(getStudyGroupUseCase.getParts(schoolId));
    }

    /**
     * 3단계: 스터디 그룹 목록 조회 (cursor 기반 페이지네이션)
     */
    @Override
    @GetMapping
    public CursorResponse<Summary> getStudyGroups(@RequestParam Long schoolId, @RequestParam ChallengerPart part,
                                                  @RequestParam(required = false) Long cursor,
                                                  @RequestParam(defaultValue = "20") int size) {

        StudyGroupListQuery query = new StudyGroupListQuery(schoolId, part, cursor, size);
        List<StudyGroupInfo> content = getStudyGroupUseCase.getStudyGroups(query);

        return CursorResponse.of(
                content,
                query.size(),
                info -> info.groupId(),
                Summary::from
        );
    }

    /**
     * 4단계: 스터디 그룹 상세 조회
     */
    @Override
    @GetMapping("/{groupId}")
    public StudyGroupResponse getStudyGroupDetail(@PathVariable Long groupId) {
        return StudyGroupResponse.from(getStudyGroupUseCase.getStudyGroupDetail(groupId));
    }
}
