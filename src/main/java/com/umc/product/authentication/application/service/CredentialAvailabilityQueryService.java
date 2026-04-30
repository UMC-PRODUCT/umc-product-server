package com.umc.product.authentication.application.service;

import com.umc.product.authentication.application.port.in.query.CheckCredentialAvailabilityUseCase;
import com.umc.product.authentication.domain.CredentialPolicy;
import com.umc.product.member.application.port.in.query.GetMemberCredentialUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CredentialAvailabilityQueryService implements CheckCredentialAvailabilityUseCase {

    private final GetMemberCredentialUseCase getMemberCredentialUseCase;

    @Override
    public boolean isLoginIdAvailable(String loginId) {
        // 형식이 잘못되면 사용 가능 여부 자체를 따지지 않고 INVALID_LOGIN_ID_FORMAT 예외를 던진다.
        CredentialPolicy.validateLoginId(loginId);

        return !getMemberCredentialUseCase.existsByLoginId(loginId);
    }
}
