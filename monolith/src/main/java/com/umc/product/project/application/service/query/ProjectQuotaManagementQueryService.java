package com.umc.product.project.application.service.query;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.project.application.access.ProjectQuotaAccessPolicy;
import com.umc.product.project.application.port.in.query.GetProjectQuotaManagementUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo.AllocationStatus;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo.Cell;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo.ChapterOption;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo.Column;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo.PartSummary;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo.ProjectRow;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo.SchoolOption;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo.Totals;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.service.policy.ProjectQuotaEditPolicy;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectQuotaManagementQueryService implements GetProjectQuotaManagementUseCase {
    private final ProjectQuotaAccessPolicy accessPolicy;
    private final ProjectQuotaEditPolicy editPolicy;
    private final ProjectQuotaManagementDataLoader dataLoader;
    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    private final GetSchoolUseCase getSchoolUseCase;
    private final Clock clock;

    @Override
    public ProjectQuotaManagementInfo get(Long memberId, Long gisuId, Long chapterId, String keyword, Long schoolId) {
        var chapters = accessPolicy.accessibleChapters(memberId, gisuId);
        List<ChapterOption> options = chapters.stream()
            .map(chapter -> new ChapterOption(chapter.chapterId(), chapter.chapterName())).toList();
        if (chapters.isEmpty()) {
            if (chapterId != null) {
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
            }
            return new ProjectQuotaManagementInfo(gisuId, null, null, options,
                List.of(), List.of(), List.of(), List.of(), totals(List.of()), totals(List.of()), 0, 0, List.of());
        }
        var selected = chapterId == null ? chapters.getFirst() : chapters.stream()
            .filter(chapter -> Objects.equals(chapter.chapterId(), chapterId)).findFirst()
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED));
        List<SchoolOption> schools = selected.schools().stream()
            .map(school -> new SchoolOption(school.schoolId(), school.schoolName())).toList();
        if (schoolId != null && schools.stream().noneMatch(school -> Objects.equals(school.schoolId(), schoolId))) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_PART_QUOTA_INVALID_REQUEST);
        }
        var snapshot = dataLoader.load(gisuId, selected.chapterId());
        List<Project> filtered = (keyword == null || keyword.isBlank()) && schoolId == null ? snapshot.projects()
            : dataLoader.projects(gisuId, selected.chapterId(), keyword, schoolId);
        var allocated = snapshot.allocated(snapshot.projects());
        List<ChallengerPart> parts = Arrays.stream(ChallengerPart.values())
            .filter(part -> part != ChallengerPart.ADMIN)
            .filter(part -> part.isSelectable()
                || snapshot.population().containsKey(part) || allocated.containsKey(part))
            .sorted(Comparator.comparingInt(ChallengerPart::getSortOrder)).toList();
        var rounds = loadProjectMatchingRoundPort.listByChapterId(selected.chapterId());
        var now = clock.instant();
        List<Column> columns = parts.stream().map(part -> {
            var window = editPolicy.evaluate(part, rounds, now);
            return new Column(part, columnLabel(part),
                window.capability(), window.editableAfter());
        }).toList();
        var columnsByPart = columns.stream().collect(Collectors.toMap(Column::part, Function.identity()));
        Set<Long> schoolIds = snapshot.projects().stream().map(Project::getProductOwnerSchoolId)
            .collect(Collectors.toSet());
        // 학교명만 조회해 활성 기수의 중복 지부 연결이나 로고 파일 상태에 영향을 받지 않도록 한다.
        Map<Long, String> schoolNames = schoolIds.isEmpty() ? Map.of() : getSchoolUseCase.getAllSchoolNames().stream()
            .filter(school -> schoolIds.contains(school.schoolId()))
            .collect(Collectors.toMap(school -> school.schoolId(), school -> school.schoolName()));
        List<ProjectRow> rows = new ArrayList<>();
        for (Project project : filtered) {
            List<Cell> cells = parts.stream().map(part -> new Cell(part, snapshot.quota(project, part),
                snapshot.current(project, part), snapshot.minimum(project, part), columnsByPart.get(part).canEdit()))
                .toList();
            rows.add(new ProjectRow(project.getId(), project.getName(), project.getStatus(),
                project.getProductOwnerSchoolId(), schoolNames.get(project.getProductOwnerSchoolId()), cells,
                cells.stream().mapToLong(Cell::quota).reduce(0L, Math::addExact)));
        }
        var summary = summary(parts, snapshot.population(), allocated);
        var filteredSummary = summary(parts, snapshot.population(), snapshot.allocated(filtered));
        return new ProjectQuotaManagementInfo(gisuId, selected.chapterId(), selected.chapterName(), options,
            schools, columns, summary, filteredSummary, totals(summary), totals(filteredSummary),
            snapshot.projects().size(), filtered.size(), rows);
    }

    private String columnLabel(ChallengerPart part) {
        return switch (part) {
            case PLAN -> "PM";
            case DESIGN -> "Design";
            case WEB_PRODUCT_ENGINEER -> "Web PE";
            case MOBILE_PRODUCT_ENGINEER -> "Mobile PE";
            default -> part.getDisplayName();
        };
    }

    private Totals totals(List<PartSummary> summaries) {
        long available = summaries.stream().mapToLong(PartSummary::availableCount).reduce(0L, Math::addExact);
        long allocated = summaries.stream().mapToLong(PartSummary::allocatedQuota).reduce(0L, Math::addExact);
        long difference = Math.subtractExact(allocated, available);
        return new Totals(available, allocated, difference,
            difference == 0 ? AllocationStatus.BALANCED
                : difference > 0 ? AllocationStatus.EXCESS : AllocationStatus.SHORTAGE);
    }

    private List<PartSummary> summary(List<ChallengerPart> parts, Map<ChallengerPart, Long> population,
        Map<ChallengerPart, Long> allocated) {
        return parts.stream().map(part -> {
            long available = population.getOrDefault(part, 0L);
            long quota = allocated.getOrDefault(part, 0L);
            long difference = Math.subtractExact(quota, available);
            return new PartSummary(part, available, quota, difference,
                difference == 0 ? AllocationStatus.BALANCED
                    : difference > 0 ? AllocationStatus.EXCESS : AllocationStatus.SHORTAGE);
        }).toList();
    }
}
