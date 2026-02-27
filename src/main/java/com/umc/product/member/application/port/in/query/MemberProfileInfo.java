package com.umc.product.member.application.port.in.query;

import com.umc.product.member.domain.MemberProfile;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

/**
 * 회원의 프로필 정보를 나타내는 DTO
 * <p>
 * 회원 자체의 ID나 프로필 이미지 등은 {@link MemberInfo}에 있습니다. 여기는 프로필 링크나 부가적인, 조회할 일이 적은 정보들을 포함합니다.
 */
@Slf4j
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
            log.warn("MemberProfile이 null인 사용자를 호출하였습니다.");
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
}
