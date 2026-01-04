package com.umc.product.form.application.port.in.query;

public record GetDraftFormResponseQuery(
        Long recruitmentId,
        Long userId
) {
}
