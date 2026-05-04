package com.umc.product.support.fixture;

import static com.umc.product.support.CommonFixture.MONKEY;

import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberFixture extends FixtureSupport {

    private final SaveMemberPort saveMemberPort;

    public MemberFixture(SaveMemberPort saveMemberPort) {
        this.saveMemberPort = saveMemberPort;
    }

    public Member 일반(String name) {
        String fixtureName = valueOrFixture(name, "member", 10);
        Member member = MONKEY.giveMeBuilder(Member.class)
            .set("name", fixtureName)
            .set("nickname", fixtureName)
            .set("email", fixtureName + "@test.com")
            .set("schoolId", null)
            .set("profileImageId", null)
            .sample();
        return saveMemberPort.save(member);
    }

    public Member 일반() {
        return 일반(fixtureString("member", 10));
    }
}
