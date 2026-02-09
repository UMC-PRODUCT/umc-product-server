package com.umc.product.organization.application.port.in.query.dto;

import com.umc.product.organization.domain.School;
import com.umc.product.organization.domain.SchoolLinkType;
import java.util.List;

public record SchoolLinkInfo(
        List<SchoolLinkItem> links
) {
    public record SchoolLinkItem(
            String title,
            SchoolLinkType type,
            String url
    ) {
    }

    public static SchoolLinkInfo from(School school) {
        List<SchoolLinkItem> items = school.getSchoolLinks().stream()
                .map(link -> new SchoolLinkItem(link.getTitle(), link.getType(), link.getUrl()))
                .toList();
        return new SchoolLinkInfo(items);
    }
}
