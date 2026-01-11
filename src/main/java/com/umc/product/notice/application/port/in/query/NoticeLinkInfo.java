package com.umc.product.notice.application.port.in.query;

public record NoticeLinkInfo(
        Long id,
        String url,
        Integer displayOrder
) {
}
