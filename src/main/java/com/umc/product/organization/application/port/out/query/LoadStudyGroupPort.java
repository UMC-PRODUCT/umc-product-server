package com.umc.product.organization.application.port.out.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.application.port.in.query.dto.PartSummaryInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolStudyGroupInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListInfo;
import com.umc.product.organization.domain.StudyGroup;
import java.util.List;

public interface LoadStudyGroupPort {

    StudyGroup findById(Long id);

    StudyGroup findByName(String name);

    /**
     * 1단계: 스터디 그룹이 있는 학교 목록 조회
     */
    List<SchoolStudyGroupInfo> findSchoolsWithStudyGroups();

    /**
     * 2단계: 특정 학교의 파트별 스터디 그룹 요약 조회
     */
    PartSummaryInfo findPartSummary(Long schoolId);

    /**
     * 3단계: 스터디 그룹 목록 조회 (cursor 기반, 활성 기수 기준)
     * 페이지네이션 처리는 Controller에서 CursorResponse.of()로 수행
     */
    List<StudyGroupListInfo.StudyGroupInfo> findStudyGroups(Long schoolId, ChallengerPart part, Long cursor, int size);

    /**
     * 4단계: 스터디 그룹 상세 조회
     */
    StudyGroupDetailInfo findStudyGroupDetail(Long groupId);
}
