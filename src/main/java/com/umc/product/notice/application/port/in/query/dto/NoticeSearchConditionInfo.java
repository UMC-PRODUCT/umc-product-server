package com.umc.product.notice.application.port.in.query.dto;

import com.umc.product.notice.domain.enums.NoticeClassification;

public record NoticeSearchConditionInfo(
        NoticeClassification category
) {
}
