package com.umc.product.project.adapter.in.web.assembler;

import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.project.adapter.in.web.dto.common.MemberBrief;
import com.umc.product.project.adapter.in.web.dto.response.MyProjectApplicationResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectApplicantResponse;
import com.umc.product.project.application.port.in.query.GetMyProjectApplicationsUseCase;
import com.umc.product.project.application.port.in.query.SearchProjectApplicationsUseCase;
import com.umc.product.project.application.port.in.query.dto.GetMyProjectApplicationsQuery;
import com.umc.product.project.application.port.in.query.dto.MyProjectApplicationCardInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationCardInfo;
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

    private MemberBrief toBrief(MemberInfo info) {
        return info == null ? null : MemberBrief.from(info);
    }
}
