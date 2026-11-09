package com.umc.product.test.adapter.in.web.dto;

import java.util.List;

import com.umc.product.test.domain.rehearsal.RoundSummary;
import com.umc.product.test.domain.rehearsal.RoundSummary.Header;
import com.umc.product.test.domain.rehearsal.RoundSummary.PartHeadcount;
import com.umc.product.test.domain.rehearsal.RoundSummary.ProjectRow;
import com.umc.product.test.domain.rehearsal.RoundSummary.ReservedRow;

public record RoundSummaryResponse(
    Header round,
    List<ProjectRow> projects,
    List<PartHeadcount> parts,
    List<ReservedRow> qaReserved,
    List<Long> underfilledProjects,
    List<Long> projectsWithoutApplicants
) {
    public static RoundSummaryResponse from(RoundSummary summary) {
        return new RoundSummaryResponse(summary.round(), summary.projects(), summary.parts(), summary.qaReserved(),
            summary.underfilledProjects(), summary.projectsWithoutApplicants());
    }
}
