package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.PartSummaryInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolStudyGroupInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListQuery;
import java.util.List;

/**
 * 스터디 그룹 조회 UseCase
 * 단계별 drill-down 조회를 지원합니다.
 */
public interface GetStudyGroupUseCase {

        /**
         * 1단계: 스터디 그룹이 있는 학교 목록 조회
         */
        List<SchoolStudyGroupInfo> getSchools();

        /**
         * 2단계: 특정 학교의 파트별 스터디 그룹 요약 조회
         */
        PartSummaryInfo getParts(Long schoolId);

        /**
         * 3단계: 스터디 그룹 목록 조회 (cursor 기반, fetchSize만큼 조회)
         * Controller에서 CursorResponse.of()로 페이지네이션 처리
         */
        List<StudyGroupListInfo.StudyGroupInfo> getStudyGroups(StudyGroupListQuery query);

        /**
         * 4단계: 스터디 그룹 상세 조회
         */
        StudyGroupDetailInfo getStudyGroupDetail(Long groupId);
}
