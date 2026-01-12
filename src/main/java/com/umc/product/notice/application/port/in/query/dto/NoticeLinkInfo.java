package com.umc.product.notice.application.port.in.query.dto;

public record NoticeLinkInfo(
        Long id,
        String url,
        Integer displayOrder
) {
}
