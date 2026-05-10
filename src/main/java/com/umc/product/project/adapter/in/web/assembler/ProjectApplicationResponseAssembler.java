package com.umc.product.project.adapter.in.web.assembler;

import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.project.adapter.in.web.dto.common.MemberBrief;
import com.umc.product.project.adapter.in.web.dto.response.MyProjectApplicationResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectApplicantResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectApplicationDetailResponse;
import com.umc.product.project.application.port.in.query.GetMyProjectApplicationsUseCase;
import com.umc.product.project.application.port.in.query.GetProjectApplicationDetailUseCase;
import com.umc.product.project.application.port.in.query.SearchProjectApplicationsUseCase;
import com.umc.product.project.application.port.in.query.dto.GetMyProjectApplicationsQuery;
import com.umc.product.project.application.port.in.query.dto.GetProjectApplicationDetailQuery;
import com.umc.product.project.application.port.in.query.dto.MyProjectApplicationCardInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationCardInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationDetailInfo;
import com.umc.product.project.application.port.in.query.dto.SearchProjectApplicationsQuery;
import java.util.List;
import java.util.Map;
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
    private final SearchProjectApplicationsUseCase searchProjectApplicationsUseCase;
    private final GetProjectApplicationDetailUseCase getProjectApplicationDetailUseCase;
    private final GetMemberUseCase getMemberUseCase;

    /**
     * 본인 지원 내역 목록 조회. PM 닉네임/실명/학교는 member 도메인을 배치 조회해 합성한다.
     */
    public List<MyProjectApplicationResponse> myApplicationsFor(GetMyProjectApplicationsQuery query) {
        List<MyProjectApplicationCardInfo> cards = getMyProjectApplicationsUseCase.getMyApplications(query);
        if (cards.isEmpty()) {
            return List.of();
        }

        Set<Long> ownerIds = cards.stream()
            .map(MyProjectApplicationCardInfo::productOwnerMemberId)
            .collect(Collectors.toSet());
        Map<Long, MemberInfo> memberMap = getMemberUseCase.findAllByIds(ownerIds);

        return cards.stream()
            .map(card -> MyProjectApplicationResponse.from(card, toBrief(memberMap.get(card.productOwnerMemberId()))))
            .toList();
    }

    /**
     * PM/운영진용 단일 프로젝트 지원자 목록 조회. 지원자(챌린저) 의 닉네임/실명/학교는 member 도메인을 batch 조회해 합성한다.
     * <p>
     * TODO: 권한 검사 (@CheckAccess) 가 미적용 상태 -- 현재 일반 챌린저도 호출하면 다른 프로젝트의 지원자
     * 실명/학교가 노출될 수 있다. 운영 배포 전 반드시 권한 추가 필요.
     */
    public List<ProjectApplicantResponse> applicantsFor(SearchProjectApplicationsQuery query) {
        List<ProjectApplicationCardInfo> cards = searchProjectApplicationsUseCase.searchByProject(query);
        if (cards.isEmpty()) {
            return List.of();
        }

        Set<Long> memberIds = cards.stream()
            .map(ProjectApplicationCardInfo::applicantMemberId)
            .collect(Collectors.toSet());
        Map<Long, MemberInfo> memberMap = getMemberUseCase.findAllByIds(memberIds);

        return cards.stream()
            .map(card -> ProjectApplicantResponse.from(card, toBrief(memberMap.get(card.applicantMemberId()))))
            .toList();
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

    private MemberBrief toBrief(MemberInfo info) {
        return info == null ? null : MemberBrief.from(info);
    }
}
