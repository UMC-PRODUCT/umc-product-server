package com.umc.product.blog.application.port.in.query.dto;

import java.time.Instant;

public record BlogSeoPathInfo(
    String type,
    String path,
    Instant updatedAt
) {
}
