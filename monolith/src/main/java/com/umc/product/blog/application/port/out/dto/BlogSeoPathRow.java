package com.umc.product.blog.application.port.out.dto;

import java.time.Instant;

public record BlogSeoPathRow(
    String type,
    String path,
    Instant updatedAt
) {
}
