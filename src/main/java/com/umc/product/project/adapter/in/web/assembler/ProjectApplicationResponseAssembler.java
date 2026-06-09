package com.umc.product.project.adapter.in.web.assembler;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.project.adapter.in.web.dto.common.MemberBrief;
import com.umc.product.project.adapter.in.web.dto.response.MyProjectApplicationResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectApplicantResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectApplicationDetailResponse;
import com.umc.product.project.application.port.in.query.GetMyProjectApplicationsUseCase;
import com.umc.product.project.application.port.in.query.GetMyRandomMatchedProjectMemberUseCase;
import com.umc.product.project.application.port.in.query.GetProjectApplicationDetailUseCase;
import com.umc.product.project.application.port.in.query.GetProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.query.GetProjectUseCase;
import com.umc.product.project.application.port.in.query.SearchProjectApplicationsUseCase;
import com.umc.product.project.application.port.in.query.dto.GetMyProjectApplicationsQuery;
import com.umc.product.project.application.port.in.query.dto.GetProjectApplicationDetailQuery;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationDetailInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationSummaryInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRoundInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMemberInfo;
import com.umc.product.project.application.port.in.query.dto.SearchProjectApplicationsQuery;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Project Application 관련 Response 조립기. Controller에서 여러 UseCase 를 조합하는 로직을 캡슐화한다.
 */
@Component
@RequiredArgsConstructor
public class ProjectApplicationResponseAssembler {

    private final GetMyProjectApplicationsUseCase getMyProjectApplicationsUseCase;
    private final GetMyRandomMatchedProjectMemberUseCase getMyRandomMatchedProjectMemberUseCase;
    private final SearchProjectApplicationsUseCase searchProjectApplicationsUseCase;
    private final GetProjectApplicationDetailUseCase getProjectApplicationDetailUseCase;
    private final GetProjectUseCase getProjectUseCase;
    private final GetProjectMatchingRoundUseCase getProjectMatchingRoundUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final GetChallengerUseCase getChallengerUseCase;

    /**
     * 본인 지원 내역 카드 목록 조립 (APPLY-004).
     * <p>
     * 두 데이터원을 한 화면에 합성한다:
     * <ul>
     *   <li>application 카드 -- 본인이 제출한 {@code ProjectApplication}</li>
     *   <li>랜덤 매칭 카드 -- 본인이 ACTIVE 멤버이면서 {@code application = null} 인 {@code ProjectMember} (한 기수 0 또는 1 건)</li>
     * </ul>
     * <p>
     * 정렬: application 카드는 service 가 반환한 순서(매칭 라운드 시작일 ASC -> 갱신일 DESC) 그대로, 랜덤 매칭 카드는 끝에 1 건 append.
     * <p>
     * status 필터가 명시된 호출에서는 랜덤 매칭 카드를 합성하지 않는다 -- application status 시맨틱이 적용되지 않는 데이터원이라 좁은 필터(SUBMITTED/REJECTED/DRAFT)
     * 호출 시 RANDOM_MATCHING 카드가 끼는 것을 방지한다.
     */
    public List<MyProjectApplicationResponse> myApplicationsFor(GetMyProjectApplicationsQuery query) {
        List<ProjectApplicationSummaryInfo> applications =
            getMyProjectApplicationsUseCase.listMyApplications(query);
        Optional<ProjectMemberInfo> randomMatched = (query.status() == null)
            ? getMyRandomMatchedProjectMemberUseCase.findMyRandomMatched(query.requesterMemberId(), query.gisuId())
            : Optional.empty();

        if (applications.isEmpty() && randomMatched.isEmpty()) {
            return List.of();
        }

        Set<Long> projectIds = new HashSet<>();
        Set<Long> roundIds = new HashSet<>();
        for (ProjectApplicationSummaryInfo application : applications) {
            projectIds.add(application.projectId());
            roundIds.add(application.matchingRoundId());
        }
        randomMatched.ifPresent(member -> projectIds.add(member.projectId()));

        Map<Long, ProjectInfo> projects = getProjectUseCase.listByIds(projectIds);
        Map<Long, ProjectMatchingRoundInfo> rounds = getProjectMatchingRoundUseCase.listByIds(roundIds);

        Set<Long> ownerIds = projects.values().stream()
            .map(ProjectInfo::productOwnerMemberId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, MemberInfo> memberMap = ownerIds.isEmpty()
            ? Map.of()
            : getMemberUseCase.findAllByIds(ownerIds);

        List<MyProjectApplicationResponse> result = new ArrayList<>(applications.size() + 1);
        for (ProjectApplicationSummaryInfo application : applications) {
            ProjectInfo project = projects.get(application.projectId());
            ProjectMatchingRoundInfo round = rounds.get(application.matchingRoundId());
            MemberBrief owner = ownerBriefOf(project, memberMap);
            result.add(MyProjectApplicationResponse.fromApplication(application, project, round, owner));
        }
        randomMatched.ifPresent(member -> {
            ProjectInfo project = projects.get(member.projectId());
            MemberBrief owner = ownerBriefOf(project, memberMap);
            result.add(MyProjectApplicationResponse.fromRandomMatched(member, project, owner));
        });
        return result;
    }

    /**
     * 단일 프로젝트의 지원자 목록 카드 조립 (APPLY-101).
     * <p>
     * Service 는 ProjectApplication 자원만 돌려준다. 화면 카드에 필요한 나머지 (지원자의 파트 / 매칭 라운드 / 지원자 닉네임/실명/학교) 는
     * 여기서 다른 도메인 UseCase 로 batch 조회해 붙인다.
     * <p>
     * 파트(part) 필터도 챌린저 도메인 정보라 in-memory 로 적용한다. 권한 scope 결정은 Service 가 이미 처리했으니 (None 이면 빈 리스트 반환)
     * 여기서는 그 결과를 그대로 신뢰한다.
     */
    public List<ProjectApplicantResponse> applicantsFor(SearchProjectApplicationsQuery query) {
        List<ProjectApplicationSummaryInfo> applications =
            searchProjectApplicationsUseCase.searchByProject(query);
        if (applications.isEmpty()) {
            return List.of();
        }

        // 챌린저 조회용 기수 ID 가 필요해서 프로젝트를 한 번 가져온다 (단건).
        ProjectInfo project = getProjectUseCase.getById(query.projectId());

        Set<Long> applicantMemberIds = applications.stream()
            .map(ProjectApplicationSummaryInfo::applicantMemberId)
            .collect(Collectors.toSet());
        Set<Long> roundIds = applications.stream()
            .map(ProjectApplicationSummaryInfo::matchingRoundId)
            .collect(Collectors.toSet());

        // 부가 정보 3종을 IN 쿼리 한 번씩으로 가져와 둔다 (N+1 방지).
        Map<Long, ChallengerPart> partsByMember = getChallengerUseCase
            .batchGetByMemberIdsAndGisuId(applicantMemberIds, project.gisuId())
            .entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().part()));
        Map<Long, ProjectMatchingRoundInfo> rounds =
            getProjectMatchingRoundUseCase.listByIds(roundIds);
        Map<Long, MemberInfo> memberMap = getMemberUseCase.findAllByIds(applicantMemberIds);

        List<ProjectApplicantResponse> result = new ArrayList<>(applications.size());
        for (ProjectApplicationSummaryInfo application : applications) {
            ChallengerPart applicantPart = partsByMember.get(application.applicantMemberId());
            // 파트 필터: 다른 파트는 건너뛴다.
            if (query.part() != null && query.part() != applicantPart) {
                continue;
            }
            ProjectMatchingRoundInfo round = rounds.get(application.matchingRoundId());
            MemberBrief brief = toBrief(memberMap.get(application.applicantMemberId()));
            result.add(ProjectApplicantResponse.from(application, applicantPart, round, brief));
        }
        return result;
    }

    /**
     * 지원서 단건 상세 조회. cross-domain 컨테이너에서 트리(섹션/질문/답변) 합성은 Response DTO 가 처리하고, Assembler 는 지원자(member) 닉네임/실명/학교만 합성한다.
     * <p>
     * 도메인 비즈니스 규칙(DRAFT 는 지원자 본인만 조회 가능)은 Service 단 ({@code getDetail}) 에서 처리되므로 Assembler 는 응답 조립에만 집중한다.
     * <p>
     * TODO: 4종 호출자(@CheckAccess) 자격 검증은 후속 권한 PR 에서 추가된다 -- 본 메서드는 호출 자격이 검증된 호출자에 한해 응답을 조립한다.
     */
    public ProjectApplicationDetailResponse detailFor(GetProjectApplicationDetailQuery query) {
        ProjectApplicationDetailInfo info = getProjectApplicationDetailUseCase.getDetail(query);

        MemberInfo memberInfo = getMemberUseCase.findAllByIds(Set.of(info.applicantMemberId()))
            .get(info.applicantMemberId());
        return ProjectApplicationDetailResponse.from(info, toBrief(memberInfo));
    }

    private MemberBrief ownerBriefOf(ProjectInfo project, Map<Long, MemberInfo> memberMap) {
        if (project == null || project.productOwnerMemberId() == null) {
            return null;
        }
        return toBrief(memberMap.get(project.productOwnerMemberId()));
    }

    private MemberBrief toBrief(MemberInfo info) {
        return info == null ? null : MemberBrief.from(info);
    }
}
