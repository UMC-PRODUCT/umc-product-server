package com.umc.product.member.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MemberEmailLookupRequest(
    @NotNull @Positive Long schoolId,
    @NotBlank @Size(max = 30) String name,
    @NotBlank @Pattern(regexp = "[가-힣]{1,4}") String nickname
) {
}
