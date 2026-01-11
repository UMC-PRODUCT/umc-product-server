package com.umc.product.recruitment.application.port.in.command;

public record CreateRecruitmentCommand(
        Long schoolId,
        Long gisuId,
        String title,
        String description
) {
}
