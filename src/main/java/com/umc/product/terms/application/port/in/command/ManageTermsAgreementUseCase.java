package com.umc.product.terms.application.port.in.command;

import com.umc.product.terms.application.port.in.command.dto.CreateTermConsentCommand;

public interface ManageTermsAgreementUseCase {
    /**
     * 회원의 약관 동의 여부를 업데이트합니다.
     */
    void createTermConsent(CreateTermConsentCommand command);
}
