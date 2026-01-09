package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import java.time.LocalDate;

public record GisuResponse(Long gisuId, Long number, LocalDate startAt, LocalDate endAt, boolean isActive) {
    public static GisuResponse from(GisuInfo info) {
        return new GisuResponse(info.gisuId(), info.number(), info.startAt(), info.endAt(), info.isActive());
    }
}
