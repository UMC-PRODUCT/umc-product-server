package com.umc.product.challenger.application.port.in.query.dto;

import java.time.Instant;
import java.util.List;

import com.umc.product.challenger.domain.enums.ChallengerDeactivationHistoryAction;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;

public record ChallengerDeactivationDetailInfo(
    Long deactivationId,
    ChallengerInfo challenger,
    ChallengerStatus status,
    ChallengerDeactivationReason reason,
    String reasonName,
    String reasonDetail,
    String memo,
    Instant registeredAt,
    ActorInfo registeredBy,
    List<EvidenceImageInfo> images,
    List<String> links,
    List<HistoryInfo> histories
) {
    public record ChallengerInfo(
        Long challengerId,
        String name,
        String nickname,
        Long gisuId,
        Long generation,
        Long schoolId,
        String schoolName,
        String chapterName,
        ChallengerPart part
    ) {
    }

    public record ActorInfo(Long memberId, String name, String nickname) {
    }

    public record EvidenceImageInfo(String imageUrl, String originalFileName) {
    }

    public record HistoryInfo(
        Long historyId,
        ChallengerDeactivationHistoryAction action,
        Instant createdAt,
        ActorInfo actor,
        int imageCount,
        int linkCount
    ) {
    }
}
