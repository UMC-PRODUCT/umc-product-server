package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateChallengerRecordRequest(
    @NotNull(message = "기수 ID는 필수입니다") Long gisuId,
    @NotNull(message = "지부 ID는 필수입니다") Long chapterId,
    @NotNull(message = "학교 ID는 필수입니다") Long schoolId,
    @NotNull(message = "챌린저 파트는 필수입니다") ChallengerPart part,
    @NotBlank(message = "회원 이름은 필수입니다") @Size(max = 30, message = "회원 이름은 30자 이하여야 합니다") String memberName,
    ChallengerRoleType challengerRoleType
) {
}
