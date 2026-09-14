package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

import com.umc.product.curriculum.application.port.in.command.dto.curriculum.CreateWeeklyCurriculumCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateWeeklyCurriculumRequest(
    @NotNull(message = "커리큘럼 ID는 필수입니다")
    Long curriculumId,

    @NotNull(message = "주차 번호는 필수입니다")
    Long weekNo,

    boolean isExtra,

    @NotBlank(message = "제목은 필수입니다")
    String title,

    @NotNull(message = "시작 일시는 필수입니다")
    Instant startsAt,

    @NotNull(message = "종료 일시는 필수입니다")
    Instant endsAt
) {

    public CreateWeeklyCurriculumCommand toCommand() {
        return CreateWeeklyCurriculumCommand.builder()
            .curriculumId(curriculumId)
            .weekNo(weekNo)
            .isExtra(isExtra)
            .title(title)
            .startsAt(startsAt)
            .endsAt(endsAt)
            .build();
    }
}