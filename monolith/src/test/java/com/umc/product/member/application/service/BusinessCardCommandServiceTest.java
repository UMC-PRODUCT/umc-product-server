package com.umc.product.member.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.member.application.port.in.command.dto.EditBusinessCardCommand;
import com.umc.product.member.application.port.in.command.dto.EditBusinessCardCommand.Contact;
import com.umc.product.member.application.port.in.command.dto.EditBusinessCardCommand.Link;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.application.port.out.SaveMemberProfilePort;
import com.umc.product.member.domain.LinkTypeAndLink;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.MemberProfile;
import com.umc.product.member.domain.MemberProfileLinkType;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusinessCardCommandService")
class BusinessCardCommandServiceTest {

    private static final Long MEMBER_ID = 1L;

    @Mock
    LoadMemberPort loadMemberPort;

    @Mock
    SaveMemberPort saveMemberPort;

    @Mock
    SaveMemberProfilePort saveMemberProfilePort;

    @Mock
    MemberRegistrationValidator registrationValidator;

    @InjectMocks
    BusinessCardCommandService sut;

    @Test
    @DisplayName("프로필이 없으면 연락처·링크·공개 여부를 담은 프로필을 새로 만들어 회원에 할당한다")
    void 프로필이_없으면_연락처_링크_공개_여부를_담은_프로필을_새로_만들어_회원에_할당한다() {
        // given
        Member member = member();
        given(loadMemberPort.findByIdForUpdate(MEMBER_ID)).willReturn(Optional.of(member));

        // when
        sut.edit(new EditBusinessCardCommand(
            MEMBER_ID,
            null,
            new Contact("contact@example.com", false),
            List.of(new Link(MemberProfileLinkType.GITHUB, "https://github.com/umc", false))
        ));

        // then
        MemberProfile profile = member.getProfile();
        assertThat(profile.getGithub()).isEqualTo("https://github.com/umc");
        assertThat(profile.isLinkPublic(MemberProfileLinkType.GITHUB)).isFalse();
        // 연락처는 로그인 이메일과 별개로 저장한다.
        assertThat(profile.getContactEmail()).isEqualTo("contact@example.com");
        assertThat(profile.isContactEmailPublic()).isFalse();
        assertThat(member.getEmail()).isEqualTo("gildong@example.com");
        then(saveMemberProfilePort).should().save(profile);
        then(saveMemberPort).should().save(member);
    }

    @Test
    @DisplayName("프로필이 있으면 링크를 전체 교체하고 연락처와 공개 여부를 변경한다")
    void 프로필이_있으면_링크를_전체_교체하고_연락처와_공개_여부를_변경한다() {
        // given
        Member member = member();
        MemberProfile profile = MemberProfile.fromLinks(List.of(
            new LinkTypeAndLink(MemberProfileLinkType.GITHUB, "https://github.com/old"),
            new LinkTypeAndLink(MemberProfileLinkType.BLOG, "https://blog.example.com")
        ));
        profile.updateContactEmail("old@example.com");
        profile.changeVisibility(null, Map.of(MemberProfileLinkType.BLOG, false));
        member.assignProfile(profile);
        given(loadMemberPort.findByIdForUpdate(MEMBER_ID)).willReturn(Optional.of(member));

        // when
        sut.edit(new EditBusinessCardCommand(
            MEMBER_ID,
            null,
            new Contact("", true),
            List.of(new Link(MemberProfileLinkType.GITHUB, "https://github.com/new", false))
        ));

        // then
        assertThat(profile.getGithub()).isEqualTo("https://github.com/new");
        assertThat(profile.isLinkPublic(MemberProfileLinkType.GITHUB)).isFalse();
        // 목록에 없는 타입의 링크는 삭제하고, 공개 여부는 유지한다.
        assertThat(profile.getBlog()).isNull();
        assertThat(profile.isLinkPublic(MemberProfileLinkType.BLOG)).isFalse();
        // 이메일을 비워 보내면 삭제한다.
        assertThat(profile.getContactEmail()).isNull();
        then(saveMemberProfilePort).should(never()).save(any());
        then(saveMemberPort).should(never()).save(any());
    }

    @Test
    @DisplayName("프로필 사진 파일 ID를 보내면 파일을 검증하고 사진을 변경한다")
    void 프로필_사진_파일_ID를_보내면_파일을_검증하고_사진을_변경한다() {
        // given
        Member member = member();
        given(loadMemberPort.findByIdForUpdate(MEMBER_ID)).willReturn(Optional.of(member));

        // when
        sut.edit(new EditBusinessCardCommand(MEMBER_ID, "new-image", new Contact(null, true), List.of()));

        // then
        then(registrationValidator).should().validateProfileImageExists("new-image");
        assertThat(member.getProfileImageId()).isEqualTo("new-image");
    }

    @Test
    @DisplayName("프로필 사진 파일 ID를 생략하면 기존 사진을 유지한다")
    void 프로필_사진_파일_ID를_생략하면_기존_사진을_유지한다() {
        // given
        Member member = Member.create("홍길동", "길동", "gildong@example.com", 1L, "old-image");
        given(loadMemberPort.findByIdForUpdate(MEMBER_ID)).willReturn(Optional.of(member));

        // when
        sut.edit(new EditBusinessCardCommand(MEMBER_ID, null, new Contact(null, true), List.of()));

        // then
        then(registrationValidator).should(never()).validateProfileImageExists(any());
        assertThat(member.getProfileImageId()).isEqualTo("old-image");
    }

    private Member member() {
        return Member.create("홍길동", "길동", "gildong@example.com", 1L, null);
    }
}
