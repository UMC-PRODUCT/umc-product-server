package com.umc.product.organization.adapter.in.web;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.response.CursorResponse;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupListResponse.LeaderSummary;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupListResponse.StudyGroupSummary;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupPartsResponse;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupResponse;
import com.umc.product.organization.adapter.in.web.dto.response.StudyGroupSchoolsResponse;
import com.umc.product.organization.application.port.in.query.GetStudyGroupUseCase;
import com.umc.product.organization.application.port.in.query.GetStudyGroupUseCase.StudyGroupListQuery;
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
     * - 중앙 운영진, 중앙 파트장만 사용
     * - 회장/파트장은 본인 학교가 고정이므로 2단계 또는 3단계부터 시작
     */
    @Override
    @GetMapping("/schools")
    public StudyGroupSchoolsResponse getSchools(@RequestParam Long gisuId) {
        List<GetStudyGroupUseCase.SchoolWithStudyGroupCount> schools =
                getStudyGroupUseCase.getSchools(gisuId);

        List<StudyGroupSchoolsResponse.SchoolSummary> schoolSummaries = schools.stream()
                .map(s -> new StudyGroupSchoolsResponse.SchoolSummary(
                        s.schoolId(),
                        s.schoolName(),
                        s.logoImageUrl(),
                        s.totalStudyGroupCount(),
                        s.totalMemberCount()
                ))
                .toList();

        return new StudyGroupSchoolsResponse(schoolSummaries);
    }

    /**
     * 2단계: 특정 학교의 파트별 스터디 그룹 요약 조회
     * - 중앙 운영진, 회장: 모든 파트 조회 가능
     * - 중앙 파트장, 파트장: 본인 파트만 조회 가능 (ABAC 필터링)
     */
    @Override
    @GetMapping("/schools/{schoolId}/parts")
    public StudyGroupPartsResponse getParts(
            @PathVariable Long schoolId,
            @RequestParam Long gisuId) {

        GetStudyGroupUseCase.PartSummaryResult result =
                getStudyGroupUseCase.getParts(gisuId, schoolId);

        List<StudyGroupPartsResponse.PartSummary> partSummaries = result.parts().stream()
                .map(p -> new StudyGroupPartsResponse.PartSummary(
                        p.part().name(),
                        p.part().getDisplayName(),
                        p.studyGroupCount(),
                        p.memberCount()
                ))
                .toList();

        return new StudyGroupPartsResponse(
                result.schoolId(),
                result.schoolName(),
                partSummaries
        );
    }

    /**
     * 3단계: 스터디 그룹 목록 조회 (cursor 기반 페이지네이션)
     * - 모든 권한이 사용 (각자 본인 권한 범위 내에서)
     */
    @Override
    @GetMapping
    public CursorResponse<StudyGroupSummary> getStudyGroups(
            @RequestParam Long gisuId,
            @RequestParam Long schoolId,
            @RequestParam ChallengerPart part,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {

        StudyGroupListQuery query = new StudyGroupListQuery(gisuId, schoolId, part, cursor, size);
        GetStudyGroupUseCase.StudyGroupListResult result = getStudyGroupUseCase.getStudyGroups(query);

        List<StudyGroupSummary> summaries = result.studyGroups().stream()
                .map(g -> new StudyGroupSummary(
                        g.groupId(),
                        g.name(),
                        g.memberCount(),
                        g.leader() != null
                                ? new LeaderSummary(
                                g.leader().challengerId(),
                                g.leader().name(),
                                g.leader().profileImageUrl())
                                : null
                ))
                .toList();

        return CursorResponse.of(summaries, result.nextCursor(), result.hasNext());
    }

    /**
     * 4단계: 스터디 그룹 상세 조회
     * - 모든 권한이 사용 (본인 권한 범위 내 그룹만)
     */
    @Override
    @GetMapping("/{groupId}")
    public StudyGroupResponse getStudyGroupDetail(@PathVariable Long groupId) {
        GetStudyGroupUseCase.StudyGroupDetail detail = getStudyGroupUseCase.getStudyGroupDetail(groupId);

        StudyGroupResponse.LeaderInfo leaderInfo = detail.leader() != null
                ? new StudyGroupResponse.LeaderInfo(
                detail.leader().challengerId(),
                detail.leader().memberId(),
                detail.leader().name(),
                detail.leader().profileImageUrl())
                : null;

        List<StudyGroupResponse.MemberInfo> memberInfos = detail.members().stream()
                .map(m -> new StudyGroupResponse.MemberInfo(
                        m.challengerId(),
                        m.memberId(),
                        m.name(),
                        m.profileImageUrl()
                ))
                .toList();

        return new StudyGroupResponse(
                detail.groupId(),
                detail.name(),
                detail.part().name(),
                detail.part().getDisplayName(),
                detail.schoolId(),
                detail.schoolName(),
                detail.createdAt(),
                detail.memberCount(),
                leaderInfo,
                memberInfos
        );
    }
}
