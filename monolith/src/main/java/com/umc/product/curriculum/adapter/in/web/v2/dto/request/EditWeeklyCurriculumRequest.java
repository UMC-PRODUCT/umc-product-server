package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

import com.umc.product.curriculum.application.port.in.command.dto.curriculum.EditWeeklyCurriculumCommand;

import java.time.Instant;

public record EditWeeklyCurriculumRequest(
    Long weekNo,
    Boolean isExtra,
    String title,
    Instant startsAt,
    Instant endsAt
) {

    public EditWeeklyCurriculumCommand toCommand(Long weeklyCurriculumId) {
        return EditWeeklyCurriculumCommand.builder()
            .weeklyCurriculumId(weeklyCurriculumId)
            .weekNo(weekNo)
            .isExtra(isExtra)
            .title(title)
            .startsAt(startsAt)
            .endsAt(endsAt)
            .build();
    }
}
