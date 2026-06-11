package com.umc.product.project.application.service.query;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.project.application.port.in.query.GetRandomMatchedProjectMemberUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectMemberInfo;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.domain.enums.MatchingType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ProjectMember 자원 조회 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectMemberQueryService implements GetRandomMatchedProjectMemberUseCase {

    private final LoadProjectMemberPort loadProjectMemberPort;

    // Cross-domain
    private final GetChallengerUseCase getChallengerUseCase;

    /**
     * 특정 챌린저의 랜덤 매칭/운영진 강제 배정 ProjectMember 단건 조회.
     * <p>
     * 대상 멤버의 챌린저 파트로부터 MatchingType 을 결정한 뒤, {@code application = null} + ACTIVE 인 ProjectMember 를 조회한다.
     * 도메인 정책상 (memberId, gisuId) 당 0 또는 1 건.
     * <p>
     * 대상 멤버가 해당 기수의 챌린저가 아니거나 매칭 대상 파트가 아닌 경우(PLAN/ADMIN) 빈 Optional 을 반환한다.
     */
    @Override
    public Optional<ProjectMemberInfo> findRandomMatched(Long memberId, Long gisuId) {
        return getChallengerUseCase
            .findByMemberIdAndGisuId(memberId, gisuId)
            .map(ChallengerInfo::part)
            .flatMap(MatchingType::fromPart)
            .flatMap(matchingType -> loadProjectMemberPort
                .findActiveWithoutApplicationByMemberIdAndGisuIdAndMatchingType(memberId, gisuId, matchingType))
            .map(ProjectMemberInfo::from);
    }
}
