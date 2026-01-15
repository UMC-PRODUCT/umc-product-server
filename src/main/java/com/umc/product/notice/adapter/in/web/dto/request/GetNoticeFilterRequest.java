package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.query.dto.NoticeSearchConditionInfo;
import com.umc.product.notice.domain.enums.NoticeClassification;

public record GetNoticeFilterRequest(
        NoticeClassification category,
        String keyword
) {

    public NoticeSearchConditionInfo toInfo() {
        return new NoticeSearchConditionInfo(category, keyword);
    }

}
