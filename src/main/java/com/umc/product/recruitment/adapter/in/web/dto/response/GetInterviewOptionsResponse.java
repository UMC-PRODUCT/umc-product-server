package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewOptionsInfo;
import java.time.LocalDate;
import java.util.List;

public record GetInterviewOptionsResponse(
    List<LocalDate> dates,
    List<PartResponse> parts
) {
    public static GetInterviewOptionsResponse from(GetInterviewOptionsInfo info) {
        return new GetInterviewOptionsResponse(
            info.dates(),
            info.parts().stream().map(p -> new PartResponse(p, p.getLabel())).toList()
        );
    }


    public record PartResponse(
        PartOption key,
        String label
    ) {
    }
}
