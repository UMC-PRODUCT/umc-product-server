package com.umc.product.project.application.port.in.query.dto;

import java.time.Instant;
import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.ProjectStatus;

public record ProjectQuotaManagementInfo(
    Long gisuId, Long chapterId, String chapterName,
    List<ChapterOption> chapters, List<SchoolOption> schools, List<Column> columns,
    List<PartSummary> summary, List<PartSummary> filteredSummary, Totals totals, Totals filteredTotals,
    int totalProjectCount, int filteredProjectCount, List<ProjectRow> projects
) {
    public enum AllocationStatus { BALANCED, EXCESS, SHORTAGE }

    public record Totals(long availableCount, long allocatedQuota, long difference,
                         AllocationStatus allocationStatus) {
    }

    public record ChapterOption(Long chapterId, String chapterName) {
    }
    public record SchoolOption(Long schoolId, String schoolName) {
    }
    public record Column(ChallengerPart part, String label, ProjectPermissionCapabilityInfo canEdit,
                         Instant editableAfter) {
                         }
    public record PartSummary(ChallengerPart part, long availableCount, long allocatedQuota,
                              long difference, AllocationStatus allocationStatus) {
                              }
    public record ProjectRow(Long projectId, String projectName, ProjectStatus status, Long schoolId, String schoolName,
                             List<Cell> cells, long totalQuota) {
                             }
    public record Cell(ChallengerPart part, long quota, long currentCount, long minimumQuota,
                       ProjectPermissionCapabilityInfo canEdit) {
                       }
}
