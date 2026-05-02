package com.umc.product.support.fixture;

import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberFixture {

    private final SaveMemberPort saveMemberPort;

    public MemberFixture(SaveMemberPort saveMemberPort) {
        this.saveMemberPort = saveMemberPort;
    }

    public Member normalMember(String name) {
        return saveMemberPort.save(Member.builder()
            .name(name)
            .nickname(name)
            .email(name + "@test.com")
            .build());
    }
}
