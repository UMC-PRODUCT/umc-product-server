package com.umc.product.member.application.port.in.query.dto;

import com.umc.product.member.domain.MemberProfile;
import com.umc.product.member.domain.MemberProfileLinkType;

import lombok.Builder;

/**
 * 회원의 프로필 정보를 나타내는 DTO
 * <p>
 * 회원 자체의 ID나 프로필 이미지 등은 {@link MemberInfo}에 있습니다. 여기는 프로필 링크나 부가적인, 조회할 일이 적은 정보들을 포함합니다.
 */
@Builder
public record MemberProfileInfo(
    Long id,
    String linkedIn,
    String instagram,
    String github,
    String blog,
    String personal
) {
    // TODO: 도메인 로직으로 분리하는 것이 나은가? or Member를 제공받아서 getProfile() 등을 쓰는게 좋은가? 고민이 필요한 부분
    public static MemberProfileInfo from(MemberProfile memberProfile) {
        if (memberProfile == null) {
            return MemberProfileInfo.builder().build();
        }

        return MemberProfileInfo.builder()
            .id(memberProfile.getId())
            .linkedIn(memberProfile.getLinkedIn())
            .instagram(memberProfile.getInstagram())
            .github(memberProfile.getGithub())
            .blog(memberProfile.getBlog())
            .personal(memberProfile.getPersonal())
            .build();
    }

    /**
     * 다른 회원에게 보여줄 프로필입니다. 비공개로 설정한 링크는 null로 채웁니다.
     */
    public static MemberProfileInfo publicFrom(MemberProfile memberProfile) {
        if (memberProfile == null) {
            return MemberProfileInfo.builder().build();
        }

        return MemberProfileInfo.builder()
            .id(memberProfile.getId())
            .linkedIn(publicLink(memberProfile, MemberProfileLinkType.LINKEDIN))
            .instagram(publicLink(memberProfile, MemberProfileLinkType.INSTAGRAM))
            .github(publicLink(memberProfile, MemberProfileLinkType.GITHUB))
            .blog(publicLink(memberProfile, MemberProfileLinkType.BLOG))
            .personal(publicLink(memberProfile, MemberProfileLinkType.PERSONAL))
            .build();
    }

    private static String publicLink(MemberProfile memberProfile, MemberProfileLinkType type) {
        return memberProfile.isLinkPublic(type) ? memberProfile.getLink(type) : null;
    }
}
