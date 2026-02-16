package com.umc.product.community.application.port.in.report;

import java.util.Objects;

public record ReportPostCommand(
        Long postId,
        Long reporterId
) {
    public ReportPostCommand {
        Objects.requireNonNull(postId, "postId must not be null");
        Objects.requireNonNull(reporterId, "reporterId must not be null");
    }
}
