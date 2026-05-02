package com.umc.product.support.fixture;

import com.umc.product.member.application.port.out.SaveMemberProfilePort;
import com.umc.product.member.domain.LinkTypeAndLink;
import com.umc.product.member.domain.MemberProfile;
import com.umc.product.member.domain.MemberProfileLinkType;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 회원 프로필(MemberProfile) 테스트용 Fixture.
 *
 * <p>{@code Member} 와의 결합은 {@code Member#assignProfile} 로 이루어지므로,
 * 본 Fixture 는 프로필 자체의 영속화만 책임진다. 회원과 연결이 필요하다면 호출 측에서 명시적으로 수행한다.</p>
 */
@Component
public class MemberProfileFixture {

    private final SaveMemberProfilePort saveMemberProfilePort;

    public MemberProfileFixture(SaveMemberProfilePort saveMemberProfilePort) {
        this.saveMemberProfilePort = saveMemberProfilePort;
    }

    /**
     * 모든 링크가 비어있는 빈 프로필.
     */
    public MemberProfile 빈_프로필() {
        return saveMemberProfilePort.save(MemberProfile.fromLinks(List.of()));
    }

    /**
     * GitHub 링크만 가진 프로필.
     */
    public MemberProfile 깃허브_프로필(String githubUrl) {
        return saveMemberProfilePort.save(MemberProfile.fromLinks(
            List.of(new LinkTypeAndLink(MemberProfileLinkType.GITHUB, githubUrl))
        ));
    }

    /**
     * 임의의 링크 목록을 가진 프로필.
     */
    public MemberProfile 프로필(List<LinkTypeAndLink> links) {
        return saveMemberProfilePort.save(MemberProfile.fromLinks(links));
    }
}
