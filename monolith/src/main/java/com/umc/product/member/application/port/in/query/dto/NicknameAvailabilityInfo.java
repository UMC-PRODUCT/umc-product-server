package com.umc.product.member.application.port.in.query.dto;

public record NicknameAvailabilityInfo(boolean available) {

    public static NicknameAvailabilityInfo of(boolean available) {
        return new NicknameAvailabilityInfo(available);
    }
}
