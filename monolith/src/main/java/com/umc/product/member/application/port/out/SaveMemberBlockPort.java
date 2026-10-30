package com.umc.product.member.application.port.out;

import com.umc.product.member.domain.MemberBlock;

public interface SaveMemberBlockPort {

    MemberBlock save(MemberBlock memberBlock);
}
