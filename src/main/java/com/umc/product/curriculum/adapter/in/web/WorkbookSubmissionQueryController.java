package com.umc.product.curriculum.adapter.in.web;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.adapter.in.web.dto.response.StudyGroupFilterResponse;
import com.umc.product.curriculum.adapter.in.web.dto.response.WorkbookSubmissionResponse;
import com.umc.product.curriculum.application.port.in.query.GetStudyGroupsForFilterUseCase;
import com.umc.product.curriculum.application.port.in.query.GetWorkbookSubmissionsUseCase;
import com.umc.product.curriculum.application.port.in.query.dto.GetWorkbookSubmissionsQuery;
import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionInfo;
import com.umc.product.global.response.CursorResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/curriculums")
@RequiredArgsConstructor
public class WorkbookSubmissionQueryController implements WorkbookSubmissionQueryControllerApi {

    private final GetWorkbookSubmissionsUseCase getWorkbookSubmissionsUseCase;
    private final GetStudyGroupsForFilterUseCase getStudyGroupsForFilterUseCase;

    @Override
    @GetMapping("/workbook-submissions")
    public CursorResponse<WorkbookSubmissionResponse> getWorkbookSubmissions(
            @RequestParam(required = false) Long schoolId,
            @RequestParam Integer weekNo,
            @RequestParam(required = false) Long studyGroupId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        // TODO: 운영진 권한 필요하도록 수정
        GetWorkbookSubmissionsQuery query = new GetWorkbookSubmissionsQuery(
                schoolId, weekNo, studyGroupId, cursor, size
        );

        List<WorkbookSubmissionInfo> content = getWorkbookSubmissionsUseCase.getSubmissions(query);

        return CursorResponse.of(
                content,
                query.size(),
                WorkbookSubmissionInfo::challengerWorkbookId,
                WorkbookSubmissionResponse::from
        );
    }

    @Override
    @GetMapping("/study-groups")
    public List<StudyGroupFilterResponse> getStudyGroupsForFilter(
            @RequestParam Long schoolId,
            @RequestParam ChallengerPart part
    ) {
        // TODO: 운영진 권한 필요하도록 수정
        return getStudyGroupsForFilterUseCase.getStudyGroupsForFilter(schoolId, part).stream()
                .map(StudyGroupFilterResponse::from)
                .toList();
    }
}
