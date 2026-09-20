package com.umc.product.term.application.port.in.query;

import com.umc.product.term.application.port.in.query.dto.RequiredTermConsentStatusInfo;

public interface GetRequiredTermConsentStatusUseCase {

    RequiredTermConsentStatusInfo getRequiredTermConsentStatus(Long memberId);
}
