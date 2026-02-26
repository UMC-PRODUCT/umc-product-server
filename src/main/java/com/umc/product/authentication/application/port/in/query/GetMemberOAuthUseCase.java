package com.umc.product.authentication.application.port.in.query;

import com.umc.product.authentication.application.port.in.query.dto.MemberOAuthInfo;
import com.umc.product.common.domain.enums.OAuthProvider;

public interface GetMemberOAuthUseCase {
    MemberOAuthInfo getMemberByOAuthInfo(OAuthProvider provider, String providerId);
}
