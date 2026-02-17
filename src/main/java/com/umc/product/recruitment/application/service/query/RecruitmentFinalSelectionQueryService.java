package com.umc.product.recruitment.application.service.query;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.recruitment.adapter.out.dto.FinalSelectionListItemProjection;
import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.application.port.in.SortOption;
import com.umc.product.recruitment.application.port.in.query.GetFinalSelectionListUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.FinalSelectionApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetFinalSelectionApplicationListQuery;
import com.umc.product.recruitment.application.port.out.LoadApplicationListPort;
import com.umc.product.recruitment.application.port.out.LoadApplicationPartPreferencePort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPort;
import com.umc.product.recruitment.domain.ApplicationPartPreference;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.enums.PartKey;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentFinalSelectionQueryService implements GetFinalSelectionListUseCase {

    private final LoadApplicationListPort loadApplicationListPort;
    private final LoadApplicationPartPreferencePort loadApplicationPartPreferencePort;
    private final LoadRecruitmentPort loadRecruitmentPort;

    @Override
    public FinalSelectionApplicationListInfo get(GetFinalSelectionApplicationListQuery query) {
        // todo: 운영진 권한 및 학교 체크
        // todo: 최종 선발 기간/조건 검증

        Recruitment recruitment = loadRecruitmentPort.findById(query.recruitmentId())
            .orElseThrow(() -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));
        Long rootId = recruitment.getEffectiveRootId();

        Long recruitmentId = query.recruitmentId();
        PartOption part = query.part();
        SortOption sort = query.sort();

        Pageable pageable = PageRequest.of(query.page(), query.size());

        // summary
        FinalSelectionApplicationListInfo.Summary summary =
            loadApplicationListPort.getFinalSelectionSummary(rootId, query.part().name());

        // page 조회
        Page<FinalSelectionListItemProjection> page =
            loadApplicationListPort.searchFinalSelections(rootId, part.name(), query.sort().name(), pageable);

        Set<Long> applicationIds = page.getContent().stream()
            .map(FinalSelectionListItemProjection::applicationId)
            .collect(Collectors.toSet());

        if (applicationIds.isEmpty()) {
            return new FinalSelectionApplicationListInfo(
                summary,
                sort.name(),
                List.of(),
                new FinalSelectionApplicationListInfo.PaginationInfo(
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalPages(),
                    page.getTotalElements(),
                    page.hasNext(),
                    page.hasPrevious()
                )
            );
        }

        // appliedParts
        Map<Long, List<ApplicationPartPreference>> prefMap =
            loadApplicationPartPreferencePort.findAllByApplicationIdsOrderByPriorityAsc(applicationIds)
                .stream()
                .collect(Collectors.groupingBy(p -> p.getApplication().getId()));

        // scores
        Map<Long, BigDecimal> avgDocScoreMap =
            loadApplicationListPort.calculateAvgDocScoreByApplicationIds(applicationIds);

        Map<Long, BigDecimal> avgInterviewScoreMap =
            loadApplicationListPort.calculateAvgInterviewScoreByApplicationIds(applicationIds);

        List<FinalSelectionApplicationListInfo.FinalSelectionApplicationInfo> items =
            page.getContent().stream()
                .map(p -> toFinalSelectionItem(p, prefMap, avgDocScoreMap, avgInterviewScoreMap))
                .toList();

        return new FinalSelectionApplicationListInfo(
            summary,
            sort.name(),
            items,
            new FinalSelectionApplicationListInfo.PaginationInfo(
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext(),
                page.hasPrevious()
            )
        );
    }

    private FinalSelectionApplicationListInfo.FinalSelectionApplicationInfo toFinalSelectionItem(
        FinalSelectionListItemProjection p,
        Map<Long, List<ApplicationPartPreference>> prefMap,
        Map<Long, BigDecimal> avgDocScoreMap,
        Map<Long, BigDecimal> avgInterviewScoreMap
    ) {
        Long applicationId = p.applicationId();

        // appliedParts
        List<ApplicationPartPreference> prefs = prefMap.getOrDefault(applicationId, List.of());
        List<FinalSelectionApplicationListInfo.AppliedPartInfo> appliedParts = prefs.stream()
            .map(pref -> new FinalSelectionApplicationListInfo.AppliedPartInfo(
                pref.getPriority(),
                PartKey.valueOf(pref.getRecruitmentPart().getPart().name())
            ))
            .toList();

        BigDecimal doc = avgDocScoreMap.get(applicationId);
        BigDecimal interview = avgInterviewScoreMap.get(applicationId);

        Double documentScore = (doc == null) ? null : doc.doubleValue();
        Double interviewScore = (interview == null) ? null : interview.doubleValue();

        Double finalScore = null;
        if (doc != null && interview != null) {
            finalScore = doc.add(interview)
                .divide(BigDecimal.valueOf(2), 1, RoundingMode.HALF_UP)
                .doubleValue();
        }

        // selection mapping
        String status;
        PartKey selectedPart = null;

        switch (p.status()) {
            case FINAL_ACCEPTED -> {
                status = "PASS";
                selectedPart = (p.selectedPart() == null) ? null : PartKey.valueOf(p.selectedPart().name());
            }
            case FINAL_REJECTED -> {
                status = "FAIL";
                selectedPart = null;
            }
            case DOC_PASSED -> {
                status = "WAIT";
                selectedPart = null;
            }
            default -> {
                log.error("[FinalSelection] unexpected status. appId={}, status={}", applicationId, p.status());
                status = "WAIT";
                selectedPart = null;
            }
        }

        return new FinalSelectionApplicationListInfo.FinalSelectionApplicationInfo(
            applicationId,
            new FinalSelectionApplicationListInfo.ApplicantInfo(p.nickname(), p.name()),
            appliedParts,
            documentScore,
            interviewScore,
            finalScore,
            new FinalSelectionApplicationListInfo.Selection(status, selectedPart)
        );
    }
}
