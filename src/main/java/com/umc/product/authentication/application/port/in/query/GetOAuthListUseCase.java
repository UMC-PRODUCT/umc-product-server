package com.umc.product.authentication.application.port.in.query;

import com.umc.product.authentication.application.port.in.query.dto.MemberOAuthInfo;
import java.util.List;

public interface GetOAuthListUseCase {
    /**
     * 회원과 연동된 OAuth 정보를 조회합니다.
     *
     * @param memberId 회원 ID
     * @return 연동된 OAuth 정보 리스트
     */
    List<MemberOAuthInfo> getOAuthList(Long memberId);
}
