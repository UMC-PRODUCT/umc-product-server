package com.umc.product.organization.application.port.in.command.dto;

public record CreateSchoolCommand(
        String schoolName,
        String remark,
        String logoImageId,
        String kakaoLink,
        String instagramLink,
        String youtubeLink
) {
}
