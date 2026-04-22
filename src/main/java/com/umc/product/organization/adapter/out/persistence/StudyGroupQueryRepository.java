package com.umc.product.organization.adapter.out.persistence;

import static com.umc.product.member.domain.QMember.member;
import static com.umc.product.organization.domain.QStudyGroup.studyGroup;
import static com.umc.product.organization.domain.QStudyGroupMember.studyGroupMember;
import static com.umc.product.organization.domain.QStudyGroupOrganizer.studyGroupOrganizer;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupNameInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupViewScope;
import com.umc.product.organization.domain.QStudyGroupMember;
import com.umc.product.organization.domain.QStudyGroupOrganizer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudyGroupQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 3단계: 스터디 그룹 목록 조회 (활성 기수 기준)
     * - 해당 학교 멤버가 포함된 스터디 그룹만 조회
     * - 페이지네이션은 Controller에서 CursorResponse.of()로 처리
     *
     */
    public List<StudyGroupListInfo.StudyGroupInfo> findStudyGroups(Long schoolId, ChallengerPart part,
            Long cursor, int size) {
        return null;
//        // 해당 학교 멤버가 포함된 스터디 그룹 ID 조회
//        List<Long> studyGroupIdsWithSchool = queryFactory
//                .selectDistinct(studyGroup.id)
//                .from(studyGroupMember)
//                .join(studyGroupMember.studyGroup, studyGroup)
//                .join(challenger).on(challenger.id.eq(studyGroupMember.challengerId))
//                .join(member).on(member.id.eq(challenger.memberId))
//                .where(
//                        studyGroup.gisu.isActive.eq(true),
//                        partCondition(part),
//                        member.schoolId.eq(schoolId)
//                )
//                .fetch();
//
//        if (studyGroupIdsWithSchool.isEmpty()) {
//            return List.of();
//        }
//
//        // 스터디 그룹 기본 정보 조회 (Tuple 사용)
//        List<Tuple> groups = queryFactory
//                .select(studyGroup.id, studyGroup.name)
//                .from(studyGroup)
//                .where(
//                        studyGroup.id.in(studyGroupIdsWithSchool),
//                        cursorCondition(cursor)
//                )
//                .orderBy(studyGroup.id.asc())
//                .limit(size)
//                .fetch();
//
//        // 스터디 그룹 ID 목록
//        List<Long> groupIds = groups.stream().map(t -> t.get(studyGroup.id)).toList();
//
//        if (groupIds.isEmpty()) {
//            return List.of();
//        }
//
//        // 멤버 정보 조회 (리더 + 일반 멤버 )
//        List<Tuple> memberResults = queryFactory
//                .select(
//                        studyGroupMember.studyGroup.id,
//                        challenger.id,
//                        member.name,
//                        member.profileImageId.stringValue(),
//                        studyGroupMember.isLeader
//                )
//                .from(studyGroupMember)
//                .join(challenger).on(challenger.id.eq(studyGroupMember.challengerId))
//                .join(member).on(member.id.eq(challenger.memberId))
//                .where(studyGroupMember.studyGroup.id.in(groupIds))
//                .fetch();
//
//        // 그룹별 멤버 맵 생성
//        Map<Long, List<Tuple>> membersByGroup = memberResults.stream()
//                .collect(Collectors.groupingBy(t -> t.get(studyGroupMember.studyGroup.id)));
//
//        // StudyGroupInfo 변환
//        return groups.stream()
//                .map(g -> {
//                    Long groupId = g.get(studyGroup.id);
//                    String groupName = g.get(studyGroup.name);
//                    List<Tuple> members = membersByGroup.getOrDefault(groupId, List.of());
//
//                    StudyGroupListInfo.StudyGroupInfo.LeaderInfo leader = members.stream()
//                            .filter(m -> Boolean.TRUE.equals(m.get(studyGroupMember.isLeader)))
//                            .findFirst()
//                            .map(m -> new StudyGroupListInfo.StudyGroupInfo.LeaderInfo(
//                                    m.get(challenger.id),
//                                    m.get(member.name),
//                                    m.get(member.profileImageId.stringValue())))
//                            .orElse(null);
//
//                    List<StudyGroupListInfo.StudyGroupInfo.MemberSummaryInfo> memberSummaries = members.stream()
//                            .filter(m -> !Boolean.TRUE.equals(m.get(studyGroupMember.isLeader)))
//                            .map(m -> new StudyGroupListInfo.StudyGroupInfo.MemberSummaryInfo(
//                                    m.get(challenger.id),
//                                    m.get(member.name),
//                                    m.get(member.profileImageId.stringValue())))
//                            .toList();
//
//                    return new StudyGroupListInfo.StudyGroupInfo(
//                            groupId,
//                            groupName,
//                            members.size(),
//                            leader,
//                            memberSummaries
//                    );
//                })
//                .toList();
    }

    /**
     * 4단계: 스터디 그룹 상세 조회
     * - 멤버들의 학교 목록을 조회 (여러 학교 가능)
     */
    public StudyGroupDetailInfo findStudyGroupDetail(Long groupId) {
        return null;
//        // 스터디 그룹 기본 정보 조회
//        Tuple groupInfo = queryFactory
//                .select(
//                        studyGroup.id,
//                        studyGroup.name,
//                        studyGroup.part,
//                        studyGroup.createdAt
//                )
//                .from(studyGroup)
//                .where(studyGroup.id.eq(groupId))
//                .fetchOne();
//
//        if (groupInfo == null) {
//            return null;
//        }
//
//        // 멤버/리더/학교 정보를 한 번에 조회한 뒤 분리
//        List<Tuple> memberRows = queryFactory
//                .select(
//                        challenger.id,
//                        member.id,
//                        member.name,
//                        member.profileImageId.stringValue(),
//                        studyGroupMember.isLeader,
//                        school.id,
//                        school.name
//                )
//                .from(studyGroupMember)
//                .join(challenger).on(challenger.id.eq(studyGroupMember.challengerId))
//                .join(member).on(member.id.eq(challenger.memberId))
//                .join(school).on(school.id.eq(member.schoolId))
//                .where(studyGroupMember.studyGroup.id.eq(groupId))
//                .fetch();
//
//        StudyGroupDetailInfo.MemberInfo leaderResult = null;
//        List<StudyGroupDetailInfo.MemberInfo> allMembers = new ArrayList<>(memberRows.size());
//        List<StudyGroupDetailInfo.MemberInfo> nonLeaderMembers = new ArrayList<>();
//        Map<Long, StudyGroupDetailInfo.SchoolInfo> schoolMap = new LinkedHashMap<>();
//
//        for (Tuple row : memberRows) {
//            StudyGroupDetailInfo.MemberInfo memberInfo = new StudyGroupDetailInfo.MemberInfo(
//                    row.get(challenger.id),
//                    row.get(member.id),
//                    row.get(member.name),
//                    row.get(member.profileImageId.stringValue())
//            );
//            allMembers.add(memberInfo);
//
//            Boolean isLeader = row.get(studyGroupMember.isLeader);
//            if (Boolean.TRUE.equals(isLeader)) {
//                leaderResult = memberInfo;
//            } else {
//                nonLeaderMembers.add(memberInfo);
//            }
//
//            Long schoolId = row.get(school.id);
//            if (schoolId != null) {
//                schoolMap.putIfAbsent(schoolId, new StudyGroupDetailInfo.SchoolInfo(
//                        schoolId,
//                        row.get(school.name)
//                ));
//            }
//        }
//
//        List<StudyGroupDetailInfo.SchoolInfo> schools = new ArrayList<>(schoolMap.values());
//
//        return new StudyGroupDetailInfo(
//                groupInfo.get(studyGroup.id),
//                groupInfo.get(studyGroup.name),
//                groupInfo.get(studyGroup.part),
//                schools,
//                groupInfo.get(studyGroup.createdAt).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
//                allMembers.size(),
//                leaderResult,
//                nonLeaderMembers
//        );
    }

    /**
     * 스터디 그룹 이름 목록 조회 (활성 기수 기준, 학교/파트 필터)
     * - 해당 학교 멤버가 포함된 스터디 그룹의 ID/이름만 반환
     */
    public List<StudyGroupNameInfo> findStudyGroupNames(Long schoolId, ChallengerPart part) {
        return null;
//        List<Long> studyGroupIds = queryFactory
//                .selectDistinct(studyGroup.id)
//                .from(studyGroupMember)
//                .join(studyGroupMember.studyGroup, studyGroup)
//                .join(challenger).on(challenger.id.eq(studyGroupMember.challengerId))
//                .join(member).on(member.id.eq(challenger.memberId))
//                .where(
//                        studyGroup.gisu.isActive.eq(true),
//                        member.schoolId.eq(schoolId),
//                        partCondition(part)
//                )
//                .fetch();
//
//        if (studyGroupIds.isEmpty()) {
//            return List.of();
//        }
//
//        return queryFactory
//                .select(Projections.constructor(StudyGroupNameInfo.class,
//                        studyGroup.id,
//                        studyGroup.name
//                ))
//                .from(studyGroup)
//                .where(studyGroup.id.in(studyGroupIds))
//                .orderBy(studyGroup.name.asc())
//                .fetch();
    }


    /**
     * 내 스터디 그룹 목록 조회 (역할 Scope 기반).
     * <p>
     * 이 함수는 다섯 단계로 구성되어 있다. 본문은 단계만 호출해 "무엇을 하는지" 를 드러내고,
     * 세부 SQL 처리는 각 단계의 private 헬퍼에 격리했다.
     * <ol>
     *   <li>{@link #buildScopePredicate(List)} — 역할 Scope들을 OR로 합쳐 하나의 WHERE 조건으로 변환</li>
     *   <li>{@link #fetchGroupHeaders(Long, BooleanExpression, Long, int)} — 상위 그룹 헤더(id/name)만 커서 페이징</li>
     *   <li>{@link #fetchOrganizersByGroupIds(List)} — 상위 그룹들의 운영진(Organizer) 행을 한 번에 조회하여 groupId 기준으로 분배</li>
     *   <li>{@link #fetchMembersByGroupIds(List)} — 상위 그룹들의 멤버 행을 한 번에 조회하여 groupId 기준으로 분배</li>
     *   <li>헤더 + 운영진 + 멤버를 조합하여 DTO 변환</li>
     * </ol>
     * Organizer와 Member를 각각 별도 bulk 쿼리로 가져오는 이유: 둘은 <b>다른 테이블</b>
     * ({@code study_group_organizer}, {@code study_group_member})에 있고 독립된 엔티티다.
     * UNION 하면 행 구조/컬럼 타입이 섞여 오히려 조립 로직이 꼬이기 때문에,
     * 각자 projection → 자바 단에서 groupingBy 하는 편이 단순하다.
     *
     * @param scopes 역할 기반 조회 범위. Service에서 사용자 역할로 조립해 넘긴다.
     * @param gisuId 활성 기수 ID (이 기수의 스터디 그룹만 대상)
     * @param cursor 직전 페이지 마지막 groupId. 첫 페이지는 null.
     * @param size   조회 사이즈 (Service가 hasNext 판단용으로 +1 포함하여 넘김)
     * @return 상위 스터디 그룹 정보 + 각 그룹의 운영진/멤버 요약.
     *         scope가 전부 비어있거나 조회 결과가 없으면 빈 리스트.
     */
    public List<StudyGroupListInfo.StudyGroupInfo> findMyStudyGroups(
        List<StudyGroupViewScope> scopes, Long gisuId, Long cursor, int size) {

        // 1) 역할 Scope들을 WHERE 조건 한 덩어리로 합친다. 빈 predicate면 즉시 종료(풀 스캔 방지).
        BooleanExpression scopePredicate = buildScopePredicate(scopes);
        if (scopePredicate == null) {
            return List.of();
        }

        // 2) 상위 그룹 헤더만 커서 페이징으로 자른다. 여기서 size 의미가 지켜진다.
        List<GroupHeaderRow> groupHeaders = fetchGroupHeaders(gisuId, scopePredicate, cursor, size);
        if (groupHeaders.isEmpty()) {
            return List.of();
        }

        List<Long> groupIds = extractGroupIds(groupHeaders);

        // 3) 잘라낸 그룹들의 운영진을 조회.
        Map<Long, List<OrganizerRow>> organizersByGroup = fetchOrganizersByGroupIds(groupIds);

        // 4) 잘라낸 그룹들의 멤버를 조회.
        Map<Long, List<MemberRow>> membersByGroup = fetchMembersByGroupIds(groupIds);

        // 5) 헤더 + 운영진 + 멤버를 조합해 DTO로 변환한다.
        return groupHeaders.stream()
            .map(header -> toStudyGroupInfo(header, organizersByGroup, membersByGroup))
            .toList();
    }

    /**
     * 역할 Scope 리스트를 하나의 {@link BooleanExpression} 으로 합친다 (OR 결합).
     * <p>
     * 각 Scope는 {@link #toScopePredicate(StudyGroupViewScope)} 로 EXISTS 서브쿼리로 변환되는데,
     * Scope의 입력값이 비어있는 경우(예: schoolMemberIds가 비어있는 AsSchoolCore)에는 null이 반환된다.
     * 여기서는 그런 null Scope를 먼저 걸러낸 뒤 {@code reduce(BooleanExpression::or)} 로 OR 결합한다.
     * <p>
     * 최종 결과가 null 이면 "조회할 Scope가 하나도 없다" 는 뜻이므로 호출 측에서 빈 리스트를 반환해야 한다.
     * 그렇지 않고 빈 WHERE 로 쿼리를 돌리면 테이블 풀 스캔이 된다.
     */
    private BooleanExpression buildScopePredicate(List<StudyGroupViewScope> scopes) {
        return scopes.stream()
            .map(this::toScopePredicate)
            .filter(Objects::nonNull)
            .reduce(BooleanExpression::or)
            .orElse(null);
    }

    /**
     * 상위 스터디 그룹의 헤더 정보(id, name)만 커서 페이징으로 가져온다.
     * <p>
     * 멤버 JOIN을 하지 않는 이유: 멤버가 포함되면 한 그룹이 멤버 수만큼 중복 row로 나와
     * {@code LIMIT size} 가 "멤버 row 기준 size" 로 해석되어 페이징 의미가 붕괴된다.
     * 따라서 여기서는 그룹 단위로 size 를 정확히 자르고, 멤버는 다음 단계에서 별도 쿼리로 채운다.
     * <p>
     * 결과는 {@link GroupHeaderRow} record로 바로 Projection
     *
     * @param gisuId         활성 기수 필터
     * @param scopePredicate {@link #buildScopePredicate(List)} 가 만든 역할 Scope WHERE 조건 (null 아님)
     * @param cursor         커서 (null 이면 첫 페이지)
     * @param size           조회 크기
     * @return (groupId, name) 헤더 리스트, id DESC 정렬
     */
    private List<GroupHeaderRow> fetchGroupHeaders(Long gisuId, BooleanExpression scopePredicate, Long cursor, int size) {
        return queryFactory
            .select(Projections.constructor(GroupHeaderRow.class,
                studyGroup.id,
                studyGroup.name))
            .from(studyGroup)
            .where(
                studyGroup.gisuId.eq(gisuId),
                scopePredicate,
                descCursorCondition(cursor)
            )
            .orderBy(studyGroup.id.desc())
            .limit(size)
            .fetch();
    }

    /**
     * 헤더 리스트에서 groupId만 뽑아낸다. 다음 단계(멤버 조회)의 IN 절 입력으로 쓰인다.
     */
    private List<Long> extractGroupIds(List<GroupHeaderRow> headers) {
        return headers.stream()
            .map(GroupHeaderRow::groupId)
            .toList();
    }

    /**
     * 주어진 groupIds에 속한 모든 <b>운영진(Organizer)</b> 행을 한 번의 쿼리로 가져와
     * {@code groupId → organizers} 맵으로 재분배한다.
     * <p>
     * 운영진은 {@code study_group_organizer} 테이블에 멤버와는 별도로 관리되는 엔티티다.
     * 따라서 {@code StudyGroupMember.isLeader} 플래그와는 별개로, 이 테이블을 직접 JOIN 해
     * 운영진 목록을 만든다.
     * <p>
     * N+1 방지를 위해 IN 절로 한 번에 묶고 자바 단에서 {@code groupingBy} 로 분배한다.
     *
     * @param groupIds 운영진을 채울 대상 그룹 ID 목록 (비어있지 않다고 가정)
     * @return {groupId → 해당 그룹 운영진 리스트}. 운영진이 없는 그룹은 맵에 키가 없다.
     */
    private Map<Long, List<OrganizerRow>> fetchOrganizersByGroupIds(List<Long> groupIds) {
        List<OrganizerRow> rows = queryFactory
            .select(Projections.constructor(OrganizerRow.class,
                studyGroupOrganizer.studyGroup.id,
                studyGroupOrganizer.memberId,
                member.name,
                member.profileImageId))
            .from(studyGroupOrganizer)
            .join(member).on(member.id.eq(studyGroupOrganizer.memberId))
            .where(studyGroupOrganizer.studyGroup.id.in(groupIds))
            .fetch();

        return rows.stream()
            .collect(Collectors.groupingBy(OrganizerRow::groupId));
    }

    /**
     * 주어진 groupIds에 속한 모든 <b>멤버</b> 행을 한 번의 쿼리로 가져와
     * {@code groupId → members} 맵으로 재분배한다.
     * <p>
     * 여기서의 "멤버"는 {@code study_group_member} 테이블의 전체 소속 멤버이며,
     * 운영진 여부와 관계없이 그룹에 속한 모든 사람을 포함한다. 운영진은 별도
     * {@link #fetchOrganizersByGroupIds} 에서 조회한다.
     * <p>
     *
     * @param groupIds 멤버를 채울 대상 그룹 ID 목록 (비어있지 않다고 가정)
     * @return {groupId → 해당 그룹 멤버 리스트}. 멤버가 없는 그룹은 맵에 키가 없다.
     */
    private Map<Long, List<MemberRow>> fetchMembersByGroupIds(List<Long> groupIds) {
        List<MemberRow> rows = queryFactory
            .select(Projections.constructor(MemberRow.class,
                studyGroupMember.studyGroup.id,
                studyGroupMember.memberId,
                member.name,
                member.profileImageId))
            .from(studyGroupMember)
            .join(member).on(member.id.eq(studyGroupMember.memberId))
            .where(studyGroupMember.studyGroup.id.in(groupIds))
            .fetch();

        return rows.stream()
            .collect(Collectors.groupingBy(MemberRow::groupId));
    }

    /**
     * 그룹 헤더 + 운영진 리스트 + 멤버 리스트를 조합하여 {@link StudyGroupListInfo.StudyGroupInfo} DTO 하나를 만든다.
     *
     * @param header            헤더 Projection 결과: (groupId, name)
     * @param organizersByGroup {groupId → 해당 그룹 운영진 리스트}. 키가 없으면 빈 리스트로 처리.
     * @param membersByGroup    {groupId → 해당 그룹 멤버 리스트}. 키가 없으면 빈 리스트로 처리.
     */
    private StudyGroupListInfo.StudyGroupInfo toStudyGroupInfo(
        GroupHeaderRow header,
        Map<Long, List<OrganizerRow>> organizersByGroup,
        Map<Long, List<MemberRow>> membersByGroup
    ) {
        List<StudyGroupListInfo.StudyGroupInfo.Organizer> organizers =
            organizersByGroup.getOrDefault(header.groupId(), List.of()).stream()
                .map(r -> new StudyGroupListInfo.StudyGroupInfo.Organizer(
                    r.memberId(), r.memberName(), r.profileImageId()))
                .toList();

        List<StudyGroupListInfo.StudyGroupInfo.Member> members =
            membersByGroup.getOrDefault(header.groupId(), List.of()).stream()
                .map(r -> new StudyGroupListInfo.StudyGroupInfo.Member(
                    r.memberId(), r.memberName(), r.profileImageId()))
                .toList();

        return new StudyGroupListInfo.StudyGroupInfo(header.groupId(), header.name(), organizers, members);
    }

    /**
     * 각 {@link StudyGroupViewScope} 타입을 그에 맞는 EXISTS {@link BooleanExpression} 으로 변환한다.
     * <p>
     * {@link StudyGroupViewScope} 가 sealed interface 라서 switch 표현식이 <b>exhaustive</b> 로 강제된다.
     * 즉 새 Scope 타입을 추가하면 여기서 컴파일 에러가 나므로, 분기 누락이 원천 차단된다.
     * <p>
     * Scope 내부 입력값(예: 빈 schoolMemberIds, null memberId)으로 predicate를 만들 수 없으면 null을 반환하고,
     * 호출 측의 {@link #buildScopePredicate(List)} 에서 null 은 OR 결합에서 제외된다.
     *
     * @param scope 변환할 Scope
     * @return 해당 Scope의 EXISTS 서브쿼리 Expression. 생성 불가 시 null.
     */
    private BooleanExpression toScopePredicate(StudyGroupViewScope scope) {
        return switch (scope) {
            case StudyGroupViewScope.AsSchoolCore s -> schoolCoreExists(s.schoolMemberIds());
            case StudyGroupViewScope.AsPartLeader s -> partLeaderExists(s.memberId());
        };
    }

    /**
     * <b>학교 회장단 Scope</b>: "해당 학교 멤버가 하나라도 포함된 스터디 그룹" 조건을 EXISTS 서브쿼리로 만든다.
     * 핵심 포인트:
     * <ul>
     *   <li>schoolMemberIds가 비어있으면 {@code IN ()} SQL 에러를 피하기 위해 null 을 반환한다.
     *       호출 측에서 이 Scope를 OR 결합에서 빼게 된다.</li>
     * </ul>
     *
     * @param schoolMemberIds 학교 소속 멤버 ID 집합
     * @return EXISTS 서브쿼리. 입력이 null/비어있으면 null.
     */
    private BooleanExpression schoolCoreExists(Set<Long> schoolMemberIds) {
        if (schoolMemberIds == null || schoolMemberIds.isEmpty()) {
            return null;
        }
        QStudyGroupMember m = new QStudyGroupMember("m_core");
        return JPAExpressions
            .selectOne()
            .from(m)
            .where(m.studyGroup.eq(studyGroup).and(m.memberId.in(schoolMemberIds)))
            .exists();
    }

    /**
     * <b>파트장 Scope</b>: "해당 memberId가 StudyGroupOrganizer 로 등록된 스터디 그룹" 조건의 EXISTS 서브쿼리.
     * 학교 회장단 Scope와 동일한 패턴
     * 차이는 {@link QStudyGroupOrganizer} 를 보는 것과 단일 memberId 매칭이라는 점
     *
     * @param memberId 요청 주체 memberId (파트장 본인)
     * @return EXISTS 서브쿼리. 입력이 null 이면 null.
     */
    private BooleanExpression partLeaderExists(Long memberId) {
        if (memberId == null) {
            return null;
        }
        QStudyGroupOrganizer o = new QStudyGroupOrganizer("o_leader");
        return JPAExpressions
            .selectOne()
            .from(o)
            .where(o.studyGroup.eq(studyGroup).and(o.memberId.eq(memberId)))
            .exists();
    }

    /**
     * id DESC(신규 우선) 정렬 기준 커서 조건. cursor 미만(lt)의 그룹만 다음 페이지에 포함된다.
     * 첫 페이지는 cursor=null 이며 이 경우 조건을 반환하지 않아 where에서 무시된다.
     */
    private BooleanExpression descCursorCondition(Long cursor) {
        return cursor != null ? studyGroup.id.lt(cursor) : null;
    }

    private BooleanExpression partCondition(ChallengerPart part) {
        return part != null ? studyGroup.part.eq(part) : null;
    }

    private BooleanExpression cursorCondition(Long cursor) {
        return cursor != null ? studyGroup.id.gt(cursor) : null;
    }

    public Set<Long> findConflictedMemberIds(Long gisuId, ChallengerPart part, Set<Long> memberIds) {

        return queryFactory
            .select(studyGroupMember.memberId)
            .from(studyGroupMember)
            .join(studyGroupMember.studyGroup, studyGroup)
            .where(
                studyGroup.gisuId.eq(gisuId),
                studyGroup.part.eq(part),
                studyGroupMember.memberId.in(memberIds)
            )
            .fetch()
            .stream()
            .collect(Collectors.toSet());
    }

    // ---------------------------------------------------------------------
    // Projection Records
    // ---------------------------------------------------------------------

    /**
     * {@link #fetchGroupHeaders} 용 상위 그룹 헤더 Projection.
     *
     * @param groupId 스터디 그룹 ID
     * @param name    스터디 그룹 이름
     */
    private record GroupHeaderRow(Long groupId, String name) {}

    /**
     * {@link #fetchMembersByGroupIds} 용 멤버 Projection.
     * <p>
     * 한 행에 스터디 그룹 ID와 멤버의 기본 정보(이름, 프로필 이미지 ID)를 함께 싣는다.
     *
     * @param groupId        소속 스터디 그룹 ID (groupingBy 키)
     * @param memberId       멤버 ID
     * @param memberName     멤버 이름 (Member 도메인 JOIN 결과)
     * @param profileImageId 멤버 프로필 이미지 파일 ID (storage 도메인 식별자)
     */
    private record MemberRow(
        Long groupId,
        Long memberId,
        String memberName,
        String profileImageId
    ) {}

    /**
     * {@link #fetchOrganizersByGroupIds} 용 운영진 Projection.
     *
     * @param groupId        소속 스터디 그룹 ID (groupingBy 키)
     * @param memberId       운영진으로 등록된 멤버 ID
     * @param memberName     멤버 이름 (Member 도메인 JOIN 결과)
     * @param profileImageId 멤버 프로필 이미지 파일 ID (storage 도메인 식별자)
     */
    private record OrganizerRow(
        Long groupId,
        Long memberId,
        String memberName,
        String profileImageId
    ) {}
}
