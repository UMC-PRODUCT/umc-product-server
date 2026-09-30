package com.umc.product.notice.application.port.in.query.dto;

public record NoticeImageInfo(
    Long id,
    String fileId,
    String url,
    Integer displayOrder
) {
}
