package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.SchoolLinkInfo;

public record SchoolLinkResponse(
        String kakaoLink,
        String instagramLink,
        String youtubeLink
) {
    public static SchoolLinkResponse of(SchoolLinkInfo schoolLinkInfo) {
        return new SchoolLinkResponse(
                schoolLinkInfo.kakaoLink(),
                schoolLinkInfo.instagramLink(),
                schoolLinkInfo.youtubeLink()
        );
    }
}
