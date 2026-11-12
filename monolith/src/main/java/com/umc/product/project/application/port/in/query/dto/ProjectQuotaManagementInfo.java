package com.umc.product.project.application.port.in.query.dto;

import java.time.Instant;
import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.ProjectStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProjectQuotaManagementInfo(
    Long gisuId, Long chapterId, String chapterName,
    List<ChapterOption> chapters, List<SchoolOption> schools, List<Column> columns,
    List<PartSummary> summary, List<PartSummary> filteredSummary, Totals totals, Totals filteredTotals,
    int totalProjectCount, int filteredProjectCount, List<ProjectRow> projects
) {
    /**
     * @deprecated 지원 중단: 대체 없음. 삭제 예정: 2026-12-13 릴리스, 담당자: wlgusqkr
     */
    @Deprecated(since = "2026-11-12", forRemoval = true)
    public enum AllocationStatus { BALANCED, EXCESS, SHORTAGE }

    public record Totals(
        @Deprecated(since = "2026-11-12", forRemoval = true)
        @Schema(deprecated = true,
            description = "지부 파트 인원 합계. 지원 중단: 대체 없음. 삭제 예정: 2026-12-13 릴리스, 담당자: wlgusqkr")
        long availableCount,
        long allocatedQuota,
        @Deprecated(since = "2026-11-12", forRemoval = true)
        @Schema(deprecated = true,
            description = "allocatedQuota - availableCount. 지원 중단: 대체 없음. 삭제 예정: 2026-12-13 릴리스, 담당자: wlgusqkr")
        long difference,
        @Deprecated(since = "2026-11-12", forRemoval = true)
        @Schema(deprecated = true,
            description = "difference의 부호. 지원 중단: 대체 없음. 삭제 예정: 2026-12-13 릴리스, 담당자: wlgusqkr")
        AllocationStatus allocationStatus
    ) {
    }

    public record ChapterOption(Long chapterId, String chapterName) {
    }
    public record SchoolOption(Long schoolId, String schoolName) {
    }
    public record Column(ChallengerPart part, String label, ProjectPermissionCapabilityInfo canEdit,
                         Instant editableAfter) {
                         }
    public record PartSummary(
        ChallengerPart part,
        @Deprecated(since = "2026-11-12", forRemoval = true)
        @Schema(deprecated = true,
            description = "지부 파트 인원. 지원 중단: 대체 없음. 삭제 예정: 2026-12-13 릴리스, 담당자: wlgusqkr")
        long availableCount,
        long allocatedQuota,
        @Deprecated(since = "2026-11-12", forRemoval = true)
        @Schema(deprecated = true,
            description = "allocatedQuota - availableCount. 지원 중단: 대체 없음. 삭제 예정: 2026-12-13 릴리스, 담당자: wlgusqkr")
        long difference,
        @Deprecated(since = "2026-11-12", forRemoval = true)
        @Schema(deprecated = true,
            description = "difference의 부호. 지원 중단: 대체 없음. 삭제 예정: 2026-12-13 릴리스, 담당자: wlgusqkr")
        AllocationStatus allocationStatus
    ) {
    }
    public record ProjectRow(Long projectId, String projectName, ProjectStatus status, Long schoolId, String schoolName,
                             List<Cell> cells, long totalQuota) {
                             }
    public record Cell(ChallengerPart part, long quota, long currentCount, long minimumQuota, long resetQuota,
                       ProjectPermissionCapabilityInfo canEdit) {
                       }
}
