package com.umc.product.analytics.application.port.in.query.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;

@Builder
public record AdminOperationsSignupsInfo(List<SignupBucketInfo> signupBuckets) {

    public static AdminOperationsSignupsInfo from(List<SignupBucketInfo> signupBuckets) {
        return AdminOperationsSignupsInfo.builder()
            .signupBuckets(List.copyOf(signupBuckets))
            .build();
    }

    @Builder
    public record SignupBucketInfo(LocalDate date, long count) {

        public static SignupBucketInfo of(LocalDate date, long count) {
            return SignupBucketInfo.builder()
                .date(date)
                .count(count)
                .build();
        }
    }
}
