package com.umc.product.curriculum.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.adapter.in.web.dto.response.StudyGroupFilterResponse;
import com.umc.product.curriculum.adapter.in.web.dto.response.WorkbookSubmissionResponse;
import com.umc.product.curriculum.application.port.in.query.GetStudyGroupsForFilterUseCase;
import com.umc.product.curriculum.application.port.in.query.GetWorkbookSubmissionsUseCase;
import com.umc.product.curriculum.application.port.in.query.dto.GetWorkbookSubmissionsQuery;
import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionInfo;
import com.umc.product.global.response.CursorResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.organization.application.port.in.query.GetSchoolAccessContextUseCase;
import com.umc.product.organization.application.port.in.query.dto.SchoolAccessContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/curriculums")
@RequiredArgsConstructor
public class WorkbookQueryController implements WorkbookQueryControllerApi {

    private final GetWorkbookSubmissionsUseCase getWorkbookSubmissionsUseCase;
    private final GetSchoolAccessContextUseCase getSchoolAccessContextUseCase;
    private final GetStudyGroupsForFilterUseCase getStudyGroupsForFilterUseCase;

    @Override
    @GetMapping("/workbook-submissions")
    @CheckAccess(
        resourceType = ResourceType.WORKBOOK_SUBMISSION,
        permission = PermissionType.READ,
        message = "워크북 제출 현황 조회는 학교 운영진만 가능합니다."
    )
    public CursorResponse<WorkbookSubmissionResponse> getWorkbookSubmissions(
            @CurrentMember MemberPrincipal memberPrincipal,
            @RequestParam Integer weekNo,
            @RequestParam(required = false) Long studyGroupId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        // 사용자 역할에 따른 조회 컨텍스트 추출 (schoolId, part)
        SchoolAccessContext context = getSchoolAccessContextUseCase.getContext(
                memberPrincipal.getMemberId()
        );

        GetWorkbookSubmissionsQuery query = new GetWorkbookSubmissionsQuery(
                context.schoolId(),
                weekNo,
                studyGroupId,
                context.part(),
                cursor,
                size
        );

        List<WorkbookSubmissionInfo> content = getWorkbookSubmissionsUseCase.getSubmissions(query);

        return CursorResponse.of(
                content,
                size,
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
