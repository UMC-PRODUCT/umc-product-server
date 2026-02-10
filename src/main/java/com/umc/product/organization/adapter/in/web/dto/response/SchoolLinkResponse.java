package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.SchoolLinkInfo;
import com.umc.product.organization.domain.SchoolLinkType;
import java.util.List;

public record SchoolLinkResponse(
        List<SchoolLinkItem> links
) {
    public record SchoolLinkItem(
            String title,
            SchoolLinkType type,
            String url
    ) {
    }

    public static SchoolLinkResponse of(SchoolLinkInfo schoolLinkInfo) {
        List<SchoolLinkItem> items = schoolLinkInfo.links().stream()
                .map(link -> new SchoolLinkItem(link.title(), link.type(), link.url()))
                .toList();
        return new SchoolLinkResponse(items);
    }
}
