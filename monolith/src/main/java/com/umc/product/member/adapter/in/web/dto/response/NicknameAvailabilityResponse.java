package com.umc.product.member.adapter.in.web.dto.response;

import com.umc.product.member.application.port.in.query.dto.NicknameAvailabilityInfo;

import io.swagger.v3.oas.annotations.media.Schema;

public record NicknameAvailabilityResponse(
    @Schema(description = "조회 시점의 학교 내 닉네임 사용 가능 여부")
    boolean available
) {

    public static NicknameAvailabilityResponse from(NicknameAvailabilityInfo info) {
        return new NicknameAvailabilityResponse(info.available());
    }
}
