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

    public Member 일반_멤버(String name) {
        return saveMemberPort.save(Member.builder()
            .name(name)
            .nickname(name)
            .email(name + "@test.com")
            .build());
    }

    public Member 학교_소속_멤버(String name, Long schoolId) {
        return saveMemberPort.save(Member.builder()
            .name(name)
            .nickname(name)
            .email(name + "@test.com")
            .schoolId(schoolId)
            .build());
    }
}
