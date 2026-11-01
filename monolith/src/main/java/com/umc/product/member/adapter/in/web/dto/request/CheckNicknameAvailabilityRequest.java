package com.umc.product.member.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CheckNicknameAvailabilityRequest(
    @Schema(description = "중복을 확인할 학교 ID", example = "1")
    @NotNull(message = "학교 ID는 필수입니다") @Positive(message = "학교 ID는 양수여야 합니다") Long schoolId,

    @Schema(description = "공백 없는 한글 1~6자 닉네임", example = "토리")
    @NotBlank(message = "닉네임은 필수입니다") @Size(min = 1, max = 6, message = "닉네임은 한글 1~6자여야 합니다") @Pattern(regexp = "^[가-힣]+$", message = "공백 없이 한글만 입력 가능합니다") String nickname
) {
}
