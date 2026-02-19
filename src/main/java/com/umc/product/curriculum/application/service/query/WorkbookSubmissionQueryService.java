package com.umc.product.curriculum.application.service.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.GetAvailableWeeksUseCase;
import com.umc.product.curriculum.application.port.in.query.GetStudyGroupsForFilterUseCase;
import com.umc.product.curriculum.application.port.in.query.GetWorkbookSubmissionsUseCase;
import com.umc.product.curriculum.application.port.in.query.dto.GetWorkbookSubmissionsQuery;
import com.umc.product.curriculum.application.port.in.query.dto.StudyGroupFilterInfo;
import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionDetailInfo;
import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionInfo;
import com.umc.product.curriculum.application.port.out.LoadChallengerWorkbookPort;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import com.umc.product.curriculum.application.port.out.LoadWorkbookSubmissionPort;
import com.umc.product.organization.application.port.in.query.GetStudyGroupUseCase;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListQuery;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkbookSubmissionQueryService implements
        GetWorkbookSubmissionsUseCase,
        GetStudyGroupsForFilterUseCase,
        GetAvailableWeeksUseCase {

    private final LoadWorkbookSubmissionPort loadWorkbookSubmissionPort;
    private final LoadChallengerWorkbookPort loadChallengerWorkbookPort;
    private final LoadOriginalWorkbookPort loadOriginalWorkbookPort;
    private final GetStudyGroupUseCase getStudyGroupUseCase;
    private final GetFileUseCase getFileUseCase;

    @Override
    public List<WorkbookSubmissionInfo> getSubmissions(GetWorkbookSubmissionsQuery query) {
        List<WorkbookSubmissionInfo> submissions = loadWorkbookSubmissionPort.findSubmissions(query);

        Map<String, String> urlMap = submissions.stream()
                .map(WorkbookSubmissionInfo::profileImageUrl)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toMap(id -> id, id -> getFileUseCase.getById(id).fileLink()));

        return submissions.stream()
                .map(s -> new WorkbookSubmissionInfo(
                        s.challengerWorkbookId(),
                        s.challengerId(),
                        s.challengerName(),
                        urlMap.getOrDefault(s.profileImageUrl(), s.profileImageUrl()),
                        s.schoolName(),
                        s.part(),
                        s.workbookTitle(),
                        s.status()
                ))
                .toList();
    }

    @Override
    public WorkbookSubmissionDetailInfo getSubmissionDetail(Long challengerWorkbookId) {
        return WorkbookSubmissionDetailInfo.from(
                loadChallengerWorkbookPort.findById(challengerWorkbookId)
        );
    }

    @Override
    public List<Integer> getAvailableWeeks(ChallengerPart part) {
        return loadOriginalWorkbookPort.findReleasedWeekNos(part);
    }

    @Override
    public List<StudyGroupFilterInfo> getStudyGroupsForFilter(Long schoolId, ChallengerPart part) {
        StudyGroupListQuery query = new StudyGroupListQuery(schoolId, part, null, 100);
        return getStudyGroupUseCase.getStudyGroups(query).stream()
                .map(info -> new StudyGroupFilterInfo(info.groupId(), info.name()))
                .toList();
    }
}
