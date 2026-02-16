package com.umc.product.community.application.port.in.report;

import java.util.Objects;

public record ReportCommentCommand(
        Long commentId,
        Long reporterId
) {
    public ReportCommentCommand {
        Objects.requireNonNull(commentId, "commentId must not be null");
        Objects.requireNonNull(reporterId, "reporterId must not be null");
    }
}
