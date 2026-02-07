package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSheetPartsInfo;
import com.umc.product.recruitment.domain.enums.PartKey;
import java.util.List;

public record GetInterviewSheetPartsResponse(
    List<PartResponse> parts
) {
    public static GetInterviewSheetPartsResponse from(GetInterviewSheetPartsInfo info) {
        return new GetInterviewSheetPartsResponse(
            info.parts().stream()
                .map(p -> new PartResponse(p))
                .toList()
        );
    }

    public record PartResponse(
        PartKey key,
        String label
    ) {
        public PartResponse(PartKey key) {
            this(key, key.getLabel());
        }
    }
}
