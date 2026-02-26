package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.ExtensionBaseRecruitmentsInfo;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public record ExtensionBaseRecruitmentsResponse(
    List<ExtensionBaseRecruitmentResponse> recruitments
) {
    public static ExtensionBaseRecruitmentsResponse from(ExtensionBaseRecruitmentsInfo info) {
        return new ExtensionBaseRecruitmentsResponse(
            info.recruitments().stream()
                .map(ExtensionBaseRecruitmentResponse::from)
                .toList()
        );
    }

    public record ExtensionBaseRecruitmentResponse(
        Long recruitmentId,
        String title,
        boolean isRoot,
        LocalDate startDate,
        LocalDate endDate
    ) {
        private static final ZoneId KST = ZoneId.of("Asia/Seoul");

        public static ExtensionBaseRecruitmentResponse from(
            ExtensionBaseRecruitmentsInfo.ExtensionBaseRecruitmentInfo itemInfo) {
            return new ExtensionBaseRecruitmentResponse(
                itemInfo.recruitmentId(),
                itemInfo.title(),
                itemInfo.isRoot(),
                itemInfo.applyStartAt() != null ? itemInfo.applyStartAt().atZone(KST).toLocalDate() : null,
                itemInfo.finalResultAt() != null ? itemInfo.finalResultAt().atZone(KST).toLocalDate() : null
            );
        }
    }
}
