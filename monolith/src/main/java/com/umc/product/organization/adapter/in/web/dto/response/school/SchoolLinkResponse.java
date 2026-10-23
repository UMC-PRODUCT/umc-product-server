package com.umc.product.organization.adapter.in.web.dto.response.school;

import java.util.List;

import com.umc.product.organization.application.port.in.query.dto.school.SchoolLinkInfo;
import com.umc.product.organization.domain.enums.SchoolLinkType;

public record SchoolLinkResponse(
    List<SchoolLinkItem> links
) {
    public static SchoolLinkResponse of(SchoolLinkInfo schoolLinkInfo) {
        List<SchoolLinkItem> items = schoolLinkInfo.links().stream()
            .map(link -> new SchoolLinkItem(link.title(), link.type(), link.url()))
            .toList();
        return new SchoolLinkResponse(items);
    }

    public record SchoolLinkItem(
        String title,
        SchoolLinkType type,
        String url
    ) {
    }
}
