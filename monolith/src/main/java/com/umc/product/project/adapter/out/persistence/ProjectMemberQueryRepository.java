package com.umc.product.project.adapter.out.persistence;

import static com.umc.product.project.domain.QProjectMatchingRound.projectMatchingRound;
import static com.umc.product.project.domain.QProjectMember.projectMember;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.out.dto.ProjectMemberMatchedRoundInfo;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectMemberStatus;

import lombok.RequiredArgsConstructor;

/**
 * ProjectMember QueryDSL 기반 조회 구현.
 */
@Repository
@RequiredArgsConstructor
public class ProjectMemberQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 랜덤 매칭 시 중복 선발을 막기 위해 해당 기수의 ACTIVE·PENDING_PUBLICATION 멤버의 memberId를 조회한다.
     */
    public List<Long> listAssignedMemberIdsByGisuId(Long gisuId) {
        return listAssignedMemberIdsByGisuId(gisuId, null);
    }

    /**
     * 기수 내 ACTIVE·PENDING_PUBLICATION 팀원의 회원 ID를 중복 없이 조회한다.
     * 지정한 조정 차수에 해제 예약된 ACTIVE 팀원은 제외한다. 차수 ID가 null이면 기존 배정 인원을 모두 포함한다.
     */
    public List<Long> listAssignedMemberIdsByGisuId(Long gisuId, Long adjustmentRoundId) {
        return queryFactory.select(projectMember.memberId).distinct()
            .from(projectMember)
            .where(projectMember.project.gisuId.eq(gisuId), assignedMember(adjustmentRoundId))
            .fetch();
    }

    /** 파트와 관계없이 ACTIVE·PENDING_PUBLICATION 회원 ID를 프로젝트별로 중복 없이 조회한다. */
    public Map<Long, Set<Long>> listAssignedMemberIdsByProjectIds(Collection<Long> projectIds) {
        if (projectIds.isEmpty()) {
            return Map.of();
        }
        List<Tuple> rows = queryFactory.select(projectMember.project.id, projectMember.memberId).distinct()
            .from(projectMember)
            .where(projectMember.project.id.in(projectIds),
                projectMember.status.in(ProjectMemberStatus.ACTIVE, ProjectMemberStatus.PENDING_PUBLICATION))
            .fetch();

        Map<Long, Set<Long>> result = new HashMap<>();
        for (Tuple row : rows) {
            result.computeIfAbsent(row.get(projectMember.project.id), ignored -> new HashSet<>())
                .add(row.get(projectMember.memberId));
        }
        return result;
    }

    /**
     * ACTIVE 멤버의 assignedMatchingRound를 조회한다. PENDING_PUBLICATION 멤버와 차수가 null인 멤버는 제외한다.
     */
    public List<ProjectMemberMatchedRoundInfo> listMatchedRoundsByProjectIdsAndMemberIds(
        Collection<Long> projectIds, Collection<Long> memberIds
    ) {
        if (projectIds.isEmpty() || memberIds.isEmpty()) {
            return List.of();
        }
        return queryFactory.select(Projections.constructor(ProjectMemberMatchedRoundInfo.class,
                projectMember.project.id, projectMember.memberId, projectMatchingRound.id,
                projectMatchingRound.type, projectMatchingRound.phase))
            .from(projectMember)
            .join(projectMember.assignedMatchingRound, projectMatchingRound)
            .where(projectMember.project.id.in(projectIds), projectMember.memberId.in(memberIds),
                projectMember.status.eq(ProjectMemberStatus.ACTIVE))
            .fetch();
    }

    /**
     * 지원서 없이 랜덤 또는 수동 배정된 프로젝트 구성원을 조회할 때, 매칭 유형에 해당하는 파트 집합을 반환한다.
     * <p>
     * PLAN_DESIGN은 DESIGN, PLAN_DEVELOPER는 신규 WEB_PRODUCT_ENGINEER·MOBILE_PRODUCT_ENGINEER와
     * 과거 WEB·ANDROID·IOS·NODEJS·SPRINGBOOT를 포함한다.
     * 과거 기수의 배정 결과도 조회할 수 있도록 기존 개발 파트를 함께 반환한다.
     */
    private static Set<ChallengerPart> partsOf(MatchingType matchingType) {
        return switch (matchingType) {
            case PLAN_DESIGN -> EnumSet.of(ChallengerPart.DESIGN);
            case PLAN_DEVELOPER -> EnumSet.of(
                ChallengerPart.WEB,
                ChallengerPart.ANDROID,
                ChallengerPart.IOS,
                ChallengerPart.NODEJS,
                ChallengerPart.SPRINGBOOT,
                ChallengerPart.WEB_PRODUCT_ENGINEER,
                ChallengerPart.MOBILE_PRODUCT_ENGINEER
            );
        };
    }

    /**
     * 본인이 application=null + ACTIVE 인 ProjectMember 단건을 매칭 종류 기준 part 필터로 조회한다. APPLY-004 의 랜덤 매칭 카드 합성에 사용된다.
     * <p>
     * project 는 fetch join 으로 함께 로드된다. 도메인 정책상 결과는 0 또는 1 건이며, 2건 이상이면 QueryDSL {@code fetchOne()} 이
     * {@code NonUniqueResultException} 을 던져 데이터 정합성 위반이 표면화된다.
     */
    public Optional<ProjectMember> findActiveWithoutApplicationByMemberIdAndGisuIdAndMatchingType(
        Long memberId, Long gisuId, MatchingType matchingType
    ) {
        Set<ChallengerPart> partFilter = partsOf(matchingType);

        ProjectMember result = queryFactory
            .selectFrom(projectMember)
            .join(projectMember.project).fetchJoin()
            .where(
                projectMember.memberId.eq(memberId),
                projectMember.project.gisuId.eq(gisuId),
                projectMember.application.isNull(),
                projectMember.status.eq(ProjectMemberStatus.ACTIVE),
                projectMember.part.in(partFilter)
            )
            .fetchOne();

        return Optional.ofNullable(result);
    }

    /**
     * 여러 프로젝트의 ACTIVE 팀원에서 지정한 조정 차수의 해제 예약자를 제외하고, 해당 차수의 PENDING_PUBLICATION 팀원을 포함한다.
     * 조정 차수 목록이 비어 있으면 ACTIVE 팀원만 반환한다. 프로젝트와 선발 차수는 fetch join으로 함께 조회한다.
     */
    public List<ProjectMember> listAdjustmentMembersByProjectIds(Collection<Long> projectIds, Collection<Long> adjustmentRoundIds) {
        if (projectIds.isEmpty()) {
            return List.of();
        }
        BooleanExpression visible = projectMember.status.eq(ProjectMemberStatus.ACTIVE);
        if (!adjustmentRoundIds.isEmpty()) {
            visible = visible.and(projectMember.pendingRemovalRoundId.isNull()
                    .or(projectMember.pendingRemovalRoundId.notIn(adjustmentRoundIds)))
                .or(projectMember.status.eq(ProjectMemberStatus.PENDING_PUBLICATION)
                    .and(projectMember.assignedMatchingRound.id.in(adjustmentRoundIds)));
        }
        return queryFactory.selectFrom(projectMember)
            .join(projectMember.project).fetchJoin()
            .leftJoin(projectMember.assignedMatchingRound).fetchJoin()
            .where(projectMember.project.id.in(projectIds), visible)
            .orderBy(projectMember.createdAt.asc(), projectMember.id.asc()).fetch();
    }

    /**
     * 여러 프로젝트의 특정 파트 ACTIVE 멤버를 한 번에 조회해 projectId 기준으로 그룹핑한다.
     * <p>
     * {@code projectMember.project.id} projection 으로 FK 만 추출하여, 호출자가 {@code member.getProject().getId()} 로 lazy
     * 프록시에 접근하지 않도록 한다.
     *
     * @return projectId -> 멤버 목록 맵. 해당 파트 멤버가 없는 프로젝트는 엔트리 없음.
     */
    public Map<Long, List<ProjectMember>> listByProjectIdsAndPartGroupedByProjectId(
        Collection<Long> projectIds, ChallengerPart part
    ) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> rows = queryFactory
            .select(projectMember.project.id, projectMember)
            .from(projectMember)
            .where(
                projectMember.project.id.in(projectIds),
                projectMember.part.eq(part),
                projectMember.status.eq(ProjectMemberStatus.ACTIVE)
            )
            .orderBy(projectMember.createdAt.asc(), projectMember.id.asc())
            .fetch();

        Map<Long, List<ProjectMember>> result = new HashMap<>();
        for (Tuple row : rows) {
            Long projectId = row.get(projectMember.project.id);
            ProjectMember member = row.get(projectMember);
            result.computeIfAbsent(projectId, k -> new ArrayList<>()).add(member);
        }
        return result;
    }

    /**
     * 여러 프로젝트 중 특정 멤버가 ACTIVE PLAN 멤버인 프로젝트 ID 를 조회한다.
     */
    public List<Long> listProjectIdsByActivePlanMember(Collection<Long> projectIds, Long memberId) {
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
            .select(projectMember.project.id)
            .distinct()
            .from(projectMember)
            .where(
                projectMember.project.id.in(projectIds),
                projectMember.memberId.eq(memberId),
                projectMember.part.eq(ChallengerPart.PLAN),
                projectMember.status.eq(ProjectMemberStatus.ACTIVE)
            )
            .fetch();
    }

    /**
     * 여러 프로젝트의 (projectId, part) 별 ACTIVE 멤버 수를 한 번에 집계한다.
     *
     * @return projectId -> (part -> 인원수) 맵. 인원이 0 인 (project, part) 조합은 엔트리 없음.
     */
    public Map<Long, Map<ChallengerPart, Long>> countByProjectIdsGroupByProjectIdAndPart(
        Collection<Long> projectIds
    ) {
        return countMembers(projectIds, List.of(ProjectMemberStatus.ACTIVE));
    }

    /**
     * 수동 배정 시 TO 검증을 위해 프로젝트의 파트별 ACTIVE·PENDING_PUBLICATION 팀원 수를 집계한다.
     * 지정한 조정 차수에 해제 예약된 ACTIVE 팀원은 제외한다. 차수 ID가 null이면 해제 예약자를 포함한다.
     */
    public Map<ChallengerPart, Long> countAssignedByProjectIdGroupByPart(Long projectId, Long adjustmentRoundId) {
        List<Tuple> rows = queryFactory.select(projectMember.part, projectMember.count()).from(projectMember)
            .where(projectMember.project.id.eq(projectId), assignedMember(adjustmentRoundId))
            .groupBy(projectMember.part).fetch();
        Map<ChallengerPart, Long> counts = new EnumMap<>(ChallengerPart.class);
        rows.forEach(row -> counts.put(row.get(projectMember.part), row.get(projectMember.count())));
        return counts;
    }

    public Map<Long, Map<ChallengerPart, Long>> countAssignedByProjectIdsGroupByProjectIdAndPart(Collection<Long> projectIds) {
        return countMembers(projectIds, List.of(ProjectMemberStatus.ACTIVE, ProjectMemberStatus.PENDING_PUBLICATION));
    }

    /**
     * ACTIVE·PENDING_PUBLICATION 팀원을 포함하고, 지정한 조정 차수에 해제 예약된 ACTIVE 팀원은 제외한다.
     */
    private BooleanExpression assignedMember(Long adjustmentRoundId) {
        BooleanExpression active = projectMember.status.eq(ProjectMemberStatus.ACTIVE);
        if (adjustmentRoundId != null) {
            active = active.and(projectMember.pendingRemovalRoundId.isNull()
                .or(projectMember.pendingRemovalRoundId.ne(adjustmentRoundId)));
        }
        return active.or(projectMember.status.eq(ProjectMemberStatus.PENDING_PUBLICATION));
    }

    private Map<Long, Map<ChallengerPart, Long>> countMembers(Collection<Long> projectIds,
        Collection<ProjectMemberStatus> statuses) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> rows = queryFactory
            .select(projectMember.project.id, projectMember.part, projectMember.count())
            .from(projectMember)
            .where(
                projectMember.project.id.in(projectIds),
                projectMember.status.in(statuses)
            )
            .groupBy(projectMember.project.id, projectMember.part)
            .fetch();

        Map<Long, Map<ChallengerPart, Long>> result = new HashMap<>();
        for (Tuple row : rows) {
            Long projectId = row.get(projectMember.project.id);
            ChallengerPart part = row.get(projectMember.part);
            Long count = row.get(projectMember.count());
            result
                .computeIfAbsent(projectId, k -> new EnumMap<>(ChallengerPart.class))
                .put(part, count);
        }
        return result;
    }

}
