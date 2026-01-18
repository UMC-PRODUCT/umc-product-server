package com.umc.product.organization.application.port.service.query;

import com.umc.product.organization.application.port.in.query.GetStudyGroupUseCase;
import com.umc.product.organization.application.port.in.query.dto.PartSummaryInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolStudyGroupInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListQuery;
import com.umc.product.organization.application.port.out.query.LoadStudyGroupPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyGroupQueryService implements GetStudyGroupUseCase {

    private final LoadStudyGroupPort loadStudyGroupPort;

    /**
     * 1단계: 스터디 그룹이 있는 학교 목록 조회
     */
    @Override
    public List<SchoolStudyGroupInfo> getSchools() {
        return loadStudyGroupPort.findSchoolsWithStudyGroups();
    }

    /**
     * 2단계: 특정 학교의 파트별 스터디 그룹 요약 조회
     */
    @Override
    public PartSummaryInfo getParts(Long schoolId) {
        return loadStudyGroupPort.findPartSummary(schoolId);
    }

    /**
     * 3단계: 스터디 그룹 목록 조회 (활성 기수 기준)
     * fetchSize(size+1)로 조회하여 Controller에서 CursorResponse.of()로 페이지네이션 처리
     */
    @Override
    public List<StudyGroupListInfo.StudyGroupInfo> getStudyGroups(StudyGroupListQuery query) {
        return loadStudyGroupPort.findStudyGroups(
                query.schoolId(),
                query.part(),
                query.cursor(),
                query.fetchSize()
        );
    }

    /**
     * 4단계: 스터디 그룹 상세 조회
     */
    @Override
    public StudyGroupDetailInfo getStudyGroupDetail(Long groupId) {
        return loadStudyGroupPort.findStudyGroupDetail(groupId);
    }
}
