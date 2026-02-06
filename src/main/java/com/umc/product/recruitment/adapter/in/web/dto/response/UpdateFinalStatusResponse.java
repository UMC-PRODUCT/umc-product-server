package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.command.dto.UpdateFinalStatusResult;
import com.umc.product.recruitment.domain.enums.PartKey;

public record UpdateFinalStatusResponse(
        Long applicationId,
        FinalResultResponse finalResult
) {
    public static UpdateFinalStatusResponse from(UpdateFinalStatusResult result) {
        PartKey selectedPart = result.finalResult().selectedPart();

        FinalResultResponse.PartResponse partResponse =
                (selectedPart == null)
                        ? null
                        : new FinalResultResponse.PartResponse(selectedPart, selectedPart.getLabel());

        return new UpdateFinalStatusResponse(
                result.applicationId(),
                new FinalResultResponse(result.finalResult().decision(), partResponse)
        );
    }

    public record FinalResultResponse(
            String decision,
            PartResponse selectedPart
    ) {
        public record PartResponse(
                PartKey key,
                String label
        ) {
        }
    }
}