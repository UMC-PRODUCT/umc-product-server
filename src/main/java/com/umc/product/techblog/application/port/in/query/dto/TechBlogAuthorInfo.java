package com.umc.product.techblog.application.port.in.query.dto;

public record TechBlogAuthorInfo(
    Long id,
    String name,
    String nickname,
    String profileImageUrl
) {
}
