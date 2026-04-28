package com.umc.product.organization.adapter.out.persistence.studygroup;

import static com.umc.product.member.domain.QMember.member;
import static com.umc.product.organization.domain.QSchool.school;
import static com.umc.product.organization.domain.QStudyGroup.studyGroup;
import static com.umc.product.organization.domain.QStudyGroupMember.studyGroupMember;
import static com.umc.product.organization.domain.QStudyGroupMentor.studyGroupMentor;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupMemberInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupNameInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupViewScope;
import com.umc.product.organization.domain.QStudyGroupMember;
import com.umc.product.organization.domain.QStudyGroupMentor;
import java.util.HashSet;
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
     * 역할 Scope 기반 스터디 그룹 이름 목록 조회.
     * <p>
     * {@link #findMyStudyGroups} 와 동일한 Scope 합성 파이프라인을 재사용하되, 페이지네이션과 파트장/멤버 상세 조립 없이 (groupId, name) 만 Projection 한다.
     * 드롭다운/토글 UI 용도라 전체 결과를 한 번에 내려준다.
     * <p>
     * Scope가 모두 비어 합성 predicate가 null 이면 빈 리스트 반환
     *
     * @param scopes 역할 기반 조회 범위 (비어있지 않다고 가정 — Adapter 에서 이미 단축)
     * @param gisuId 활성 기수 ID
     * @return Scope 범위 내 (groupId, name), id DESC 정렬
     */
    public List<StudyGroupNameInfo> findStudyGroupNames(List<StudyGroupViewScope> scopes, Long gisuId) {
        BooleanExpression scopePredicate = buildScopePredicate(scopes);
        if (scopePredicate == null) {
            return List.of();
        }

        return queryFactory
            .select(Projections.constructor(StudyGroupNameInfo.class,
                studyGroup.id,
                studyGroup.name))
            .from(studyGroup)
            .where(
                studyGroup.gisuId.eq(gisuId),
                scopePredicate
            )
            .orderBy(studyGroup.name.asc())
            .fetch();
    }


    /**
     * 내 스터디 그룹 목록 조회 (역할 Scope 기반).
     * <p>
     * 이 함수는 다섯 단계로 구성되어 있다. 본문은 단계만 호출해 "무엇을 하는지" 를 드러내고, 세부 SQL 처리는 각 단계의 private 헬퍼에 격리했다.
     * <ol>
     *   <li>{@link #buildScopePredicate(List)} — 역할 Scope들을 OR로 합쳐 하나의 WHERE 조건으로 변환</li>
     *   <li>{@link #fetchGroupHeaders(Long, BooleanExpression, Long, int)} — 상위 그룹 헤더(id/name)만 커서 페이징</li>
     *   <li>{@link #fetchMentorsByGroupIds(List)} — 상위 그룹들의 파트장(Mentor) 행을 한 번에 조회하여 groupId 기준으로 분배</li>
     *   <li>{@link #fetchMembersByGroupIds(List)} — 상위 그룹들의 멤버 행을 한 번에 조회하여 groupId 기준으로 분배</li>
     *   <li>헤더 + 파트장 + 멤버를 조합하여 DTO 변환</li>
     * </ol>
     * Mentor와 Member를 각각 별도 bulk 쿼리로 가져오는 이유: 둘은 <b>다른 테이블</b>
     * ({@code study_group_mentor}, {@code study_group_member})에 있고 독립된 엔티티다.
     * UNION 하면 행 구조/컬럼 타입이 섞여 오히려 조립 로직이 꼬이기 때문에,
     * 각자 projection → 자바 단에서 groupingBy 하는 편이 단순하다.
     *
     * @param scopes 역할 기반 조회 범위. Service에서 사용자 역할로 조립해 넘긴다.
     * @param gisuId 활성 기수 ID (이 기수의 스터디 그룹만 대상)
     * @param cursor 직전 페이지 마지막 groupId. 첫 페이지는 null.
     * @param size   조회 사이즈 (Service가 hasNext 판단용으로 +1 포함하여 넘김)
     * @return 상위 스터디 그룹 정보 + 각 그룹의 파트장/멤버 요약. scope가 전부 비어있거나 조회 결과가 없으면 빈 리스트.
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

        // 3) 잘라낸 그룹들의 파트장를 조회.
        Map<Long, List<MentorRow>> mentorsByGroup = fetchMentorsByGroupIds(groupIds);

        // 4) 잘라낸 그룹들의 멤버를 조회.
        Map<Long, List<MemberRow>> membersByGroup = fetchMembersByGroupIds(groupIds);

        // 5) 헤더 + 파트장 + 멤버를 조합해 DTO로 변환한다.
        return groupHeaders.stream()
            .map(header -> toStudyGroupInfo(header, mentorsByGroup, membersByGroup))
            .toList();
    }

    /**
     * 역할 Scope 리스트를 하나의 {@link BooleanExpression} 으로 합친다 (OR 결합).
     * <p>
     * 각 Scope는 {@link #toScopePredicate(StudyGroupViewScope)} 로 EXISTS 서브쿼리로 변환되는데, Scope의 입력값이 비어있는 경우(예:
     * schoolMemberIds가 비어있는 AsSchoolCore)에는 null이 반환된다. 여기서는 그런 null Scope를 먼저 걸러낸 뒤
     * {@code reduce(BooleanExpression::or)} 로 OR 결합한다.
     * <p>
     * 최종 결과가 null 이면 "조회할 Scope가 하나도 없다" 는 뜻이므로 호출 측에서 빈 리스트를 반환해야 한다. 그렇지 않고 빈 WHERE 로 쿼리를 돌리면 테이블 풀 스캔이 된다.
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
     * 멤버 JOIN을 하지 않는 이유: 멤버가 포함되면 한 그룹이 멤버 수만큼 중복 row로 나와 {@code LIMIT size} 가 "멤버 row 기준 size" 로 해석되어 페이징 의미가 붕괴된다.
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
    private List<GroupHeaderRow> fetchGroupHeaders(Long gisuId, BooleanExpression scopePredicate, Long cursor,
                                                   int size) {
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
     * 주어진 groupIds에 속한 모든 <b>파트장(Mentor)</b> 행을 한 번의 쿼리로 가져와 {@code groupId → mentors} 맵으로 재분배한다.
     * <p>
     * 파트장는 {@code study_group_mentor} 테이블에 멤버와는 별도로 관리되는 엔티티다. 따라서 {@code StudyGroupMember.isLeader} 플래그와는 별개로, 이 테이블을
     * 직접 JOIN 해 파트장 목록을 만든다.
     * <p>
     * N+1 방지를 위해 IN 절로 한 번에 묶고 자바 단에서 {@code groupingBy} 로 분배한다.
     *
     * @param groupIds 파트장를 채울 대상 그룹 ID 목록 (비어있지 않다고 가정)
     * @return {groupId → 해당 그룹 파트장 리스트}. 파트장가 없는 그룹은 맵에 키가 없다.
     */
    private Map<Long, List<MentorRow>> fetchMentorsByGroupIds(List<Long> groupIds) {
        List<MentorRow> rows = queryFactory
            .select(Projections.constructor(MentorRow.class,
                studyGroupMentor.studyGroup.id,
                studyGroupMentor.memberId,
                member.name,
                member.profileImageId))
            .from(studyGroupMentor)
            .join(member).on(member.id.eq(studyGroupMentor.memberId))
            .where(studyGroupMentor.studyGroup.id.in(groupIds))
            .fetch();

        return rows.stream()
            .collect(Collectors.groupingBy(MentorRow::groupId));
    }

    /**
     * 주어진 groupIds에 속한 모든 <b>멤버</b> 행을 한 번의 쿼리로 가져와 {@code groupId → members} 맵으로 재분배한다.
     * <p>
     * 여기서의 "멤버"는 {@code study_group_member} 테이블의 전체 소속 멤버이며, 파트장 여부와 관계없이 그룹에 속한 모든 사람을 포함한다. 파트장는 별도
     * {@link #fetchMentorsByGroupIds} 에서 조회한다.
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
     * 그룹 헤더 + 파트장 리스트 + 멤버 리스트를 조합하여 {@link StudyGroupListInfo.StudyGroupInfo} DTO 하나를 만든다.
     *
     * @param header         헤더 Projection 결과: (groupId, name)
     * @param mentorsByGroup {groupId → 해당 그룹 파트장 리스트}. 키가 없으면 빈 리스트로 처리.
     * @param membersByGroup {groupId → 해당 그룹 멤버 리스트}. 키가 없으면 빈 리스트로 처리.
     */
    private StudyGroupListInfo.StudyGroupInfo toStudyGroupInfo(
        GroupHeaderRow header,
        Map<Long, List<MentorRow>> mentorsByGroup,
        Map<Long, List<MemberRow>> membersByGroup
    ) {
        List<StudyGroupListInfo.StudyGroupInfo.Mentor> mentors =
            mentorsByGroup.getOrDefault(header.groupId(), List.of()).stream()
                .map(r -> new StudyGroupListInfo.StudyGroupInfo.Mentor(
                    r.memberId(), r.memberName(), r.profileImageId()))
                .toList();

        List<StudyGroupListInfo.StudyGroupInfo.Member> members =
            membersByGroup.getOrDefault(header.groupId(), List.of()).stream()
                .map(r -> new StudyGroupListInfo.StudyGroupInfo.Member(
                    r.memberId(), r.memberName(), r.profileImageId()))
                .toList();

        return new StudyGroupListInfo.StudyGroupInfo(header.groupId(), header.name(), mentors, members);
    }

    /**
     * 각 {@link StudyGroupViewScope} 타입을 그에 맞는 EXISTS {@link BooleanExpression} 으로 변환한다.
     * <p>
     * {@link StudyGroupViewScope} 가 sealed interface 라서 switch 표현식이 <b>exhaustive</b> 로 강제된다. 즉 새 Scope 타입을 추가하면 여기서
     * 컴파일 에러가 나므로, 분기 누락이 원천 차단된다.
     * <p>
     * Scope 내부 입력값(예: 빈 schoolMemberIds, null memberId)으로 predicate를 만들 수 없으면 null을 반환하고, 호출 측의
     * {@link #buildScopePredicate(List)} 에서 null 은 OR 결합에서 제외된다.
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
     * <b>파트장 Scope</b>: "해당 memberId가 StudyGroupMentor 로 등록된 스터디 그룹" 조건의 EXISTS 서브쿼리.
     * 학교 회장단 Scope와 동일한 패턴 차이는 {@link QStudyGroupMentor} 를 보는 것과 단일 memberId 매칭이라는 점
     *
     * @param memberId 요청 주체 memberId (파트장 본인)
     * @return EXISTS 서브쿼리. 입력이 null 이면 null.
     */
    private BooleanExpression partLeaderExists(Long memberId) {
        if (memberId == null) {
            return null;
        }
        QStudyGroupMentor o = new QStudyGroupMentor("o_leader");
        return JPAExpressions
            .selectOne()
            .from(o)
            .where(o.studyGroup.eq(studyGroup).and(o.memberId.eq(memberId)))
            .exists();
    }

    /**
     * id DESC(신규 우선) 정렬 기준 커서 조건. cursor 미만(lt)의 그룹만 다음 페이지에 포함된다. 첫 페이지는 cursor=null 이며 이 경우 조건을 반환하지 않아 where에서
     * 무시된다.
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

    /**
     * 스터디 그룹 ID 로 소속 스터디원(study_group_member) 목록을 한 번의 쿼리로 조회한다.
     * <p>
     * Member/School 도메인과 JOIN 하여 한 행에 (memberId, 학교명, 프로필 이미지 ID) 를 실어 반환한다.
     *
     * @param groupId 스터디 그룹 ID
     * @return 스터디원 Projection 목록. 소속이 없으면 빈 리스트.
     */
    public List<StudyGroupMemberInfo> findStudyGroupMembers(Long groupId) {
        return queryFactory
            .select(Projections.constructor(StudyGroupMemberInfo.class,
                studyGroupMember.memberId,
                school.id,
                school.name,
                member.profileImageId))
            .from(studyGroupMember)
            .join(member).on(member.id.eq(studyGroupMember.memberId))
            .join(school).on(school.id.eq(member.schoolId))
            .where(studyGroupMember.studyGroup.id.eq(groupId))
            .fetch();
    }

    public Set<Long> findConflictedMemberIds(Long gisuId, ChallengerPart part, Set<Long> memberIds) {

        return new HashSet<>(queryFactory
            .select(studyGroupMember.memberId)
            .from(studyGroupMember)
            .join(studyGroupMember.studyGroup, studyGroup)
            .where(
                studyGroup.gisuId.eq(gisuId),
                studyGroup.part.eq(part),
                studyGroupMember.memberId.in(memberIds)
            )
            .fetch()
        );
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
    private record GroupHeaderRow(Long groupId, String name) {
    }

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
    ) {
    }

    /**
     * {@link #fetchMentorsByGroupIds} 용 파트장 Projection.
     *
     * @param groupId        소속 스터디 그룹 ID (groupingBy 키)
     * @param memberId       파트장으로 등록된 멤버 ID
     * @param memberName     멤버 이름 (Member 도메인 JOIN 결과)
     * @param profileImageId 멤버 프로필 이미지 파일 ID (storage 도메인 식별자)
     */
    private record MentorRow(
        Long groupId,
        Long memberId,
        String memberName,
        String profileImageId
    ) {
    }
}
