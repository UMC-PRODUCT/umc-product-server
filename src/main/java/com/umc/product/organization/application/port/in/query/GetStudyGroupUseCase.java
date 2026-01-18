package com.umc.product.organization.application.port.in.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;

/**
 * 스터디 그룹 조회 UseCase
 * 단계별 drill-down 조회를 지원합니다.
 */
public interface GetStudyGroupUseCase {

    /**
     * 1단계: 스터디 그룹이 있는 학교 목록 조회
     *
     * @param gisuId 기수 ID
     * @return 학교 목록 (스터디 그룹 수, 멤버 수 포함)
     */
    List<SchoolWithStudyGroupCount> getSchools(Long gisuId);

    /**
     * 2단계: 특정 학교의 파트별 스터디 그룹 요약 조회
     *
     * @param gisuId   기수 ID
     * @param schoolId 학교 ID
     * @return 파트 목록 (스터디 그룹 수, 멤버 수 포함)
     */
    PartSummaryResult getParts(Long gisuId, Long schoolId);

    /**
     * 3단계: 스터디 그룹 목록 조회 (cursor 기반 페이지네이션)
     *
     * @param query 조회 조건
     * @return 스터디 그룹 목록 (cursor 포함)
     */
    StudyGroupListResult getStudyGroups(StudyGroupListQuery query);

    /**
     * 4단계: 스터디 그룹 상세 조회
     *
     * @param groupId 스터디 그룹 ID
     * @return 스터디 그룹 상세 정보 (멤버 목록 포함)
     */
    StudyGroupDetail getStudyGroupDetail(Long groupId);

    // ============ Query & Result Records ============

    record SchoolWithStudyGroupCount(
            Long schoolId,
            String schoolName,
            String logoImageUrl,
            int totalStudyGroupCount,
            int totalMemberCount
    ) {
    }

    record PartSummaryResult(
            Long schoolId,
            String schoolName,
            List<PartSummary> parts
    ) {
    }

    record PartSummary(
            ChallengerPart part,
            int studyGroupCount,
            int memberCount
    ) {
    }

    record StudyGroupListQuery(
            Long gisuId,
            Long schoolId,
            ChallengerPart part,
            Long cursor,
            int size
    ) {
        public StudyGroupListQuery {
            if (size <= 0) {
                size = 20;
            }
            if (size > 100) {
                size = 100;
            }
        }
    }

    record StudyGroupListResult(
            List<StudyGroupSummary> studyGroups,
            Long nextCursor,
            boolean hasNext
    ) {
    }

    record StudyGroupSummary(
            Long groupId,
            String name,
            int memberCount,
            LeaderInfo leader
    ) {
    }

    record LeaderInfo(
            Long challengerId,
            String name,
            String profileImageUrl
    ) {
    }

    record StudyGroupDetail(
            Long groupId,
            String name,
            ChallengerPart part,
            Long schoolId,
            String schoolName,
            java.time.LocalDateTime createdAt,
            int memberCount,
            MemberInfo leader,
            List<MemberInfo> members
    ) {
    }

    record MemberInfo(
            Long challengerId,
            Long memberId,
            String name,
            String profileImageUrl
    ) {
    }
}
