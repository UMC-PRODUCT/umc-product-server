package com.umc.product.organization.adapter.out.persistence;

import static com.umc.product.challenger.domain.QChallenger.challenger;
import static com.umc.product.member.domain.QMember.member;
import static com.umc.product.organization.domain.QSchool.school;
import static com.umc.product.organization.domain.QStudyGroup.studyGroup;
import static com.umc.product.organization.domain.QStudyGroupMember.studyGroupMember;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.application.port.in.query.dto.PartSummaryInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolStudyGroupInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListInfo;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudyGroupQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 1단계: 스터디 그룹이 있는 학교 목록 조회
     */
    public List<SchoolStudyGroupInfo> findSchoolsWithStudyGroups() {
        return queryFactory
                .select(Projections.constructor(SchoolStudyGroupInfo.class,
                        school.id,
                        school.name,
                        school.logoImageUrl,
                        studyGroup.id.countDistinct().intValue(),
                        studyGroupMember.id.count().intValue()
                ))
                .from(studyGroup)
                .join(school).on(school.id.eq(studyGroup.schoolId))
                .leftJoin(studyGroupMember).on(studyGroupMember.studyGroup.eq(studyGroup))
                .where(studyGroup.gisu.isActive.eq(true))
                .groupBy(school.id, school.name, school.logoImageUrl)
                .orderBy(school.name.asc())
                .fetch();
    }

    /**
     * 2단계: 특정 학교의 파트별 스터디 그룹 요약 조회 (활성 기수 기준)
     */
    public PartSummaryInfo findPartSummary(Long schoolId) {
        // Tuple 사용하여 쿼리
        List<Tuple> results = queryFactory
                .select(
                        school.id,
                        school.name,
                        studyGroup.part,
                        studyGroup.id.countDistinct().intValue(),
                        studyGroupMember.id.count().intValue()
                )
                .from(studyGroup)
                .join(school).on(school.id.eq(studyGroup.schoolId))
                .leftJoin(studyGroupMember).on(studyGroupMember.studyGroup.eq(studyGroup))
                .where(
                        studyGroup.gisu.isActive.eq(true),
                        studyGroup.schoolId.eq(schoolId)
                )
                .groupBy(school.id, school.name, studyGroup.part)
                .orderBy(studyGroup.part.asc())
                .fetch();

        if (results.isEmpty()) {
            return new PartSummaryInfo(schoolId, null, List.of());
        }

        // 첫 번째 결과에서 학교 정보 추출
        Long resultSchoolId = results.get(0).get(school.id);
        String schoolName = results.get(0).get(school.name);

        // 파트 정보 변환
        List<PartSummaryInfo.PartInfo> parts = results.stream()
                .map(r -> new PartSummaryInfo.PartInfo(
                        r.get(studyGroup.part),
                        r.get(3, Integer.class),
                        r.get(4, Integer.class)
                ))
                .toList();

        return new PartSummaryInfo(resultSchoolId, schoolName, parts);
    }

    /**
     * 3단계: 스터디 그룹 목록 조회 (활성 기수 기준)
     * 페이지네이션은 Controller에서 CursorResponse.of()로 처리
     */
    public List<StudyGroupListInfo.StudyGroupInfo> findStudyGroups(Long schoolId, ChallengerPart part,
            Long cursor, int size) {
        // 스터디 그룹 기본 정보 조회 (Tuple 사용)
        List<Tuple> groups = queryFactory
                .select(studyGroup.id, studyGroup.name)
                .from(studyGroup)
                .where(
                        studyGroup.gisu.isActive.eq(true),
                        studyGroup.schoolId.eq(schoolId),
                        studyGroup.part.eq(part),
                        cursorCondition(cursor)
                )
                .orderBy(studyGroup.id.asc())
                .limit(size)
                .fetch();

        // 스터디 그룹 ID 목록
        List<Long> groupIds = groups.stream().map(t -> t.get(studyGroup.id)).toList();

        if (groupIds.isEmpty()) {
            return List.of();
        }

        // 멤버 정보 조회 (리더 + 일반 멤버)
        List<Tuple> memberResults = queryFactory
                .select(
                        studyGroupMember.studyGroup.id,
                        challenger.id,
                        member.name,
                        member.profileImageId.stringValue(),
                        studyGroupMember.isLeader
                )
                .from(studyGroupMember)
                .join(challenger).on(challenger.id.eq(studyGroupMember.challengerId))
                .join(member).on(member.id.eq(challenger.memberId))
                .where(studyGroupMember.studyGroup.id.in(groupIds))
                .fetch();

        // 그룹별 멤버 맵 생성
        Map<Long, List<Tuple>> membersByGroup = memberResults.stream()
                .collect(Collectors.groupingBy(t -> t.get(studyGroupMember.studyGroup.id)));

        // StudyGroupInfo 변환
        return groups.stream()
                .map(g -> {
                    Long groupId = g.get(studyGroup.id);
                    String groupName = g.get(studyGroup.name);
                    List<Tuple> members = membersByGroup.getOrDefault(groupId, List.of());

                    StudyGroupListInfo.StudyGroupInfo.LeaderInfo leader = members.stream()
                            .filter(m -> Boolean.TRUE.equals(m.get(studyGroupMember.isLeader)))
                            .findFirst()
                            .map(m -> new StudyGroupListInfo.StudyGroupInfo.LeaderInfo(
                                    m.get(challenger.id),
                                    m.get(member.name),
                                    m.get(member.profileImageId.stringValue())))
                            .orElse(null);

                    List<StudyGroupListInfo.StudyGroupInfo.MemberSummaryInfo> memberSummaries = members.stream()
                            .filter(m -> !Boolean.TRUE.equals(m.get(studyGroupMember.isLeader)))
                            .map(m -> new StudyGroupListInfo.StudyGroupInfo.MemberSummaryInfo(
                                    m.get(challenger.id),
                                    m.get(member.name),
                                    m.get(member.profileImageId.stringValue())))
                            .toList();

                    return new StudyGroupListInfo.StudyGroupInfo(
                            groupId,
                            groupName,
                            members.size(),
                            leader,
                            memberSummaries
                    );
                })
                .toList();
    }

    /**
     * 4단계: 스터디 그룹 상세 조회
     */
    public StudyGroupDetailInfo findStudyGroupDetail(Long groupId) {
        // 스터디 그룹 기본 정보 + 학교 정보 조회
        Tuple groupInfo = queryFactory
                .select(
                        studyGroup.id,
                        studyGroup.name,
                        studyGroup.part,
                        school.id,
                        school.name,
                        studyGroup.createdAt
                )
                .from(studyGroup)
                .join(school).on(school.id.eq(studyGroup.schoolId))
                .where(studyGroup.id.eq(groupId))
                .fetchOne();

        if (groupInfo == null) {
            return null;
        }

        // 멤버 정보 조회
        List<StudyGroupDetailInfo.MemberInfo> members = queryFactory
                .select(Projections.constructor(StudyGroupDetailInfo.MemberInfo.class,
                        challenger.id,
                        member.id,
                        member.name,
                        member.profileImageId.stringValue() // TODO: 실제 URL 변환 필요
                ))
                .from(studyGroupMember)
                .join(challenger).on(challenger.id.eq(studyGroupMember.challengerId))
                .join(member).on(member.id.eq(challenger.memberId))
                .where(studyGroupMember.studyGroup.id.eq(groupId))
                .fetch();

        // 리더 찾기
        StudyGroupDetailInfo.MemberInfo leaderResult = queryFactory
                .select(Projections.constructor(StudyGroupDetailInfo.MemberInfo.class,
                        challenger.id,
                        member.id,
                        member.name,
                        member.profileImageId.stringValue()
                ))
                .from(studyGroupMember)
                .join(challenger).on(challenger.id.eq(studyGroupMember.challengerId))
                .join(member).on(member.id.eq(challenger.memberId))
                .where(
                        studyGroupMember.studyGroup.id.eq(groupId),
                        studyGroupMember.isLeader.isTrue()
                )
                .fetchOne();

        // 일반 멤버 (리더 제외)
        List<StudyGroupDetailInfo.MemberInfo> nonLeaderMembers = queryFactory
                .select(Projections.constructor(StudyGroupDetailInfo.MemberInfo.class,
                        challenger.id,
                        member.id,
                        member.name,
                        member.profileImageId.stringValue()
                ))
                .from(studyGroupMember)
                .join(challenger).on(challenger.id.eq(studyGroupMember.challengerId))
                .join(member).on(member.id.eq(challenger.memberId))
                .where(
                        studyGroupMember.studyGroup.id.eq(groupId),
                        studyGroupMember.isLeader.isFalse()
                )
                .fetch();

        return new StudyGroupDetailInfo(
                groupInfo.get(studyGroup.id),
                groupInfo.get(studyGroup.name),
                groupInfo.get(studyGroup.part),
                groupInfo.get(school.id),
                groupInfo.get(school.name),
                groupInfo.get(studyGroup.createdAt).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                members.size(),
                leaderResult,
                nonLeaderMembers
        );
    }

    private BooleanExpression cursorCondition(Long cursor) {
        return cursor != null ? studyGroup.id.gt(cursor) : null;
    }
}
