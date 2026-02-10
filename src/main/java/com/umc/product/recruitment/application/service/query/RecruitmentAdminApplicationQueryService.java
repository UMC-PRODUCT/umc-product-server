package com.umc.product.recruitment.application.service.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.adapter.in.web.dto.request.EvaluationDecision;
import com.umc.product.recruitment.adapter.out.dto.AdminApplicationRow;
import com.umc.product.recruitment.application.port.in.query.GetApplicationListForAdminUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationListForAdminInfo;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationListForAdminInfo.AppliedPart;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationListForAdminQuery;
import com.umc.product.recruitment.application.port.out.LoadApplicationListPort;
import com.umc.product.recruitment.application.port.out.LoadApplicationPartPreferencePort;
import com.umc.product.recruitment.domain.ApplicationPartPreference;
import com.umc.product.recruitment.domain.enums.ApplicationStatus;
import com.umc.product.recruitment.domain.enums.PartKey;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecruitmentAdminApplicationQueryService implements GetApplicationListForAdminUseCase {

    private final LoadApplicationListPort loadApplicationListPort;
    private final LoadApplicationPartPreferencePort loadApplicationPartPreferencePort;

    @Override
    public ApplicationListForAdminInfo get(GetApplicationListForAdminQuery query) {
        // todo: 총괄 권한 검증 필요
        Pageable pageable = PageRequest.of(query.page(), query.size());

        // 페이지 대상 row 조회
        Page<AdminApplicationRow> page = loadApplicationListPort.searchAdminApplications(
            query.chapterId(),
            query.schoolId(),
            query.part() == null ? "ALL" : query.part().name(),
            query.keyword(),
            pageable
        );

        List<AdminApplicationRow> rows = page.getContent();

        // appliedParts 배치 조회 (N+1 방지)
        Set<Long> applicationIds = rows.stream()
            .map(AdminApplicationRow::applicationId)
            .collect(Collectors.toSet());

        List<ApplicationPartPreference> prefs =
            loadApplicationPartPreferencePort.findAllByApplicationIdsOrderByPriorityAsc(applicationIds);

        Map<Long, List<AppliedPart>> appliedPartsByAppId =
            prefs.stream()
                .collect(Collectors.groupingBy(
                    p -> p.getApplication().getId(),
                    Collectors.mapping(p -> new ApplicationListForAdminInfo.AppliedPart(
                        p.getPriority(),
                        toPartKey(p.getRecruitmentPart().getPart()) // ChallengerPart -> PartKey
                    ), Collectors.toList())
                ));

        // 각 지원서별 priority 순 정렬
        appliedPartsByAppId.values().forEach(list ->
            list.sort(Comparator.comparingInt(ApplicationListForAdminInfo.AppliedPart::priority))
        );

        // Info rows 매핑
        List<ApplicationListForAdminInfo.ApplicationForAdminInfo> content = rows.stream()
            .map(r -> new ApplicationListForAdminInfo.ApplicationForAdminInfo(
                r.applicationId(),
                new ApplicationListForAdminInfo.Applicant(r.nickname(), r.name()),
                new ApplicationListForAdminInfo.School(r.applicantSchoolId(), r.applicantSchoolName()),
                appliedPartsByAppId.getOrDefault(r.applicationId(), List.of()),
                toFinalResult(r.applicationStatus(), r.selectedPart())
            ))
            .toList();

        // pagination info 구성
        ApplicationListForAdminInfo.PaginationInfo pageInfo = new ApplicationListForAdminInfo.PaginationInfo(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext(),
            page.hasPrevious()
        );

        ApplicationListForAdminInfo.Filters filters = new ApplicationListForAdminInfo.Filters(
            query.chapterId(),
            query.schoolId(),
            query.part() == null ? "ALL" : query.part().name(),
            query.keyword()
        );

        return new ApplicationListForAdminInfo(filters, pageInfo, content);
    }

    private ApplicationListForAdminInfo.FinalResult toFinalResult(
        ApplicationStatus status,
        ChallengerPart selectedPart
    ) {
        // PASS
        if (status == ApplicationStatus.FINAL_ACCEPTED) {
            return new ApplicationListForAdminInfo.FinalResult(
                EvaluationDecision.PASS.name(),
                selectedPart == null ? null : toPartKey(selectedPart)
            );
        }

        // FAIL
        if (status == ApplicationStatus.FINAL_REJECTED
            || status == ApplicationStatus.DOC_FAILED
            || status == ApplicationStatus.WITHDRAWN) {
            return new ApplicationListForAdminInfo.FinalResult(EvaluationDecision.FAIL.name(), null);
        }

        // WAIT (APPLIED, DOC_PASSED 등)
        return new ApplicationListForAdminInfo.FinalResult(EvaluationDecision.WAIT.name(), null);
    }

    /**
     * appliedParts / selectedPart 모두 PartKey(key/label)로 내려주기 위한 변환
     */
    private PartKey toPartKey(ChallengerPart part) {
        if (part == null) {
            return null;
        }
        return PartKey.valueOf(part.name()); // enum 이름이 동일하다는 전제
    }
}
