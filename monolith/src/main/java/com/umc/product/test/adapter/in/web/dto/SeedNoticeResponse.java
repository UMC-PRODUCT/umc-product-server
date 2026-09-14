package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.test.application.port.in.command.dto.SeedNoticeResult;
import java.util.List;

public record SeedNoticeResponse(
    Long gisuId,
    Long authorMemberId,
    List<Long> createdNoticeIds,
    List<ScopeSummary> scopeBreakdown,
    int totalCreated,
    int totalFailed,
    boolean skipped,
    String reason
) {

    public record ScopeSummary(
        String scope,
        int attempted,
        int created,
        int failed
    ) {

        public static ScopeSummary from(SeedNoticeResult.ScopeSummary src) {
            return new ScopeSummary(src.scope(), src.attempted(), src.created(), src.failed());
        }
    }

    public static SeedNoticeResponse from(SeedNoticeResult result) {
        return new SeedNoticeResponse(
            result.gisuId(),
            result.authorMemberId(),
            result.createdNoticeIds(),
            result.scopeBreakdown().stream().map(ScopeSummary::from).toList(),
            result.totalCreated(),
            result.totalFailed(),
            result.skipped(),
            result.reason()
        );
    }
}
