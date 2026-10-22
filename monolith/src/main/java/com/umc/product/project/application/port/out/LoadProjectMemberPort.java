package com.umc.product.project.application.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.out.dto.ProjectMemberMatchedRoundInfo;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.enums.MatchingType;

/**
 * ProjectMember 조회 Port (Driven / Port Out).
 */
public interface LoadProjectMemberPort {

    /**
     * 특정 프로젝트의 활성(ACTIVE) 멤버 전체를 조회합니다. PROJECT-003(팀원 조회)에서 사용.
     */
    List<ProjectMember> listByProjectId(Long projectId);

    /**
     * 특정 프로젝트의 ACTIVE 및 PENDING_PUBLICATION 멤버를 조회합니다.
     */
    List<ProjectMember> listActiveAndPendingPublicationByProjectId(Long projectId);

    /**
     * 해당 차수의 PENDING_PUBLICATION 멤버와 소속 프로젝트를 잠금 후 조회합니다.
     */
    List<ProjectMember> listPendingByMatchingRoundId(Long matchingRoundId);

    /**
     * 중복 선발을 막기 위해 해당 기수의 ACTIVE·PENDING_PUBLICATION 멤버의 memberId를 조회합니다.
     */
    List<Long> listAssignedMemberIdsByGisuId(Long gisuId);

    /**
     * ACTIVE 멤버의 assignedMatchingRound를 조회합니다. 차수가 null인 멤버는 제외합니다.
     */
    List<ProjectMemberMatchedRoundInfo> listMatchedRoundsByProjectIdsAndMemberIds(
        Collection<Long> projectIds, Collection<Long> memberIds);

    /**
     * 특정 프로젝트의 파트별 ACTIVE 및 PENDING_PUBLICATION 멤버 수를 집계합니다.
     * 내부 정원 검증에 사용합니다.
     *
     * @return 파트 → 멤버 수 맵
     */
    Map<ChallengerPart, Long> countAssignedByProjectIdGroupByPart(Long projectId);

    /**
     * 여러 프로젝트의 파트별 ACTIVE 및 PENDING_PUBLICATION 멤버 수를 한 번에 집계합니다.
     * 집계 대상은 {@link #countAssignedByProjectIdGroupByPart(Long)} 와 같습니다.
     *
     * @return projectId → (파트 → 멤버 수) 맵
     */
    Map<Long, Map<ChallengerPart, Long>> countAssignedByProjectIdsGroupByProjectIdAndPart(Collection<Long> projectIds);

    /**
     * 여러 프로젝트의 활성(ACTIVE) 멤버를 한 번에 조회합니다 (N+1 방지용 IN 쿼리). PROJECT-004(팀원 일괄 조회)에서 사용.
     *
     * @return projectId → 활성 멤버 목록 맵. 활성 멤버가 없는 프로젝트는 엔트리 없음.
     */
    Map<Long, List<ProjectMember>> listByProjectIds(Collection<Long> projectIds);

    /**
     * 특정 프로젝트의 특정 파트에 속한 ACTIVE 멤버 목록을 조회합니다. PLAN 파트 조회 시 보조 PM(coPM) 추출에 사용됩니다.
     */
    List<ProjectMember> listByProjectIdAndPart(Long projectId, ChallengerPart part);

    /**
     * 여러 프로젝트의 특정 파트 ACTIVE 멤버를 한 번에 조회해 projectId 기준 Map 으로 반환합니다 (N+1 방지).
     *
     * @return projectId -> 멤버 목록 맵. 해당 파트 멤버가 없는 프로젝트는 엔트리 없음.
     */
    Map<Long, List<ProjectMember>> listByProjectIdsAndPartGroupedByProjectId(
        Collection<Long> projectIds, ChallengerPart part);

    /**
     * 특정 프로젝트의 파트별 활성 멤버 수를 집계합니다. {@code PartQuotaInfo.currentCount} 계산에 사용됩니다.
     *
     * @return 파트 → 인원수 맵. 활성 멤버가 없는 파트는 0 또는 엔트리 없음
     */
    Map<ChallengerPart, Long> countByProjectIdGroupByPart(Long projectId);

    /**
     * 여러 프로젝트의 (projectId, part) 별 활성 멤버 수를 한 번에 집계합니다 (N+1 방지용 IN 쿼리).
     *
     * @return projectId -> (part -> 인원수) 맵. 인원이 0 인 (project, part) 조합은 엔트리 없음.
     */
    Map<Long, Map<ChallengerPart, Long>> countByProjectIdsGroupByProjectIdAndPart(Collection<Long> projectIds);

    /**
     * 특정 프로젝트의 특정 멤버를 status 무관 단건 조회합니다. PROJECT-005(멤버 제거)에서 사용 — 이미 비활성화된 row 도 idempotent 처리하기 위함.
     */
    Optional<ProjectMember> findByProjectIdAndMemberId(Long projectId, Long memberId);

    /**
     * 특정 프로젝트의 특정 멤버를 status 무관 단건 조회합니다. 존재하지 않으면 도메인 예외를 던집니다.
     */
    ProjectMember getByProjectIdAndMemberId(Long projectId, Long memberId);

    /**
     * 해당 기수에 ACTIVE 또는 PENDING_PUBLICATION 멤버가 존재하는지 확인합니다. (중복 지원 방지용)
     */
    boolean existsByGisuAndMember(Long gisuId, Long memberId);

    /**
     * 특정 프로젝트의 ACTIVE PLAN 파트 멤버인지 확인합니다. 보조 PM(Sub-PM) 검사에 사용됩니다.
     */
    boolean isActivePlanMember(Long projectId, Long memberId);

    /**
     * 여러 프로젝트 중 요청자가 ACTIVE PLAN 멤버인 프로젝트 ID 를 조회합니다. PM/운영진 지원자 목록 batch 권한 판정에서 Sub-PM 여부를 IN 쿼리로 확인할 때
     * 사용합니다.
     */
    List<Long> listProjectIdsByActivePlanMember(Collection<Long> projectIds, Long memberId);

    /**
     * 본인이 ACTIVE 멤버이면서 application 이 null 인 (즉, 지원서 경로가 아닌 랜덤 매칭/운영진 강제 배정으로 합류한) 멤버를 단건 조회한다. APPLY-004(본인 지원 내역 목록
     * 조회) 의 랜덤 매칭 카드 합성에 사용된다.
     * <p>
     * 도메인 정책: 한 챌린저는 한 기수에 한 프로젝트에만 합류 가능하므로 결과는 최대 1건이다. 2건 이상 발견되면 호출자(Service)가 invariant 위반 처리한다.
     * <p>
     * 필터:
     * <ul>
     *   <li>{@code memberId} -- 본인 멤버 ID</li>
     *   <li>{@code gisuId} -- {@code project.gisuId} 일치 (해당 기수)</li>
     *   <li>{@code matchingType} -- 본인 챌린저 파트로부터 추론된 매칭 종류로 part 집합 필터.
     *       PLAN_DEVELOPER -> WEB/ANDROID/IOS/NODEJS/SPRINGBOOT, PLAN_DESIGN -> DESIGN.
     *       동일 멤버의 다른 파트 노이즈를 차단하는 안전망이다.</li>
     *   <li>{@code application = null}</li>
     *   <li>{@code status = ACTIVE} (DISMISSED 제외)</li>
     * </ul>
     * project 는 fetch join 으로 함께 로드된다 -- 호출자가 lazy 프록시 traversal 없이 {@code member.getProject()} 를 사용할 수 있게 한다.
     */
    Optional<ProjectMember> findActiveWithoutApplicationByMemberIdAndGisuIdAndMatchingType(
        Long memberId,
        Long gisuId,
        MatchingType matchingType
    );
}
