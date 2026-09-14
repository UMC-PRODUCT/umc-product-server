package com.umc.product.analytics.adapter.in.web.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsSignupsInfo;

import lombok.Builder;

@Builder
public record AdminOperationsSignupsResponse(List<SignupBucketResponse> signupBuckets) {

    public static AdminOperationsSignupsResponse from(AdminOperationsSignupsInfo info) {
        return AdminOperationsSignupsResponse.builder()
            .signupBuckets(info.signupBuckets().stream().map(SignupBucketResponse::from).toList())
            .build();
    }

    @Builder
    public record SignupBucketResponse(LocalDate date, long count) {

        public static SignupBucketResponse from(AdminOperationsSignupsInfo.SignupBucketInfo info) {
            return SignupBucketResponse.builder()
                .date(info.date())
                .count(info.count())
                .build();
        }
    }
}
