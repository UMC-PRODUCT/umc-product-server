package com.umc.product.curriculum.application.service.query;

import com.umc.product.authorization.application.port.out.LoadChallengerRolePort;
import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.GetStudyGroupsForFilterUseCase;
import com.umc.product.curriculum.application.port.in.query.GetWorkbookSubmissionContextUseCase;
import com.umc.product.curriculum.application.port.in.query.GetWorkbookSubmissionsUseCase;
import com.umc.product.curriculum.application.port.in.query.dto.GetWorkbookSubmissionsQuery;
import com.umc.product.curriculum.application.port.in.query.dto.StudyGroupFilterInfo;
import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionContext;
import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionInfo;
import com.umc.product.curriculum.application.port.out.LoadWorkbookSubmissionPort;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
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
        GetWorkbookSubmissionContextUseCase,
        GetStudyGroupsForFilterUseCase {

    private final LoadWorkbookSubmissionPort loadWorkbookSubmissionPort;
    private final GetStudyGroupUseCase getStudyGroupUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final LoadChallengerRolePort loadChallengerRolePort;

    @Override
    public List<WorkbookSubmissionInfo> getSubmissions(GetWorkbookSubmissionsQuery query) {
        return loadWorkbookSubmissionPort.findSubmissions(query);
    }

    @Override
    public WorkbookSubmissionContext getContext(Long memberId) {
        // 1. 회원 정보 조회 (schoolId 획득)
        MemberInfo memberInfo = getMemberUseCase.getById(memberId);
        Long schoolId = memberInfo.schoolId();

        // 2. 현재 활성 기수 조회
        Long activeGisuId = getGisuUseCase.getActiveGisuId();

        // 3. 해당 기수에서의 역할 조회 (권한 체크는 @CheckAccess에서 처리됨)
        List<RoleAttribute> roles = loadChallengerRolePort // TODO: usecase에 의존하도록 수정되어야 한다.
            .findRolesByMemberIdAndGisuId(memberId, activeGisuId)
            .stream()
            .map(RoleAttribute::from)
            .toList();

        // 학교 운영진 역할 찾기 (@CheckAccess 통과 시 반드시 존재)
        RoleAttribute schoolAdminRole = roles.stream()
            .filter(role -> role.roleType().isSchoolAdmin())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "현재 활성 기수에서 학교 운영진 역할을 찾을 수 없습니다. 권한 검증 로직을 확인하세요."));

        // 4. 파트 제한 적용
        // 회장/부회장(isSchoolCore): 모든 파트 조회 가능 (part = null)
        // 파트장/기타 운영진: 본인 담당 파트만 조회 가능
        ChallengerPart part = schoolAdminRole.roleType().isSchoolCore()
            ? null
            : schoolAdminRole.responsiblePart();

        return new WorkbookSubmissionContext(schoolId, part);
    }

    @Override
    public List<StudyGroupFilterInfo> getStudyGroupsForFilter(Long schoolId, ChallengerPart part) {
        StudyGroupListQuery query = new StudyGroupListQuery(schoolId, part, null, 100);
        return getStudyGroupUseCase.getStudyGroups(query).stream()
                .map(info -> new StudyGroupFilterInfo(info.groupId(), info.name()))
                .toList();
    }
}
