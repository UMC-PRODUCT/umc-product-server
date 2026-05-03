package com.umc.product.member;


import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.umc.product.member.domain.Member;
import com.umc.product.support.CommonFixture;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MemberMonkeyTest {
    @DisplayName("Fixture Monkey 동작 테스트")
    @Test
    void Fixture_Monkey_동작_테스트() {
        List<Member> members = CommonFixture.MONKEY
            .giveMe(Member.class, 1000);

        members
            .forEach(m -> System.out.println(m.getName()));
    }

    @DisplayName("이메일은 고정으로 두고 테스트")
    @Test
    void 이메일은_고정으로_두고_테스트() {
        // given
        ArbitraryBuilder<Member> memberArbitraryBuilder = CommonFixture.MONKEY.giveMeBuilder(Member.class);

        memberArbitraryBuilder.sampleStream()
            .forEach(m -> System.out.println(m.getName()));
    }
}
