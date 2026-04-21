package com.umc.product.organization.application.port.in.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListQuery;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupNameInfo;
import java.util.List;
import java.util.Set;

/**
 * 스터디 그룹 조회 UseCase
 */
public interface GetStudyGroupUseCase {

        /**
         * 내 스터디 그룹 목록 조회.
         * <p>
         * memberId만으로 schoolId/활성 기수/역할을 내부에서 resolve 하여 역할 기반 Scope로 조회한다.
         * <ul>
         *   <li>학교 회장단 → 학교 멤버가 포함된 모든 그룹</li>
         *   <li>파트장 → 본인이 운영진인 그룹</li>
         *   <li>권한 없음 → 빈 리스트</li>
         * </ul>
         *
         * @param memberId 요청 주체 memberId
         * @param cursor   직전 페이지 마지막 groupId (첫 페이지는 null)
         * @param size     페이지 크기
         * @return 조회된 스터디 그룹 요약 목록 (권한 없으면 빈 리스트)
         */
        List<StudyGroupListInfo.StudyGroupInfo> getMyStudyGroups(Long memberId, Long cursor, int size);

        /**
         * 스터디 그룹 목록 조회 (cursor 기반, fetchSize만큼 조회)
         */
        List<StudyGroupListInfo.StudyGroupInfo> getStudyGroups(StudyGroupListQuery query);

        /**
         * 스터디 그룹 이름 목록 조회 - memberId 기반으로 schoolId/part를 자동 resolve
         */
        List<StudyGroupNameInfo> getStudyGroupNames(Long memberId);

        /**
         * 스터디 그룹 상세 조회
         */
        StudyGroupDetailInfo getStudyGroupDetail(Long groupId);

        /**
         * 특정 기수에서 해당 파트들의 스터디 그룹 ID 목록 조회 (파트장용)
         */
        List<Long> getStudyGroupIdsByParts(Long gisuId, Set<ChallengerPart> parts);
}
