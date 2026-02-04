package com.umc.product.organization.application.port.service.query;

import com.umc.product.organization.application.port.in.query.GetSchoolAccessContextUseCase;
import com.umc.product.organization.application.port.in.query.GetStudyGroupUseCase;
import com.umc.product.organization.application.port.in.query.dto.PartSummaryInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolAccessContext;
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
    private final GetSchoolAccessContextUseCase getSchoolAccessContextUseCase;

    @Deprecated
    @Override
    public List<SchoolStudyGroupInfo> getSchools() {
        return loadStudyGroupPort.findSchoolsWithStudyGroups();
    }

    @Deprecated
    @Override
    public PartSummaryInfo getParts(Long schoolId) {
        return loadStudyGroupPort.findPartSummary(schoolId);
    }

    @Override
    public List<StudyGroupListInfo.StudyGroupInfo> getMyStudyGroups(Long memberId, Long cursor, int size) {
        SchoolAccessContext context = getSchoolAccessContextUseCase.getContext(memberId);

        StudyGroupListQuery query = new StudyGroupListQuery(
                context.schoolId(), context.part(), cursor, size
        );

        return getStudyGroups(query);
    }

    @Override
    public List<StudyGroupListInfo.StudyGroupInfo> getStudyGroups(StudyGroupListQuery query) {
        return loadStudyGroupPort.findStudyGroups(
                query.schoolId(), query.part(), query.cursor(), query.fetchSize());
    }

    @Override
    public StudyGroupDetailInfo getStudyGroupDetail(Long groupId) {
        return loadStudyGroupPort.findStudyGroupDetail(groupId);
    }
}
