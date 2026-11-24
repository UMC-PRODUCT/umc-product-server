package com.umc.product.challenger.application.port.in.query.dto;

import java.time.LocalDate;

import com.umc.product.common.domain.enums.ChallengerStatus;

public record ChallengerReadmissionInfo(boolean hasHistory, boolean restricted, HistoryInfo mostRecent) {

    public record HistoryInfo(Long generation, ChallengerStatus status, LocalDate registeredDate) {
    }
}
