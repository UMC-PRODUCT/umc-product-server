package com.umc.product.authentication.application.port.out;

import com.umc.product.authentication.domain.MemberOAuth;

/**
 * MemberOAuth 저장 Port
 */
public interface SaveMemberOAuthPort {
    /**
     * MemberOAuth를 저장합니다.
     *
     * @param memberOAuth 저장할 MemberOAuth
     * @return 저장된 MemberOAuth
     */
    MemberOAuth save(MemberOAuth memberOAuth);

    /**
     * MemberOAuth를 삭제합니다.
     *
     * @param memberOAuth 삭제할 MemberOAuth
     */
    void delete(MemberOAuth memberOAuth);
}
