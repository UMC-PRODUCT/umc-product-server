package com.umc.product.curriculum.application.service.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.GetStudyGroupsForFilterUseCase;
import com.umc.product.curriculum.application.port.in.query.GetWorkbookSubmissionsUseCase;
import com.umc.product.curriculum.application.port.in.query.dto.GetWorkbookSubmissionsQuery;
import com.umc.product.curriculum.application.port.in.query.dto.StudyGroupFilterInfo;
import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionInfo;
import com.umc.product.curriculum.application.port.out.LoadWorkbookSubmissionPort;
import com.umc.product.organization.application.port.in.query.GetStudyGroupUseCase;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListQuery;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkbookSubmissionQueryService implements
        GetWorkbookSubmissionsUseCase,
        GetStudyGroupsForFilterUseCase {

    private final LoadWorkbookSubmissionPort loadWorkbookSubmissionPort;
    private final GetStudyGroupUseCase getStudyGroupUseCase;

    @Override
    public List<WorkbookSubmissionInfo> getSubmissions(GetWorkbookSubmissionsQuery query) {
        return loadWorkbookSubmissionPort.findSubmissions(query);
    }

    @Override
    public List<StudyGroupFilterInfo> getStudyGroupsForFilter(Long schoolId, ChallengerPart part) {
        StudyGroupListQuery query = new StudyGroupListQuery(schoolId, part, null, 100);
        return getStudyGroupUseCase.getStudyGroups(query).stream()
                .map(info -> new StudyGroupFilterInfo(info.groupId(), info.name()))
                .toList();
    }
}
