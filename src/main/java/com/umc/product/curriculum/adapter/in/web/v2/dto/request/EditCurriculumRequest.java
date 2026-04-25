package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

import jakarta.validation.constraints.NotBlank;

public record EditCurriculumRequest(
    @NotBlank(message = "수정할 제목은 필수입니다.")
    String title
) {

}
