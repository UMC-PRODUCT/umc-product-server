package com.umc.product.organization.application.port.in.command.dto;

public record UpdateSchoolCommand(
        String schoolName,
        Long chapterId,
        String remark,
        String logoImageId,
        String kakaoLink,
        String instagramLink,
        String youtubeLink
) {

}
