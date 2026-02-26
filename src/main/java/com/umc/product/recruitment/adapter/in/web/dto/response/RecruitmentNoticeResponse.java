package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentNoticeInfo;
import java.util.List;

public record RecruitmentNoticeResponse(
    Long recruitmentId,
    String title,
    String content,
    List<String> parts
) {
    public static RecruitmentNoticeResponse from(RecruitmentNoticeInfo info) {
        return new RecruitmentNoticeResponse(
            info.recruitmentId(),
            info.title(),
            info.content(),
            info.parts().stream().map(Enum::name).toList()
        );
    }
}
