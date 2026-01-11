package com.umc.product.community.domain.vo;

import java.time.LocalDateTime;

/**
 * 번개 모임 정보 VO
 */
public record LightningInfo(
        LocalDateTime meetAt,
        String location,
        Integer maxParticipants
) {
    public LightningInfo {
        if (meetAt == null) {
            throw new IllegalArgumentException("모임 시간은 필수입니다.");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("모임 장소는 필수입니다.");
        }
        if (maxParticipants == null || maxParticipants <= 0) {
            throw new IllegalArgumentException("최대 참가자는 1명 이상이어야 합니다.");
        }
    }

    public static LightningInfo of(LocalDateTime meetAt, String location, Integer maxParticipants) {
        return new LightningInfo(meetAt, location, maxParticipants);
    }
}
