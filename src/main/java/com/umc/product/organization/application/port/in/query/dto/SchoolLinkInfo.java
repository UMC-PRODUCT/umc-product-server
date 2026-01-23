package com.umc.product.organization.application.port.in.query.dto;

import com.umc.product.organization.domain.School;

public record SchoolLinkInfo(
        String kakaoLink,
        String instagramLink,
        String youtubeLink
) {
    public static SchoolLinkInfo from(School school) {
        return new SchoolLinkInfo(school.getKakaoLink(), school.getInstagramLink(), school.getYoutubeLink());
    }
}
