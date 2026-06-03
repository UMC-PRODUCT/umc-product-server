package com.umc.product.member.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.umc.product.member.application.port.in.command.dto.UpsertMemberProfileCommand;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.application.port.out.SaveMemberProfilePort;
import com.umc.product.member.domain.LinkTypeAndLink;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.MemberProfile;
import com.umc.product.member.domain.MemberProfileLinkType;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberProfileCommandService")
class MemberProfileCommandServiceTest {

    @Mock
    LoadMemberPort loadMemberPort;

    @Mock
    SaveMemberPort saveMemberPort;

    @Mock
    SaveMemberProfilePort saveMemberProfilePort;

    @InjectMocks
    MemberProfileCommandService sut;

    @Test
    @DisplayName("프로필이 없으면 새 프로필을 생성해 회원에 할당한다")
    void 프로필이_없으면_새_프로필을_생성해_회원에_할당한다() {
        Member member = member();
        given(loadMemberPort.findByIdForUpdate(1L)).willReturn(Optional.of(member));

        sut.upsert(UpsertMemberProfileCommand.builder()
            .memberId(1L)
            .links(List.of(new LinkTypeAndLink(MemberProfileLinkType.GITHUB, "https://github.com/umc")))
            .build());

        assertThat(member.getProfile()).isNotNull();
        assertThat(member.getProfile().getGithub()).isEqualTo("https://github.com/umc");
        then(saveMemberProfilePort).should().save(any(MemberProfile.class));
        then(saveMemberPort).should().save(member);
    }

    @Test
    @DisplayName("프로필이 있으면 기존 링크를 갱신한다")
    void 프로필이_있으면_기존_링크를_갱신한다() {
        Member member = member();
        MemberProfile profile = MemberProfile.fromLinks(List.of(
            new LinkTypeAndLink(MemberProfileLinkType.GITHUB, "https://github.com/old")
        ));
        member.assignProfile(profile);
        given(loadMemberPort.findByIdForUpdate(1L)).willReturn(Optional.of(member));

        sut.upsert(UpsertMemberProfileCommand.builder()
            .memberId(1L)
            .links(List.of(new LinkTypeAndLink(MemberProfileLinkType.BLOG, "https://blog.example.com")))
            .build());

        assertThat(profile.getGithub()).isNull();
        assertThat(profile.getBlog()).isEqualTo("https://blog.example.com");
        then(saveMemberProfilePort).should(never()).save(any());
        then(saveMemberPort).should(never()).save(any());
    }

    @Test
    @DisplayName("프로필이 없으면 삭제할 수 없다")
    void 프로필이_없으면_삭제할_수_없다() {
        Member member = member();
        given(loadMemberPort.findByIdForUpdate(1L)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> sut.delete(1L))
            .isInstanceOf(MemberDomainException.class)
            .extracting("baseCode")
            .isEqualTo(MemberErrorCode.MEMBER_PROFILE_NOT_FOUND);

        then(saveMemberProfilePort).should(never()).delete(any());
        then(saveMemberPort).should(never()).save(any());
    }

    @Test
    @DisplayName("프로필을 삭제하면 회원 연결을 제거하고 프로필을 삭제한다")
    void 프로필을_삭제하면_회원_연결을_제거하고_프로필을_삭제한다() {
        Member member = member();
        MemberProfile profile = MemberProfile.fromLinks(List.of(
            new LinkTypeAndLink(MemberProfileLinkType.GITHUB, "https://github.com/umc")
        ));
        member.assignProfile(profile);
        given(loadMemberPort.findByIdForUpdate(1L)).willReturn(Optional.of(member));

        sut.delete(1L);

        assertThat(member.getProfile()).isNull();
        then(saveMemberPort).should().save(member);
        then(saveMemberProfilePort).should().delete(profile);
    }

    private Member member() {
        return Member.create("홍길동", "길동", "gildong@example.com", 1L, null);
    }
}
