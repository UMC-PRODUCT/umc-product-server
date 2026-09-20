package com.umc.product.blog.application.port.in.query.dto;

public record BlogAuthorInfo(
    Long id,
    String name,
    String nickname,
    String profileImageUrl
) {
}
