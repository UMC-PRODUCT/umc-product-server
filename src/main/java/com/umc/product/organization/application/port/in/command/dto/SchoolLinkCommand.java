package com.umc.product.organization.application.port.in.command.dto;

import com.umc.product.organization.domain.School;
import com.umc.product.organization.domain.SchoolLink;
import com.umc.product.organization.domain.SchoolLinkType;

public record SchoolLinkCommand(
        String title,
        SchoolLinkType type,
        String url
) {
    public SchoolLink toEntity(School school) {
        return SchoolLink.create(school, title, type, url);
    }
}
