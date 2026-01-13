package com.umc.product.member.application.port.out;

import com.umc.product.authentication.domain.MemberOAuth;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.MemberTermAgreement;
import java.util.List;

public interface SaveMemberPort {
    Member save(Member member);

    MemberOAuth saveOAuth(MemberOAuth memberOAuth);

    List<MemberTermAgreement> saveTermAgreements(List<MemberTermAgreement> agreements);

    void delete(Member member);
}
