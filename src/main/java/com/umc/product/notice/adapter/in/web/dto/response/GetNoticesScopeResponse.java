package com.umc.product.notice.adapter.in.web.dto.response;

import com.umc.product.notice.application.port.in.query.dto.NoticeScopeInfo;
import java.util.List;

public record GetNoticesScopeResponse(
    List<NoticeScopeInfo> filters
) {
}
