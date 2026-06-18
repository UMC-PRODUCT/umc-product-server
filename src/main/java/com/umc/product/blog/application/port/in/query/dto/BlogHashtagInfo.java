package com.umc.product.blog.application.port.in.query.dto;

public record BlogHashtagInfo(
    Long id,
    String name,
    String slug,
    int contentCount
) {
}
