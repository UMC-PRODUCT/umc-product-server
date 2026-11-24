package com.umc.product.support.fixture;

import static com.umc.product.support.CommonFixture.MONKEY;

import org.springframework.stereotype.Component;

import com.umc.product.member.adapter.out.persistence.MemberSystemRoleJpaRepository;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.MemberSystemRole;
import com.umc.product.member.domain.MemberSystemRoleType;

@Component
public class MemberFixture extends FixtureSupport {

    private final SaveMemberPort saveMemberPort;
    private final MemberSystemRoleJpaRepository memberSystemRoleJpaRepository;

    public MemberFixture(SaveMemberPort saveMemberPort, MemberSystemRoleJpaRepository memberSystemRoleJpaRepository) {
        this.saveMemberPort = saveMemberPort;
        this.memberSystemRoleJpaRepository = memberSystemRoleJpaRepository;
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

    /**
     * 시스템 역할은 운영에서 SQL로만 부여해 저장 Port 가 없으므로 JPA repository 로 저장한다.
     */
    public void 시스템_역할_부여(Long memberId, MemberSystemRoleType roleType) {
        memberSystemRoleJpaRepository.save(MemberSystemRole.create(memberId, roleType));
    }
}
