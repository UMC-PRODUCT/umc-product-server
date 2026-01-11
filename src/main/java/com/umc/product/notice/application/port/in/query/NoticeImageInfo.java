package com.umc.product.notice.application.port.in.query;

public record NoticeImageInfo(
        Long id,
        String url,
        Integer displayOrder
) {
}
