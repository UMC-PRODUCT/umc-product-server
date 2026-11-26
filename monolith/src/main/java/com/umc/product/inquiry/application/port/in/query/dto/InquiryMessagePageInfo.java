package com.umc.product.inquiry.application.port.in.query.dto;

import java.util.List;

public record InquiryMessagePageInfo(
    List<InquiryMessageInfo> messages,
    Long nextCursor,
    boolean hasNext
) {
}
