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
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
     * - 멤버의 학교를 기준으로 조회 (한 스터디 그룹에 여러 학교 멤버 가능)
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
                .from(studyGroupMember)
                .join(studyGroupMember.studyGroup, studyGroup)
                .join(challenger).on(challenger.id.eq(studyGroupMember.challengerId))
                .join(member).on(member.id.eq(challenger.memberId))
                .join(school).on(school.id.eq(member.schoolId))
                .where(studyGroup.gisu.isActive.eq(true))
                .groupBy(school.id, school.name, school.logoImageUrl)
                .orderBy(school.name.asc())
                .fetch();
    }

    /**
     * 2단계: 특정 학교의 파트별 스터디 그룹 요약 조회 (활성 기수 기준)
     * - 해당 학교 멤버가 포함된 스터디 그룹을 파트별로 집계
     */
    public PartSummaryInfo findPartSummary(Long schoolId) {
        // 학교 정보 먼저 조회
        Tuple schoolInfo = queryFactory
                .select(school.id, school.name)
                .from(school)
                .where(school.id.eq(schoolId))
                .fetchOne();

        if (schoolInfo == null) {
            return new PartSummaryInfo(schoolId, null, List.of());
        }

        String schoolName = schoolInfo.get(school.name);

        // 해당 학교 멤버가 포함된 스터디 그룹의 파트별 집계
        List<Tuple> results = queryFactory
                .select(
                        studyGroup.part,
                        studyGroup.id.countDistinct().intValue(),
                        studyGroupMember.id.count().intValue()
                )
                .from(studyGroupMember)
                .join(studyGroupMember.studyGroup, studyGroup)
                .join(challenger).on(challenger.id.eq(studyGroupMember.challengerId))
                .join(member).on(member.id.eq(challenger.memberId))
                .where(
                        studyGroup.gisu.isActive.eq(true),
                        member.schoolId.eq(schoolId)
                )
                .groupBy(studyGroup.part)
                .orderBy(studyGroup.part.asc())
                .fetch();

        if (results.isEmpty()) {
            return new PartSummaryInfo(schoolId, schoolName, List.of());
        }

        // 파트 정보 변환
        List<PartSummaryInfo.PartInfo> parts = results.stream()
                .map(r -> new PartSummaryInfo.PartInfo(
                        r.get(studyGroup.part),
                        r.get(1, Integer.class),
                        r.get(2, Integer.class)
                ))
                .toList();

        return new PartSummaryInfo(schoolId, schoolName, parts);
    }

    /**
     * 3단계: 스터디 그룹 목록 조회 (활성 기수 기준)
     * - 해당 학교 멤버가 포함된 스터디 그룹만 조회
     * - 페이지네이션은 Controller에서 CursorResponse.of()로 처리
     *
     */
    public List<StudyGroupListInfo.StudyGroupInfo> findStudyGroups(Long schoolId, ChallengerPart part,
            Long cursor, int size) {
        // 해당 학교 멤버가 포함된 스터디 그룹 ID 조회
        List<Long> studyGroupIdsWithSchool = queryFactory
                .selectDistinct(studyGroup.id)
                .from(studyGroupMember)
                .join(studyGroupMember.studyGroup, studyGroup)
                .join(challenger).on(challenger.id.eq(studyGroupMember.challengerId))
                .join(member).on(member.id.eq(challenger.memberId))
                .where(
                        studyGroup.gisu.isActive.eq(true),
                        studyGroup.part.eq(part),
                        member.schoolId.eq(schoolId)
                )
                .fetch();

        if (studyGroupIdsWithSchool.isEmpty()) {
            return List.of();
        }

        // 스터디 그룹 기본 정보 조회 (Tuple 사용)
        List<Tuple> groups = queryFactory
                .select(studyGroup.id, studyGroup.name)
                .from(studyGroup)
                .where(
                        studyGroup.id.in(studyGroupIdsWithSchool),
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

        // 멤버 정보 조회 (리더 + 일반 멤버 )
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
     * - 멤버들의 학교 목록을 조회 (여러 학교 가능)
     */
    public StudyGroupDetailInfo findStudyGroupDetail(Long groupId) {
        // 스터디 그룹 기본 정보 조회
        Tuple groupInfo = queryFactory
                .select(
                        studyGroup.id,
                        studyGroup.name,
                        studyGroup.part,
                        studyGroup.createdAt
                )
                .from(studyGroup)
                .where(studyGroup.id.eq(groupId))
                .fetchOne();

        if (groupInfo == null) {
            return null;
        }

        // 멤버/리더/학교 정보를 한 번에 조회한 뒤 분리
        List<Tuple> memberRows = queryFactory
                .select(
                        challenger.id,
                        member.id,
                        member.name,
                        member.profileImageId.stringValue(),
                        studyGroupMember.isLeader,
                        school.id,
                        school.name
                )
                .from(studyGroupMember)
                .join(challenger).on(challenger.id.eq(studyGroupMember.challengerId))
                .join(member).on(member.id.eq(challenger.memberId))
                .join(school).on(school.id.eq(member.schoolId))
                .where(studyGroupMember.studyGroup.id.eq(groupId))
                .fetch();

        StudyGroupDetailInfo.MemberInfo leaderResult = null;
        List<StudyGroupDetailInfo.MemberInfo> allMembers = new ArrayList<>(memberRows.size());
        List<StudyGroupDetailInfo.MemberInfo> nonLeaderMembers = new ArrayList<>();
        Map<Long, StudyGroupDetailInfo.SchoolInfo> schoolMap = new LinkedHashMap<>();

        for (Tuple row : memberRows) {
            StudyGroupDetailInfo.MemberInfo memberInfo = new StudyGroupDetailInfo.MemberInfo(
                    row.get(challenger.id),
                    row.get(member.id),
                    row.get(member.name),
                    row.get(member.profileImageId.stringValue())
            );
            allMembers.add(memberInfo);

            Boolean isLeader = row.get(studyGroupMember.isLeader);
            if (Boolean.TRUE.equals(isLeader)) {
                leaderResult = memberInfo;
            } else {
                nonLeaderMembers.add(memberInfo);
            }

            Long schoolId = row.get(school.id);
            if (schoolId != null) {
                schoolMap.putIfAbsent(schoolId, new StudyGroupDetailInfo.SchoolInfo(
                        schoolId,
                        row.get(school.name)
                ));
            }
        }

        List<StudyGroupDetailInfo.SchoolInfo> schools = new ArrayList<>(schoolMap.values());

        return new StudyGroupDetailInfo(
                groupInfo.get(studyGroup.id),
                groupInfo.get(studyGroup.name),
                groupInfo.get(studyGroup.part),
                schools,
                groupInfo.get(studyGroup.createdAt).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                allMembers.size(),
                leaderResult,
                nonLeaderMembers
        );
    }

    private BooleanExpression cursorCondition(Long cursor) {
        return cursor != null ? studyGroup.id.gt(cursor) : null;
    }
}
