package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.PartSummaryInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolStudyGroupInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListQuery;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupNameInfo;
import java.util.List;

/**
 * 스터디 그룹 조회 UseCase
 */
public interface GetStudyGroupUseCase {

        /**
         * @deprecated 3단계 getMyStudyGroups로 대체
         */
        @Deprecated
        List<SchoolStudyGroupInfo> getSchools();

        /**
         * @deprecated 3단계 getMyStudyGroups로 대체
         */
        @Deprecated
        PartSummaryInfo getParts(Long schoolId);

        /**
         * 내 스터디 그룹 목록 조회 - memberId 기반으로 schoolId/part를 자동 resolve
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
}
